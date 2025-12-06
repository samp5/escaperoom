package group24.escaperoom.game.entities.properties.locks;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.LockingMethod;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEvent.EventType;
import group24.escaperoom.game.state.GameEventBus;

public class TrivialLock extends LockingMethod {
  protected class TryUnlock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Try to unlock";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      if (ctx.player == null)  return new ActionResult();

      if (isBarrier){
        owner.get().setBlocksPlayer(false);
        owner.get().setAlpha(0.75f);
      }
      isLocked = false;
      GameEventBus.get().post(
        new GameEvent.Builder(EventType.ItemStateChange, ctx)
          .message(owner.get().getItemName() + " was unlocked!")
          .build()
      );
      return new ActionResult();
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked;
    }

  };

	@Override
	public String getName() {
    return "Unlocked";
	}

	@Override
	public Array<PlayerAction> getActions() {
    return Array.with(new TryUnlock());
	}

	@Override
	public LockingMethodType getType() {
    return LockingMethodType.TrivialLock;
	}

  @Override
  protected LockingMethod getEmptyMethod() {
    return new TrivialLock();
  }

  @Override
  protected PlayerAction maybeGetLockAction() {
    return null;
  }

  @Override
  protected PlayerAction maybeGetUnlockAction() {
    return new TryUnlock();
  }

}
