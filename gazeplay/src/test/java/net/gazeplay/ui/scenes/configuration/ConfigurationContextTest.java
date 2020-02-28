package net.gazeplay.ui.scenes.configuration;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import mockit.Expectations;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.ui.scenes.gamemenu.GameButtonOrientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class ConfigurationContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Configuration mockConfig;

    @Mock
    private ConfigurationContext mockContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(20d, 20d));
    }

    @Test
    void shouldReturnToMenuOnHomeButtonPress() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        HomeButton button = context.createHomeButtonInConfigurationManagementScreen(mockGazePlay);
        button.fireEvent(TestingUtils.clickOnTarget(button));

        verify(mockGazePlay).onReturnToMenu();
    }

    @Test
    void shouldBuildConfigGridPane() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        GridPane pane = context.buildConfigGridPane(context, mockTranslator);

        ObservableList<Node> children = pane.getChildren();

        assertEquals(63, children.size());
        assertTrue(children.get(3) instanceof MenuButton);
        assertTrue(children.get(7) instanceof ChoiceBox);
        assertTrue(children.get(9) instanceof Spinner);
        assertTrue(children.get(11) instanceof CheckBox);
        assertTrue(children.get(15) instanceof ChoiceBox);
        assertTrue(children.get(17) instanceof Spinner);
        assertTrue(children.get(21) instanceof ChoiceBox);
        assertTrue(children.get(23) instanceof CheckBox);
        assertTrue(children.get(25) instanceof ChoiceBox);
        assertTrue(children.get(41) instanceof CheckBox);
        assertTrue(children.get(43) instanceof ChoiceBox);
        assertTrue(children.get(49) instanceof CheckBox);
        assertTrue(children.get(51) instanceof CheckBox);
        assertTrue(children.get(55) instanceof CheckBox);
        assertTrue(children.get(57) instanceof CheckBox);
        assertTrue(children.get(61) instanceof CheckBox);
    }

    @Test
    void shouldAddCategoryTitleLeftAligned() {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(HPos.CENTER, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddCategoryTitleRightAligned() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(HPos.CENTER, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddSubcategoryTitleLeftAligned() {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addSubCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().get(1) instanceof Separator);
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddSubcategoryTitleRightAligned() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addSubCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().get(1) instanceof Separator);
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddNodeToGridTitleLeftAligned() {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");
        CheckBox input = new CheckBox();

        context.addToGrid(grid, currentFormRow, label, input);

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(input));
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddNodeToGridRightAligned() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");
        CheckBox input = new CheckBox();

        context.addToGrid(grid, currentFormRow, label, input);

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(input));
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldBuildSpinner() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.increment(5);
        assertEquals(1, result.getValue());
        assertEquals(1000, length.get());
    }

    @Test
    void shouldSetSpinnerValueToMaxIfHigher() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.getEditor().setText("11");
        result.commitValue();

        assertEquals(10, result.getValue());
        assertEquals(10000, length.get());
    }

    @Test
    void shouldSetSpinnerValueToMinIfLower() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.getEditor().setText("0.2");
        result.commitValue();

        assertEquals(0.3, result.getValue());
        assertEquals(300, length.get());
    }

    @Test
    void shouldCreateThemeChooserNonDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:BLUE");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:BLUE");
        when(mockConfig.getCssfileProperty()).thenReturn(cssFileProperty);
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ChoiceBox<BuiltInUiTheme> result = ConfigurationContext.buildStyleThemeChooser(mockConfig, mockContext);

        assertEquals(BuiltInUiTheme.values().length, result.getItems().size());
        assertEquals(BuiltInUiTheme.BLUE, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:BLUE");

        result.setValue(BuiltInUiTheme.GREEN);

        assertEquals(BuiltInUiTheme.GREEN, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:GREEN");
    }

    @Test
    void shouldCreateThemeChooserDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:WRONG");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:WRONG");
        when(mockConfig.getCssfileProperty()).thenReturn(cssFileProperty);
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ChoiceBox<BuiltInUiTheme> result = ConfigurationContext.buildStyleThemeChooser(mockConfig, mockContext);

        assertEquals(BuiltInUiTheme.values().length, result.getItems().size());
        assertEquals(BuiltInUiTheme.SILVER_AND_GOLD, result.getValue());

        result.setValue(BuiltInUiTheme.GREEN);

        assertEquals(BuiltInUiTheme.GREEN, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:GREEN");
    }

    @ParameterizedTest
    @EnumSource(ConfigurationContext.DirectoryType.class)
    void shouldCreateDirectoryChooser(ConfigurationContext.DirectoryType type) throws InterruptedException {
        new MockUp<BackgroundMusicManager>() {
            public BackgroundMusicManager getInstance() {
                return mock(BackgroundMusicManager.class);
            }
        };

        ConfigurationContext context = new ConfigurationContext(mockGazePlay);
        StringProperty fileDirProperty = new SimpleStringProperty(System.getProperty("user.home") + "/GazePlay/");
        Scene mockScene = mock(Scene.class);
        Window mockWindow = mock(Window.class);

        Map<ConfigurationContext.DirectoryType, String> answers = Map.of(
            ConfigurationContext.DirectoryType.FILE, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath(),
            ConfigurationContext.DirectoryType.WHERE_IS_IT, Configuration.DEFAULT_VALUE_WHEREISIT_DIR,
            ConfigurationContext.DirectoryType.MUSIC, new File(System.getProperty("user.home") + "/GazePlay/", "music").getAbsolutePath(),
            ConfigurationContext.DirectoryType.VIDEO, GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath()
        );

        when(mockConfig.getVideoFolder()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getVideoFolderProperty()).thenReturn(fileDirProperty);
        when(mockConfig.getWhereIsItDir()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getWhereIsItDirProperty()).thenReturn(fileDirProperty);
        when(mockConfig.getFileDir()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getFiledirProperty()).thenReturn(fileDirProperty);
        when(mockConfig.getMusicFolder()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getMusicFolderProperty()).thenReturn(fileDirProperty);

        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);

        HBox result = (HBox) context.buildDirectoryChooser(mockConfig, mockContext, mockTranslator, type);
        Button loadButton = (Button) result.getChildren().get(0);
        Button resetButton = (Button) result.getChildren().get(1);

        assertEquals(fileDirProperty.getValue(), loadButton.textProperty().getValue());

        resetButton.fire();
        assertEquals(answers.get(type), fileDirProperty.getValue());
        assertEquals(answers.get(type), loadButton.textProperty().getValue());
    }

    @Test
    void shouldBuildLanguageChooser() throws InterruptedException {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);
        StringProperty languageProperty = new SimpleStringProperty("eng");
        StringProperty countryProperty = new SimpleStringProperty("GB");

        when(mockConfig.getLanguage()).thenReturn(languageProperty.getValue());
        when(mockConfig.getCountry()).thenReturn(countryProperty.getValue());
        when(mockConfig.getLanguageProperty()).thenReturn(languageProperty);
        when(mockConfig.getCountryProperty()).thenReturn(countryProperty);
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);

        MenuButton result = context.buildLanguageChooser(mockConfig, context);

        assertEquals(23, result.getItems().size());

        Platform.runLater(() -> {
            result.getItems().get(1).fire();
        });
        TestingUtils.waitForRunLater();

        ImageView image = (ImageView) result.getGraphic();
        assertTrue(image.getImage().getUrl().contains("Arab"));
        assertEquals("ara", languageProperty.getValue());
    }

    @Test
    void shouldBuildEyeTrackerChooser() {
        StringProperty eyeTrackerProperty = new SimpleStringProperty("mouse_control");

        when(mockConfig.getEyeTracker()).thenReturn(eyeTrackerProperty.getValue());
        when(mockConfig.getEyetrackerProperty()).thenReturn(eyeTrackerProperty);

        ChoiceBox<EyeTracker> result = ConfigurationContext.buildEyeTrackerConfigChooser(mockConfig);

        assertEquals(3, result.getItems().size());

        result.setValue(EyeTracker.eyetribe);

        assertEquals("eyetribe", eyeTrackerProperty.getValue());
    }

    @Test
    void shouldBuildCheckBox() throws InterruptedException {
        BooleanProperty testProperty = new SimpleBooleanProperty(true);

        CheckBox result = ConfigurationContext.buildCheckBox(testProperty);
        assertTrue(result.isSelected());

        Platform.runLater(result::fire);
        TestingUtils.waitForRunLater();

        assertFalse(testProperty.getValue());
    }

    @Test
    void shouldBuildGameButtonOrientationChooser() {
        StringProperty buttonOrientationProperty = new SimpleStringProperty("HORIZONTAL");
        when(mockConfig.getMenuButtonsOrientationProperty()).thenReturn(buttonOrientationProperty);
        when(mockConfig.getMenuButtonsOrientation()).thenReturn(buttonOrientationProperty.getValue());

        ChoiceBox<GameButtonOrientation> result = ConfigurationContext.buildGameButtonOrientationChooser(mockConfig);

        assertEquals(2, result.getItems().size());
        result.setValue(GameButtonOrientation.VERTICAL);

        assertEquals("VERTICAL", buttonOrientationProperty.getValue());
    }

    @Test
    void shouldChangeTheMusicFolderAndPlayIfWasPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                       @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = true;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 1;
        }};
    }

    @Test
    void shouldChangeTheMusicFolderAndNotPlayIfWasNotPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                             @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 0;
        }};
    }

    @Test
    void shouldChangeTheMusicFolderWithBlankFolder(@Mocked BackgroundMusicManager mockMusicManager,
                                                   @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();
        String expectedFolder = System.getProperty("user.home") + File.separator + "GazePlay" + File.separator + "music";

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("", mockConfiguration);

        new Verifications() {{
            mockMusicManager.getAudioFromFolder(expectedFolder);
        }};
    }

    @Test
    void shouldSetupANewMusicFolder() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void shouldSetupANewMusicFolderIfTheFolderExists() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void shouldSetupANewMusicFolderIfTheSongDoesntExist() {
        String songName = "fakesong.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertFalse(expectedFile.exists());

        assertTrue(testFolder.delete());
    }

    @Test
    void shouldBuildQuitKeyChooser() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);
        StringProperty quitProperty = new SimpleStringProperty("Y");

        when(mockConfig.getQuitKeyProperty()).thenReturn(quitProperty);

        ChoiceBox<String> result = context.buildQuitKeyChooser(mockConfig);
        assertEquals(6, result.getItems().size());

        result.setValue("Q");
        assertEquals("Q", quitProperty.getValue());
    }

    @Test
    void shouldBuildHeatMapOpacityChoiceBox() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);
        DoubleProperty heatMapProperty = new SimpleDoubleProperty(1);

        when(mockConfig.getHeatMapOpacityProperty()).thenReturn(heatMapProperty);
        when(mockConfig.getHeatMapOpacity()).thenReturn(heatMapProperty.getValue());

        ChoiceBox<Double> result = context.buildHeatMapOpacityChoiceBox(mockConfig);

        assertEquals(11, result.getItems().size());

        result.setValue(0.2);
        assertEquals(0.2, heatMapProperty.getValue());
    }

    @Test
    void shouldBuildHeatMapColorHBox() throws InterruptedException {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);
        StringProperty heatMapProperty = new SimpleStringProperty("001122, 110022, 002211, 112200");
        List<Color> colors = List.of(Color.web("001122"), Color.web("110022"), Color.web("002211"), Color.web("112200"));

        when(mockConfig.getHeatMapColorsProperty()).thenReturn(heatMapProperty);
        when(mockConfig.getHeatMapColors()).thenReturn(colors);

        HBox result = context.buildHeatMapColorHBox(mockConfig, mockTranslator);
        assertEquals(7, result.getChildren().size());

        Button reset = (Button) result.getChildren().get(0);
        Button plus = (Button) result.getChildren().get(1);
        Button minus = (Button) result.getChildren().get(2);

        Platform.runLater(plus::fire);
        TestingUtils.waitForRunLater();

        assertEquals(8, result.getChildren().size());

        Platform.runLater(reset::fire);
        TestingUtils.waitForRunLater();

        assertEquals(7, result.getChildren().size());

        Platform.runLater(minus::fire);
        TestingUtils.waitForRunLater();

        assertEquals(6, result.getChildren().size());
    }
}
