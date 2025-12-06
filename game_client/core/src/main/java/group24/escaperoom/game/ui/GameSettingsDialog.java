package group24.escaperoom.game.ui;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import group24.escaperoom.screens.GameScreen;
import group24.escaperoom.screens.GameSummary;
import group24.escaperoom.screens.utils.ScreenManager;
import group24.escaperoom.ui.SettingsDialog;
import group24.escaperoom.ui.widgets.G24TextButton;

public class GameSettingsDialog extends SettingsDialog {
  private class SurrenderButton extends G24TextButton {
    SurrenderButton(GameScreen screen) {
      super("Give Up");
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          screen.calculateStatistics(false);
          ScreenManager.instance().showScreen(new GameSummary(screen.stats, screen.getMetadata(), screen.getGameType()));
        }
      });

    }

  }
  public GameSettingsDialog(GameScreen game){
    super();
    button(new SurrenderButton(game));
  }
}
