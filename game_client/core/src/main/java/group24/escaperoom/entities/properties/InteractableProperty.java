package group24.escaperoom.entities.properties;


import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.ui.ActionDialog;

public class InteractableProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Interactable",
    "Can be interacted with",
    "Interactable items provide actions to the player. These actions are determined by other item properties",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Interactable Property";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Interactable;
  }

  public void interact(GameContext ctx) {
    Array<PlayerAction> actions = owner.getPlayerActions(ctx);
    for (PlayerAction action : actions){
      if (!action.isValid(ctx)) actions.removeValue(action, false);
    }
    if (actions.isEmpty()){
      return;
    } else if (actions.size == 1){
      ctx.player.stats.actionsPerformed += 1;
      actions.first().act(ctx).getDialog().ifPresent((dialog) ->{
        dialog.show(ctx.map.getUIStage());
      });
    } else {
      new ActionDialog(owner, ctx.player).show(ctx.map.getUIStage());
    }
  }
}
