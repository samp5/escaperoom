package group24.escaperoom.game.ui;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

import group24.escaperoom.engine.control.CursorManager;
import group24.escaperoom.engine.control.CursorManager.CursorType;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.ui.InteractableItemSlot;
import group24.escaperoom.ui.dnd.ItemPayload;

public class PlayerInventoryItemSlot extends InteractableItemSlot {
  Logger log = Logger.getLogger("PlayerInventoryItemSlot");
  PlayerInventorySource source;
  PlayerInventoryTarget target;

  public PlayerInventoryItemSlot(Player actingPlayer) {
    super(actingPlayer);

    target = new PlayerInventoryTarget(this);
    actingPlayer.getGameScreen()
        .getDragAndDrop()
        .addTarget(target);
  }

  public PlayerInventoryItemSlot(Item item, Player actingPlayer) {
    super(Optional.of(item), actingPlayer);

    source = new PlayerInventorySource(this);
    actingPlayer.getGameScreen()
        .getDragAndDrop()
        .addSource(source);
  }

  public class PlayerInventorySource extends DragAndDrop.Source {
    PlayerInventorySource(PlayerInventoryItemSlot slot) {
      super(slot);
      slot.addListener(CursorManager.hoverHelper(CursorType.Hand));
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      inner.slotValues.get().itemPreview.setVisible(false);
      return new ItemPayload(inner.slotValues.get().item);
    }

    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
      if (target == null || target instanceof PlayerInventoryTarget) {
        inner.slotValues.get().itemPreview.setVisible(true);
      } else {
        removeItemFromSlot();
      }

      CursorManager.restoreDefault();
    }
  }

  @Override 
  public void setItem(Item i){
    super.setItem(i);
    if (source == null){
      source = new PlayerInventorySource(this);
    }
    actingPlayer.getGameScreen().getDragAndDrop().addSource(source);
    actingPlayer.getGameScreen().getDragAndDrop().removeTarget(target);
    actingPlayer.addItemToInventory(i);
  }

  @Override 
  public Item removeItemFromSlot(){
    Item removed = super.removeItemFromSlot();
    actingPlayer.getGameScreen().getDragAndDrop().removeSource(source);
    if (target == null){
      target = new PlayerInventoryTarget(this);
    }
    actingPlayer.getGameScreen().getDragAndDrop().addTarget(target);
    actingPlayer.removeItemFromInventory(removed);
    return removed;
  }

  public class PlayerInventoryTarget extends DragAndDrop.Target {
    public PlayerInventoryTarget(PlayerInventoryItemSlot slot) {
      super(slot);
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
      inner.onHover();
      return true;
    }
		public void reset (Source source, Payload payload) {
      inner.onHoverExit();
		}

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
      if (source instanceof PlayerInventorySource){
        return;
      }
      Item i = ItemPayload.class.cast(payload).getItem();
      setItem(i);
    }
  }

}
