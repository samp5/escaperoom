package group24.escaperoom;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.badlogic.gdx.Game;

import group24.escaperoom.engine.assets.AssetManager;
import group24.escaperoom.engine.assets.items.ItemLoader;
import group24.escaperoom.engine.control.CursorManager;
import group24.escaperoom.engine.control.CursorManager.CursorType;
import group24.escaperoom.screens.MainMenu;
import group24.escaperoom.screens.utils.ScreenManager;


public class EscapeRoomGame extends Game {

  public void create() {
    configLogger();
    ItemLoader.LoadAllObjects();
    AssetManager.instance().finishLoading();
    ScreenManager.instance().initialize(this);
    CursorManager.setCursor(CursorType.Pointer);
    ScreenManager.instance().showScreen(new MainMenu());
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
