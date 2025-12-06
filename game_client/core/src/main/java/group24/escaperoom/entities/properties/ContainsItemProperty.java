package group24.escaperoom.entities.properties;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.GameEvent;
import group24.escaperoom.data.GameEvent.EventType;
import group24.escaperoom.data.GameEventBus;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.data.Grid;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.ui.ContainerUI;
import group24.escaperoom.ui.GameDialog;
import group24.escaperoom.ui.ItemSelectUI;
import group24.escaperoom.ui.ItemSelectUI.SelectedItem;
import group24.escaperoom.ui.editor.ConfigurationMenu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;

public class ContainsItemProperty
    extends ItemProperty<group24.escaperoom.entities.properties.ContainsItemProperty.ContainedItem> {

  private static final PropertyDescription description = new PropertyDescription(
    "Contains Items",
    "Can contain other items",
    "Items with the contains items property can hold items inside them.",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  protected Array<SelectedItem> selectedItem = new Array<>();
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(ContainsItemProperty.class.getName());

  public class OpenAction implements PlayerAction {

    @Override
    public String getActionName() {
      return "Open";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      Optional<LockedProperty> locked = owner.getProperty(PropertyType.LockedProperty, LockedProperty.class);
      if (locked.isPresent() && locked.get().isLocked()) {

        LockedProperty lockedProp = locked.get();
        PlayerAction unlockAction = lockedProp.getCurrentValue().getUnlockAction(ctx);

        if (unlockAction != null) unlockAction.act(ctx);

        if (lockedProp.isLocked()){
          GameEventBus.get().post(
            new GameEvent.Builder(EventType.ItemStateChange, ctx)
              .message("Can't open " + owner.getItemName() + ", it is still locked!")
              .build()
          );
          return ActionResult.DEFAULT;
        }
      }
      return new ActionResult().showsDialog(
        new GameDialog(new ContainerUI(ContainsItemProperty.this, ctx.player), ctx.player, "Contained...")
      );
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  public class ContainedItem implements ItemPropertyValue {
    Item inner;

    public ContainedItem(Item item, Item container) {
      this.inner = item;
    }

    public Item getItem() {
      return inner;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ContainedItem) {
        return ((ContainedItem) obj).getItem().getID() == this.getItem().getID();
      }
      return false;
    }
  }

  public void addValue(ContainedItem o) {
    selectedItem.add(new SelectedItem(o.inner));
  }

  public void removeItem(Item i) {
    selectedItem.removeValue(new SelectedItem(i), false);
  }

  @Override
  public Array<ContainedItem> getPotentialValues() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Contained Items";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ContainsItemsProperty;
  }

  @Override
  public void set(Array<ContainedItem> value) {
    selectedItem.clear();
    value.forEach(c -> addValue(c));
  }


  @Override
  public MenuType getInputType() {
    return MenuType.PopOut;
  }

  @Override
  public ConfigurationMenu<ItemSelectUI> getPopOut(MenuEntry parent) {
    LevelEditorScreen editor = (LevelEditorScreen)parent.getScreen();

    Array<Item> potentialValues = new Array<>();
    for (Item item : editor.getGrid().placedItems.values()) {
      if (item.getID() == owner.getID()) {
        continue;
      }
      if (item.hasProperty(PropertyType.Containable)) {
        potentialValues.add(item);
      }
    }
    this.selectedItem.forEach((si) -> potentialValues.add(si.getItem()));

    ItemSelectUI ui = new ItemSelectUI(potentialValues, 
                                       "No containable items are currently on the grid!",
                                       selectedItem, true, editor);
    ui.setOnSelect((i) -> {
      i.setContained(true);
      i.remove(false);
      return null;
    });
    ui.setOnDeselect((i) -> {
      i.setContained(false);
      editor.placeItem(i);
      return null;
    });
    ConfigurationMenu<ItemSelectUI> m = new ConfigurationMenu<>(parent, ui, "Contains Items", editor);
    return m;
  }

  @Override
  public Array<ContainedItem> getCurrentValues() {
    Array<ContainedItem> items = new Array<>();
    selectedItem.forEach((si) -> {
      items.add(new ContainedItem(si.getItem(), owner));
    });
    return items;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new OpenAction());
  }

  @Override
  public ContainsItemProperty cloneProperty(Item newOwner) {
    ContainsItemProperty prop = new ContainsItemProperty();
    prop.owner = newOwner;
    // should not allow multiple ContainsItemProperty items to contain
    // the same item, so simply returns a new empty property.
    // --> prop.selectedItem.addAll(selectedItem);
    return prop;
  }

  @Override
  public void write(Json json) {
    json.writeArrayStart("contained");
    this.selectedItem.forEach((contained) -> {
      json.writeValue(contained.getItem().getID());
    });
    json.writeArrayEnd();
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue containedData = jsonData.get("contained");
    Array<Integer> ids = new Array<>();
    if (containedData != null) {
      containedData.forEach((JsonValue itemJson) -> {
        try {
          ids.add(itemJson.asInt());
        } catch (Exception e) {
          Item i = new Item();
          i.read(json, itemJson);
          ids.add(i.getID());
        }
      });
    }

    Grid.onMapCompletion.add((g) -> {
      ids.forEach((i) -> {
        this.selectedItem.add(new SelectedItem(g.items.get(i)));
      });
      return null;
    });
  }

  @Override
  public Class<ContainedItem> getValueClass() {
    return ContainedItem.class;
  }
}
