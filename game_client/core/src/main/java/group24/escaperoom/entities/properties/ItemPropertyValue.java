package group24.escaperoom.entities.properties;

import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;

public interface ItemPropertyValue {

  /**
   * @param parent the menu spawning this entry
   * @return the {@link MenuEntry} describing this  {@link ItemPropertyValue}
   */
  default public MenuEntry getDisplay(Menu parent) { return null; }
}
