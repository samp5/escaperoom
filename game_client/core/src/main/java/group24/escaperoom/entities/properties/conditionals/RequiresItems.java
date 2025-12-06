package group24.escaperoom.entities.properties.conditionals;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.data.Grid;
import group24.escaperoom.entities.Item;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.MapScreen;
import group24.escaperoom.ui.RequireItemsUI;
import group24.escaperoom.ui.RequiredItemEntry.ItemRequired;
import group24.escaperoom.ui.RequiredItemEntry.RequiredItem;
import group24.escaperoom.ui.SmallLabel;
import group24.escaperoom.ui.editor.ConfigurationMenu.HandlesMenuClose;

public abstract class RequiresItems extends Conditional {
  Array<RequiredItem> items = new Array<>();

  public void removeStaleItems(){
    Array<RequiredItem> newArr = new Array<>();
    for (RequiredItem item : this.items){
      if (Grid.current().items.containsKey(item.getItem().getID())){
        newArr.add(item);
      }
    }
    this.items = newArr;
  }

  @Override
  public void write(Json json) {
    removeStaleItems();
    json.writeArrayStart("items");
    items.forEach((i) -> {
      json.writeObjectStart();
      json.writeValue("id", i.getItem().getID());
      json.writeValue("required", i.getRequired().name());
      json.writeObjectEnd();
    });
    json.writeArrayEnd();
  }

  abstract protected Array<Item> getPotentialItems(MapScreen map);
  abstract protected String getEmptyMessage();

  private class EmptyMessage extends SmallLabel implements HandlesMenuClose {
    EmptyMessage(String msg){
      super(msg);
    }
    @Override
    public void handle() {
    }
  }


  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditorScreen stage) {
    removeStaleItems();
    // Find our potential items
    Array<Item> potentialItems = getPotentialItems(stage);

    if (potentialItems.isEmpty()) {
      return Optional.of(new EmptyMessage(getEmptyMessage()));
    }

    return Optional.of(new RequireItemsUI(potentialItems, this.items, stage));
  }


  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue items = jsonData.get("items");

    Grid.onMapCompletion.add((grid) -> {

      if (items != null) {
        items.forEach((tij) -> {
          Item item = grid.items.get(tij.getInt("id"));
          ItemRequired typeRequired = ItemRequired.valueOf(tij.getString("required"));
          RequiredItem ti = new RequiredItem(item, typeRequired);
          this.items.add(ti);
        });
      }
      return null;
    });
  }
}
