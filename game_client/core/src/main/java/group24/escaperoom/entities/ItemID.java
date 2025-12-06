package group24.escaperoom.entities;


import group24.escaperoom.entities.properties.ItemPropertyValue;
import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;
import group24.escaperoom.ui.editor.Menu.MenuEntryBuilder;

public class ItemID implements ItemPropertyValue {
  int inner;

  public ItemID(int val) {
    inner = val;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ItemID) {
      return this.inner == ((ItemID) other).inner;
    }
    return false;
  }

  public int getID() {
    return inner;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, "ID:" + Integer.toString(inner)).build();
  }
}
