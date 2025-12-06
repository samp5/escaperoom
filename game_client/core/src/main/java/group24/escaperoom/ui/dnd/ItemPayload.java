package group24.escaperoom.ui.dnd;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import group24.escaperoom.entities.Item;
import group24.escaperoom.ui.SmallLabel;

public class ItemPayload extends DragAndDrop.Payload{
  public ItemPayload(Item i){
    setDragActor(new SmallLabel(i.getItemName(), "bubble",0.65f));
    setObject(i);
  }

  public Item getItem(){
    return Item.class.cast(getObject());
  }
}
