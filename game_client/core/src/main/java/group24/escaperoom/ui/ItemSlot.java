package group24.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;

import group24.escaperoom.AssetManager;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.player.Player;

public class ItemSlot extends Container<group24.escaperoom.ui.ItemSlot.ItemSlotInner> {
  protected ItemSlotInner inner;

  public static class ItemSlotInner extends Stack {
    protected static class ItemSlotValues {
      Item item;
      Image itemPreview;
      boolean selected = false;

      ItemSlotValues(Item item) {
        this.item = item;
        TextureRegionDrawable trd = new TextureRegionDrawable(item.getTexture());
        trd.setMinWidth(16 * item.getWidth());
        trd.setMinHeight(16 * item.getHeight());
        this.itemPreview = new Image(trd, Scaling.fit);
      }
    }

    Image selection = new Image(AssetManager.instance().getRegion("selection_outline"));

    protected Item removeItem(){
        if (ItemSlotInner.this.slotValues.isPresent()) {
          ItemSlotValues iv = slotValues.get();
          Item i = iv.item;
          iv.itemPreview.remove();
          slotValues = Optional.empty();
          return i;
        }
      return null;
    }

    protected void setItem(Item item){
      ItemSlotValues iv = new ItemSlotValues(item);
      this.add(iv.itemPreview);
      new Tooltip.Builder(new SmallLabel(item.getItemName() + " ID " + item.getID(), "bubble_gray", 0.65f)).target(iv.itemPreview, Tooltip.stageHelper(this)).build();
      this.slotValues = Optional.of(iv);
    }

    Optional<ItemSlotValues> slotValues = Optional.empty();

    public ItemSlotInner(Optional<Item> item) {

      Image bkg = new Image(AssetManager.instance().getRegion("empty_container_slot"));
      this.add(bkg);
      this.selection.setTouchable(Touchable.disabled);

      item.ifPresent((i) -> setItem(i));
    }

    public void onHover() {
      this.add(selection);
    }

    public void onHoverExit() {
      this.slotValues.ifPresentOrElse((i) -> {
        if (!i.selected) {
          selection.remove();
        }
      }, () -> selection.remove());
    }

    public void toggleSelectionOutline() {
      this.slotValues.ifPresent((i) -> {
        if (i.selected) {
          selection.remove();
        } else {
          this.add(selection);
        }
        i.selected = !i.selected;
      });
    }

    public boolean isSelected() {
      return slotValues.map(sv -> sv.selected).orElse(false);
    }

    public void onClick(Player actingPlayer, Stage uiStage) {
      slotValues.ifPresent((sv) -> {
        new ActionDialog(sv.item, actingPlayer).show(uiStage);
      });
    }
  }

  public ItemSlot(Item item) {
    this(Optional.of(item));
  }

  public ItemSlot() {
    this(Optional.empty());
  }

  protected ItemSlot(Optional<Item> item) {
    super();
    minSize(30, 30);
    inner = new ItemSlotInner(item);
    setActor(inner);
  }
}
