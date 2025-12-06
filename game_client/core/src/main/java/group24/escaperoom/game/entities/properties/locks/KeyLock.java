package group24.escaperoom.game.entities.properties.locks;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.FragileProperty;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.UnlockerProperty;
import group24.escaperoom.game.entities.properties.base.LockingMethod;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEvent.EventType;
import group24.escaperoom.game.state.GameEventBus;
import group24.escaperoom.game.world.Grid;
import group24.escaperoom.screens.LevelEditor;
import group24.escaperoom.ui.ItemSelectUI;
import group24.escaperoom.ui.ItemSelectUI.SelectedItem;

public class KeyLock extends LockingMethod {

  private Array<SelectedItem> selectedItems = new Array<>();

  private Optional<Item> holdsKey(GameContext ctx){
    for (Item i : ctx.player.getInventory()) {
      if (selectedItems.contains(new SelectedItem(i), false)){
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  /**
   * Check if an item is fragile, if so, "break" it.
   */
  private void checkFragile(GameContext ctx, Item item){
    item.getProperty(PropertyType.Fragile, FragileProperty.class).ifPresent((fp) -> {
      if (fp.isTrue()){
        ctx.player.removeItemFromInventory(item);
        GameEventBus.get().post(
          new GameEvent.Builder(EventType.ItemStateChange, ctx)
            .message(item.getItemName() + " was fragile and broke!")
            .build()
        );
      }
    });
  }

  /**
   * Try to set lock status to {@code isLocked}
   *
   * @return {@code Some(key)} if valid  
   */
  private Optional<Item> trySetLock(GameContext ctx, boolean isLocked){
    Optional<Item> maybeKey = holdsKey(ctx);

    if (maybeKey.isEmpty()){
      String msg = "Hm, " + owner.get().getItemName() +
                   " is still " + (this.isLocked ? "locked": "unlocked");
      GameEventBus.get().post(
        new GameEvent.Builder(EventType.ItemStateChange, ctx)
          .message(msg)
          .build()
      );
    } else {
      updateLocked(ctx, isLocked);
    }

    return maybeKey;
  }

  private void updateLocked(GameContext ctx, boolean locked){
      // We are locked, update state
      isLocked = locked;
      if (isBarrier) {
        owner.get().setBlocksPlayer(locked);
        owner.get().setAlpha(locked ? 1.0f: 0.5f);
      }

      GameEventBus.get().post(
        new GameEvent.Builder(EventType.ItemStateChange, ctx)
          .message(owner.get().getItemName() + " is now " + (locked ? "locked!": "unlocked!"))
          .build()
      );
  }

  protected class TryLock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Try to lock";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      trySetLock(ctx, true).ifPresent((key) -> checkFragile(ctx, key));
      return ActionResult.DEFAULT;
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return !isLocked;
    }
  };

  protected class TryUnlock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Try to unlock";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      trySetLock(ctx, false).ifPresent((key) -> checkFragile(ctx, key));
      return ActionResult.DEFAULT;
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked;
    }
  };

  @Override
  public String getName() {
    return "Key Lock";
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.KeyLock;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, getName())
      .spawns((e) -> {
        return new ConfigurationMenu<ItemSelectUI>(e,configurationDisplay((LevelEditor)parent.getScreen()), "Unlocked By", parent.getScreen());
      })
      .build();
  }

  private ItemSelectUI configurationDisplay(LevelEditor editor){
    HashSet<Item> potentialValues = new HashSet<>();
    for (Item i : editor.getGrid().items.values()) {
      i.getProperty(PropertyType.UnlocksProperty, UnlockerProperty.class).ifPresent((p) -> {
        potentialValues.add(i);
      });
    }
    Array<Item> potentialValueArray = Array.with(potentialValues.toArray(new Item[0]));

    ItemSelectUI ui = new ItemSelectUI(potentialValueArray,"No unlocker items on the grid!", selectedItems,  true, editor);
    return ui;
  }


  @Override
  public Array<PlayerAction> getActions() {
    return Array.with(new TryUnlock(), new TryLock());
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new KeyLock();
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeArrayStart("unlocked_by");
    selectedItems.forEach((si) -> {
      if (si != null && si.getItem() != null) json.writeValue(si.getItem().getID());
    });
    json.writeArrayEnd();
  }

  @Override
  public void read(Json json, JsonValue data) {
    super.read(json, data);

    JsonValue arr = data.get("unlocked_by");

    Array<Integer> ids = new Array<>();

    if (arr != null) arr.forEach((val) -> ids.add(val.asInt()));

    if (!ids.isEmpty()){
      Grid.onMapCompletion.add((g) -> {
        ids.forEach((id) -> selectedItems.add(new SelectedItem(g.items.get(id))));
        return null;
      });
    }
  }

  @Override
  protected PlayerAction maybeGetLockAction() {
    return new TryLock();
  }

  @Override
  protected PlayerAction maybeGetUnlockAction() {
    return new TryUnlock();
  }
}
