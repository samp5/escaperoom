package group24.escaperoom.ui.dnd;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import group24.escaperoom.game.entities.Item;
import group24.escaperoom.ui.widgets.G24Label;

public class ItemPayload extends DragAndDrop.Payload{
  public ItemPayload(Item i){
    setDragActor(new G24Label(i.getItemName(), "bubble",0.65f));
    setObject(i);
  }

  public Item getItem(){
    return Item.class.cast(getObject());
  }
}
