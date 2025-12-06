package group24.escaperoom.entities.properties.conditionals;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.Grid;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.ui.AreaUI;
import group24.escaperoom.ui.ItemSelectUI;
import group24.escaperoom.ui.ItemSelectUI.SelectedItem;
import group24.escaperoom.ui.editor.ConfigurationMenu.HandlesMenuClose;
import group24.escaperoom.ui.SmallLabel;

public class ItemInArea extends Conditional {
  Logger log = Logger.getLogger(ItemInArea.class.getName());
  Rectangle targetRegion = new Rectangle();
  SelectedItem item = new SelectedItem();

	@Override
	public void write(Json json) {
    json.writeValue("x", targetRegion.x);
    json.writeValue("y", targetRegion.y);
    json.writeValue("width", targetRegion.width);
    json.writeValue("height", targetRegion.height);
    json.writeValue("item_id", item.getItem() == null ? -1 : item.getItem().getID());
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
    int x = jsonData.getInt("x", 0);
    int y = jsonData.getInt("y", 0);
    int width = jsonData.getInt("width", 0);
    int height = jsonData.getInt("height", 0);
    this.targetRegion = new Rectangle(x, y, width, height);

    int i = jsonData.getInt("item_id", -1);
    Grid.onMapCompletion.add((grid) -> {
      if (i != -1){
        this.item = new SelectedItem(grid.items.get(i));
      }
      return null;
    });
	}

	@Override
	public boolean evaluate(GameContext ctx) {
    if (item.getItem() != null && ctx.map.itemIsPlaced(item.getItem())) {
      return targetRegion.overlaps(item.getItem().getOccupiedRegion());
    }
    return false;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.ItemInArea;
	}

  private class ItemInRegionUI extends Table implements HandlesMenuClose {
    private ItemSelectUI itemSelect;
    private AreaUI areaPicker;

    @Override
    public void handle() {
      if (itemSelect != null) itemSelect.handle();
      areaPicker.handle();
    }
  }

  @Override
  public Optional<ItemInRegionUI> getEditorConfiguration(LevelEditorScreen screen) {

    Array<Item> potentialItems = new Array<>();
    screen.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.Obtainable)) {
        potentialItems.add(item);
      }
    });

    ItemInRegionUI itemInRegionUI = new ItemInRegionUI();

    itemInRegionUI.add(new SmallLabel("Item", "underline", 0.65f));
    itemInRegionUI.row();

    ItemSelectUI selectUI = new ItemSelectUI(potentialItems, this.item, "No obtainable items are currently on the grid!", screen);
    itemInRegionUI.add(selectUI);
    itemInRegionUI.row();

    itemInRegionUI.add(new SmallLabel("Region", "underline", 0.65f));
    itemInRegionUI.row();

    AreaUI areaUI = new AreaUI(screen, this.targetRegion);
    itemInRegionUI.add(areaUI);

    itemInRegionUI.areaPicker = areaUI;
    itemInRegionUI.itemSelect = selectUI;

    return Optional.of(itemInRegionUI);
  }

	@Override
	public String getName() {
    return "Item in area";
	}
}
