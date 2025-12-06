package group24.escaperoom.entities.properties;

import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;
import group24.escaperoom.ui.editor.Menu.MenuEntryBuilder;

abstract public class LockingMethod implements Json.Serializable, ItemPropertyValue {
  protected boolean isBarrier = false;
  protected Optional<Item> owner = Optional.empty();
  protected boolean isLocked = true;

  // display name (Combination Lock, KeyLock)
  abstract public String getName();

  public boolean isLocked(){
    return isLocked;
  }

  abstract public Array<PlayerAction> getActions();

  /**
   * Get a player action that would unlock this locking method
   *
   * This can be null if the action is not valid, or if there is no such
   * action for this lock
   */
  public @Null PlayerAction getUnlockAction(GameContext ctx){
    return getAction(ctx, (Void) -> maybeGetUnlockAction());
  }

  private @Null PlayerAction getAction(GameContext ctx, Function<Void, PlayerAction> func){
    if (owner.isEmpty()) return null;

    PlayerAction action = func.apply(null);
    if (action == null || !action.isValid(ctx)){
      return null;
    }

    return action;
  }

  /**
   * Get a player action that would lock this locking method
   *
   * This can be null if the action is not valid, or if there is no such
   * action for this lock
   */
  public @Null PlayerAction getLockAction(GameContext ctx){
    return getAction(ctx, (Void) -> maybeGetUnlockAction());
  }

  abstract protected @Null PlayerAction maybeGetLockAction();
  abstract protected @Null PlayerAction maybeGetUnlockAction();

  abstract public LockingMethodType getType();

  public void onAttach(Item item){
    this.owner = Optional.of(item);
    if (item.hasProperty(PropertyType.Barrier)){
      item.setBlocksPlayer(true);
      isBarrier = true;
    }
  }
  public void onDetatch(){
    this.owner = Optional.empty();
  }

  @Override
  public MenuEntry getDisplay(Menu parent){
    return new MenuEntryBuilder(parent, getName()).build();
  }

  @Override
  public void write(Json json) {
    json.writeValue("locked", isLocked);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    isLocked  = jsonData.getBoolean("locked", true);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LockingMethod){
      return this.getType() == LockingMethod.class.cast(obj).getType();
    }
    return false;
  }

  abstract protected LockingMethod getEmptyMethod();

  protected LockingMethod clone(Item newOwner) {
    LockingMethod p = this.getEmptyMethod();
    p.owner = Optional.of(newOwner);
    p.isLocked = this.isLocked;
    p.isBarrier = this.isBarrier;
    p.read(new Json(), new JsonReader().parse(new Json().toJson(this)));
    return p;
  }
}
