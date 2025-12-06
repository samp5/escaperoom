package group24.escaperoom.game.entities.properties.values;

import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;

public interface ItemPropertyValue {

  /**
   * @param parent the menu spawning this entry
   * @return the {@link MenuEntry} describing this  {@link ItemPropertyValue}
   */
  default public MenuEntry getDisplay(Menu parent) { return null; }
}
