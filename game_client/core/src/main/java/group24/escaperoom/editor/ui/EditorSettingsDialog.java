package group24.escaperoom.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import group24.escaperoom.engine.assets.maps.MapLoader;
import group24.escaperoom.ui.SettingsDialog;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.screens.OnlineMainMenu;
import group24.escaperoom.screens.MainMenu;
import group24.escaperoom.screens.MapSelectScreen.MapSelectScreenBuilder;
import group24.escaperoom.screens.utils.ScreenManager;
import group24.escaperoom.services.User;

public class EditorSettingsDialog extends SettingsDialog {
  private class MainMenuButton extends G24TextButton {
    public MainMenuButton() {
      super("Main Menu");
      addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScreenManager
            .instance()
            .showScreen(User.isLoggedIn() ? new OnlineMainMenu() : new MainMenu());
        }
      });
    }
  }

  private class MapSelectButton extends G24TextButton {
    public MapSelectButton() {
      super("Map Select");
      addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ScreenManager.instance().showScreen(
              new MapSelectScreenBuilder(User.isLoggedIn() ? new OnlineMainMenu() : new MainMenu())
                  .withMaps(MapLoader.discoverMaps())
                  .edit()
                  .play()
                  .delete()
                  .verify()
                  .creation()
                  .build());
        }
      });
    }
  }

  public EditorSettingsDialog() {
    super();
    button(new MainMenuButton());
    button(new MapSelectButton());
  }
}
