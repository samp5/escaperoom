package group24.escaperoom;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.badlogic.gdx.Game;

import group24.escaperoom.entities.objects.ObjectLoader;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.MainMenuScreen;
import group24.escaperoom.screens.CursorManager.CursorType;


public class EscapeRoomGame extends Game {

  public void create() {
    configLogger();
    ObjectLoader.LoadAllObjects();
    AssetManager.instance().finishLoading();
    ScreenManager.instance().initialize(this);
    CursorManager.setCursor(CursorType.Pointer);
    ScreenManager.instance().showScreen(new MainMenuScreen());
  }

  public void configLogger() {
    Logger rootLogger = Logger.getLogger("");
    for (Handler handler : rootLogger.getHandlers()) {
      rootLogger.removeHandler(handler);
    }
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.ALL);
    consoleHandler.setFormatter(new SimpleFormatter());
    rootLogger.addHandler(consoleHandler);
    rootLogger.setLevel(Level.WARNING);
  }
}
