package net.gazeplay.commons.utils.stats;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.HeatMap;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.games.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.LinkedList;


/**
 * Created by schwab on 16/08/2017.
 */
@Slf4j
@ToString
public class Stats implements GazeMotionListener {

    private static final int trail = 10;
    private final double heatMapPixelSize = computeHeatMapPixelSize();
    private EventHandler<MouseEvent> recordMouseMovements;
    private EventHandler<GazeEvent> recordGazeMovements;
    private final Scene gameContextScene;
    private final LifeCycle lifeCycle = new LifeCycle();
    private final RoundsDurationReport roundsDurationReport = new RoundsDurationReport();
    protected String gameName;
    private int nbShots = 0;
    @Getter
    protected int nbGoals = 0;
    @Setter
    private long accidentalShotPreventionPeriod = 0;
    @Getter
    private int nbUnCountedShots;
    private double[][] heatMap;
    @Getter
    @Setter
    private long currentGazeTime;
    @Getter
    @Setter
    private long lastGazeTime;
    @Getter
    private LinkedList<FixationPoint> fixationSequence;
    @Getter
    private SavedStatsInfo savedStatsInfo;

    private Long currentRoundStartTime;

    public Stats(Scene gameContextScene) {
        this(gameContextScene, null);
    }

    public Stats(Scene gameContextScene, String gameName) {
        this.gameContextScene = gameContextScene;
        this.gameName = gameName;
    }

    private static double[][] instanciateHeatMapData(Scene gameContextScene, double heatMapPixelSize) {
        int heatMapWidth = (int) (gameContextScene.getHeight() / heatMapPixelSize);
        int heatMapHeight = (int) (gameContextScene.getWidth() / heatMapPixelSize);

        log.info("heatMapWidth = {}, heatMapHeight = {}", heatMapWidth, heatMapHeight);
        return new double[heatMapWidth][heatMapHeight];
    }

//    private void instantiateFixationSequence(int rows, int columns) {
//        fixationSequence = new FixationPoint[rows][columns];
//        for (int i = 0; i < fixationSequence.length; i++) {
//            for (int j = 0; j < fixationSequence[0].length; j++) {
//                fixationSequence[i][j] = new FixationPoint();
//            }
//        }
//    }

    public void notifyNewRoundReady() {
        currentRoundStartTime = System.currentTimeMillis();
    }

    public void start() {

        final Configuration config = Configuration.getInstance();

        lifeCycle.start(() -> {
            if (config.isHeatMapDisabled()) {
                log.info("HeatMap is disabled, skipping instantiation of the HeatMap Data model");
            } else {
                heatMap = instanciateHeatMapData(gameContextScene, heatMapPixelSize);
                fixationSequence = new LinkedList(); // do this if checkmark for fixation sequence is checked
                recordGazeMovements = e -> {

                    incHeatMap((int) e.getX(), (int) e.getY());
                    incFixationSequence((int) e.getX(), (int) e.getY());
                };
                recordMouseMovements = e -> {

                    incHeatMap((int) e.getX(), (int) e.getY());
                    incFixationSequence((int) e.getX(), (int) e.getY());
                };

                gameContextScene.addEventFilter(GazeEvent.ANY, recordGazeMovements);
                gameContextScene.addEventFilter(MouseEvent.ANY, recordMouseMovements);
            }
        });
        currentRoundStartTime = lifeCycle.getStartTime();
    }

    public void stop() {
        lifeCycle.stop(() -> {
            if (recordGazeMovements != null) {
                gameContextScene.removeEventFilter(GazeEvent.ANY, recordGazeMovements);
            }
            if (recordMouseMovements != null) {
                gameContextScene.removeEventFilter(MouseEvent.ANY, recordMouseMovements);
            }
        });
    }

    @Override
    public void gazeMoved(javafx.geometry.Point2D position) {
        final int positionX = (int) position.getX();
        final int positionY = (int) position.getY();
        incHeatMap(positionX, positionY);
        incFixationSequence(positionX,positionY); //check if good
    }

    public SavedStatsInfo saveStats() throws IOException {

        File todayDirectory = getGameStatsOfTheDayDirectory();
        final String heatmapFilePrefix = Utils.now() + "-heatmap";
        final String fixationSequenceFilePrefix = Utils.now() + "-fixationSequence";

        File heatMapPngFile = new File(todayDirectory, heatmapFilePrefix + ".png");
        File heatMapCsvFile = new File(todayDirectory, heatmapFilePrefix + ".csv");

        File fixationSequencePngFile = new File(todayDirectory, fixationSequenceFilePrefix + ".png");

        SavedStatsInfo savedStatsInfo = new SavedStatsInfo(heatMapPngFile, heatMapCsvFile, fixationSequencePngFile);

        this.savedStatsInfo = savedStatsInfo;

        if (this.heatMap != null) {
            saveHeatMapAsPng(heatMapPngFile);
            saveHeatMapAsCsv(heatMapCsvFile);
            saveFixationSequenceAsPng(fixationSequencePngFile);
        }

        savedStatsInfo.notifyFilesReady();
        return savedStatsInfo;
    }

    public long computeRoundsDurationAverageDuration() {
        return roundsDurationReport.computeAverageLength();
    }

    public long computeRoundsDurationMedianDuration() {
        return roundsDurationReport.computeMedianDuration();
    }

    public long getRoundsTotalAdditiveDuration() {
        return roundsDurationReport.getTotalAdditiveDuration();
    }

    public long computeTotalElapsedDuration() {
        return lifeCycle.computeTotalElapsedDuration();
    }

    public double computeRoundsDurationVariance() {
        return roundsDurationReport.computeVariance();
    }

    public double computeRoundsDurationStandardDeviation() {
        return roundsDurationReport.computeSD();
    }

    public void incNbGoals() {
        final long currentRoundEndTime = System.currentTimeMillis();
        final long currentRoundDuration = currentRoundEndTime - currentRoundStartTime;
        if (currentRoundDuration < accidentalShotPreventionPeriod) {
            nbUnCountedShots++;
        } else {
            nbGoals++;
            this.roundsDurationReport.addRoundDuration(currentRoundDuration);
        }
        currentRoundStartTime = currentRoundEndTime;
        log.debug("The number of goals is " + nbGoals + "and the number shots is " + nbShots);
    }

    public int getShotRatio() {
        if (this.nbGoals == this.nbShots || this.nbShots == 0) {
            return 100;
        } else {
            int ratioRate = (int) ((float) this.nbGoals / (float) this.nbShots * 100.0);
            return ratioRate;
        }
    }

    public void incNbShots() {
        this.nbShots++;
    }

    public List<Long> getSortedDurationsBetweenGoals() {
        return this.roundsDurationReport.getSortedDurationsBetweenGoals();
    }

    public List<Long> getOriginalDurationsBetweenGoals() {
        return this.roundsDurationReport.getOriginalDurationsBetweenGoals();
    }

    File createInfoStatsFile() {
        File outputDirectory = getGameStatsOfTheDayDirectory();
        final String fileName = Utils.now() + "-info-game.csv";
        return new File(outputDirectory, fileName);
    }

    File getGameStatsOfTheDayDirectory() {
        File statsDirectory = new File(Utils.getUserStatsFolder(Configuration.getInstance().getUserName()));
        File gameDirectory = new File(statsDirectory, gameName);
        File todayDirectory = new File(gameDirectory, Utils.today());
        boolean outputDirectoryCreated = todayDirectory.mkdirs();
        log.info("outputDirectoryCreated = {}", outputDirectoryCreated);

        return todayDirectory;
    }

    void printLengthBetweenGoalsToString(PrintWriter out) {
        this.roundsDurationReport.printLengthBetweenGoalsToString(out);
    }

    private void saveHeatMapAsCsv(File file) throws IOException {
        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            for (int i = 0; i < heatMap.length; i++) {
                for (int j = 0; j < heatMap[0].length - 1; j++) {
                    out.print((int) heatMap[i][j]);
                    out.print(", ");
                }
                out.print((int) heatMap[i][heatMap[i].length - 1]);
                out.println("");
            }
        }
    }


//    private void saveFixationPointsAsCsv(File file) throws IOException {
//        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
//            for (int i = 0; i < fixationSequence.length; i++) {
//                for (int j = 0; j < fixationSequence[0].length - 1; j++) {
//                    out.print(fixationSequence[i][j].getFirstGaze());
//                    out.print(", ");
//                }
//                out.print(fixationSequence[i][fixationSequence[i].length - 1].getFirstGaze());
//                out.println("");
//            }
//        }
//    }
    private void saveHeatMapAsPng(File outputPngFile) {

        log.info(String.format("Heatmap size: %3d X %3d", heatMap[0].length, heatMap.length));

        HeatMap map = new HeatMap(heatMap);

        try {
            map.saveToFile(outputPngFile);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
    private void saveFixationSequenceAsPng(File outputPngFile){
        //
        // the function to draw in Fixation Sequence Class
        //
    }

    private void incFixationSequence(int X, int Y){
        long previousGaze ;
        long gazeDuration ;

        int x = (int) (Y / heatMapPixelSize);
        int y = (int) (X / heatMapPixelSize);
        if(fixationSequence.size()==0){
            previousGaze = 0;
        }
        else{
            previousGaze = (fixationSequence.get(fixationSequence.size()-1)).getFirstGaze();
        }
        FixationPoint newGazePoint = new FixationPoint(System.currentTimeMillis(),0,x,y);
        gazeDuration = previousGaze - newGazePoint.getFirstGaze();
        newGazePoint.setGazeDuration(gazeDuration);
        fixationSequence.add(newGazePoint);

    }

    private void incHeatMap(int X, int Y) {

        currentGazeTime = System.currentTimeMillis();
        // in heatChart, x and y are opposed
        int x = (int) (Y / heatMapPixelSize);
        int y = (int) (X / heatMapPixelSize);

        for (int i = -trail; i <= trail; i++)
            for (int j = -trail; j <= trail; j++) {

                if (Math.sqrt(i * i + j * j) < trail) {

                    inc(x + i, y + j);
                }
            }
    }

    private void inc(int x, int y) {
        if (heatMap != null && x >= 0 && y >= 0 && x < heatMap.length && y < heatMap[0].length) {
            // heatMap[heatMap[0].length - y][heatMap.length - x]++;
            //currentGazeTime = System.currentTimeMillis();

            //fixationSequence[x][y].setFirstGaze(firstGazeVal/1000);
            heatMap[x][y]++;
        }
    }
    /**
     * @return the size of the HeatMap Pixel Size in order to avoid a too big heatmap (400 px) if maximum memory is more
     *         than 1Gb, only 200
     */
    private double computeHeatMapPixelSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        double width = Screen.getPrimary().getBounds().getWidth();
        double result;
        if (maxMemory < 1024 * 1024 * 1024) {
            // size is less than 1Gb (2^30)
            result = width / 200;
        } else {
            result = width / 400;
        }
        log.info("computeHeatMapPixelSize() : result = {}", result);
        return result;
    }

}
