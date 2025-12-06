package group24.escaperoom.game.entities.properties.values;

import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;

/**
 * A wrapper around a boolean that is also a {@link ItemPropertyValue}
 */
public class BooleanValue implements ItemPropertyValue {
  protected boolean inner = false;

  /**
   * @param inner the inner boolean value
   */
  public BooleanValue(boolean inner) {
    this.inner = inner;
  }

  /**
   * @return whether this value is true
   */
  public boolean isTrue(){
    return inner;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return null;
  }
}
