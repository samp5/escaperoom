package group24.escaperoom.entities.properties.conditionals;

import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.entities.properties.Toggleable;
import group24.escaperoom.screens.MapScreen;
import group24.escaperoom.ui.RequiredItemEntry.RequiredItem;

public class ItemsAreToggled extends RequiresItems {
  Logger log = Logger.getLogger(ItemsAreToggled.class.getName());


  @Override
  public boolean evaluate(GameContext ctx) {
    for (RequiredItem i : items) {
      boolean isToggled = i.getItem().getProperty(PropertyType.Toggleable, Toggleable.class).get().isToggled();
      if (!i.getRequired().matches(isToggled)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.ItemsAreToggled;
  }

  protected Array<Item> getPotentialItems(MapScreen map){
    Array<Item> potentialItems = new Array<>();
    map.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.Toggleable)) {
        potentialItems.add(item);
      }
    });
    return potentialItems;
  }

  protected String getEmptyMessage(){
    return "No Toggleable Items on the map";
  }


  @Override
  public String getName() {
    return "Items are toggled";
  }
}
