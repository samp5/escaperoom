package group24.escaperoom.entities.properties;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.entities.properties.conditionals.ConditionalProperty;
import group24.escaperoom.screens.SinglePlayerGameScreen;

public class ConditionallyVisible extends ConditionalProperty {
  private static final PropertyDescription description = new PropertyDescription(
    "Conditionally Visible",
    "Is visible only under some condition",
    "Conditionally visible items only appear on the map when some condition is met",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Visible When...";
  }

  @Override
  public boolean requiresPoll(){
    return true;
  }

  public boolean poll(GameContext ctx) {
    condition.poll(ctx);

    boolean valid = isValid(ctx);

    // handle if the player is conditionally visible
    if (owner.hasProperty(PropertyType.Player) && owner.map instanceof SinglePlayerGameScreen) {
      SinglePlayerGameScreen game = (SinglePlayerGameScreen) owner.map;

      if (valid && owner.id != game.playerId) {  // TODO: mostly works, but not if multiple players are visible.
        ctx.map.placeItem(owner);
        game.loadPlayer();
        return false;
      } else {
        owner.remove(false);
        return true;
      }
    }

    if (valid && !owner.isContained()){
      ctx.map.placeItem(owner);
      return false;
    } else {
      owner.remove(false);
      return true;
    }
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ConditionallyVisible;
  }
}
