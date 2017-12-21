package net.gazeplay;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import net.gazeplay.commons.utils.stats.Stats;

public class GameSpec {

    public interface GameLauncher {

        Stats launchGame(GameSpec gameSpec, Scene scene, Group root, ChoiceBox<String> cbxGames);

    }

    private final String label;

    private final GameLauncher gameLauncher;

    public GameSpec(String label, GameLauncher gameLauncher) {
        this.label = label;
        this.gameLauncher = gameLauncher;
    }

    public String getLabel() {
        return label;
    }

    public Stats launch(Scene scene, Group root, ChoiceBox<String> cbxGames) {
        return gameLauncher.launchGame(this, scene, root, cbxGames);
    }
}
