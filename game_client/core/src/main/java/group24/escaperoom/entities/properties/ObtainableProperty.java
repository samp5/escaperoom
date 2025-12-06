package group24.escaperoom.entities.properties;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.GameEvent;
import group24.escaperoom.data.GameEvent.EventType;
import group24.escaperoom.data.GameEventBus;
import group24.escaperoom.entities.player.PlayerAction;

public class ObtainableProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Obtainable",
    "Can be picked up",
    "Obtainable properties can be obtained by the player",
    new HashSet<>(Set.of(PropertyType.ContainsItemsProperty))
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  private class ObtainAction implements PlayerAction {
    @Override
    public String getActionName() {
      return "Pick up";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      owner.remove(false);

      GameEvent ev = new GameEvent.Builder(EventType.ItemObtained, ctx).source(owner).message("Added " +owner.getItemName() + " to inventory!" ).build();
      GameEventBus.get().post(ev);

      return new ActionResult().setCompletesInteraction(true);
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return !ctx.player.getInventory().contains(owner, false);
    }
  }

  @Override
  public String getDisplayName() {
    return "ObtainableProperty";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Obtainable;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new ObtainAction());
  }
}
