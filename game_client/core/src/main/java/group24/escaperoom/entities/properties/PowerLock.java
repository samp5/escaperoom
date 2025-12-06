package group24.escaperoom.entities.properties;


import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.data.GameEvent;
import group24.escaperoom.data.GameEvent.EventType;
import group24.escaperoom.data.GameEventBus;
import group24.escaperoom.entities.player.PlayerAction;

public class PowerLock extends LockingMethod {

  protected class TryUnlock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Try to Unlock";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      if (ctx.player == null) {
        return ActionResult.DEFAULT;
      }

      owner.get().getProperty(PropertyType.ConnectorSink, ConnectorSink.class).ifPresent((csp) -> {
        String eventMessage;
        if (csp.isConnected()) {
          isLocked = false;
          if (isBarrier) {
            owner.get().setBlocksPlayer(false);
            owner.get().setAlpha(0.5f);
          }
          eventMessage = owner.get().getItemName() + " is now unlocked!";
        } else {
          eventMessage = owner.get().getItemName() + " is still locked!";
        }

        GameEvent ev = new GameEvent.Builder(EventType.ItemStateChange, ctx)
                                    .message(eventMessage)
                                    .build();
        GameEventBus.get().post(ev);
      });

      return ActionResult.DEFAULT;
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked();
    }
  };

  @Override
  public String getName() {
    return "Power Lock";
  }

  @Override
  public Array<PlayerAction> getActions() {
    return Array.with(new TryUnlock());
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.PowerLock;
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new PowerLock();
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
