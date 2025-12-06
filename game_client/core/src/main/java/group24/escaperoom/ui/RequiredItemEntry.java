package group24.escaperoom.ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.entities.Item;
import group24.escaperoom.screens.AbstractScreen;

public class RequiredItemEntry {
  public enum ItemRequired {
    Yes,
    Forbidden,
    Either;

    public boolean matches(boolean isRequired) {
      switch (this) {
        case Either:
          return true;
        case Forbidden:
          return !isRequired;
        case Yes:
          return isRequired;
      }
      return false;
    }
  }

  public static class RequiredItem {
    ItemRequired required;
    Item item;
    public RequiredItem(Item item, ItemRequired required) {
      this.item = item;
      this.required = required;
    }
    public Item getItem(){
      return item;
    }
    public void setRequired(ItemRequired required){
      this.required = required;
    }
    public ItemRequired getRequired(){
      return required;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof RequiredItem){
        return RequiredItem.class.cast(obj).item.getID() == this.item.getID();
      }
      return false;
    }
  }

  public interface RequiredItemChanged {
    public void onChange(RequiredItem item);
  }
  private RequiredItemChanged callBack = (i) -> {};
  private ButtonGroup<RequireButton> buttonGroup;
  private Item item;
  public final ItemSlot slot;
  public final SmallLabel label;
  public final RequireButton checkBtn;
  public final RequireButton forbidBtn;

  public static RequiredItemChanged onChangeHelper(Array<RequiredItem> mutArr, HashMap<Integer, RequiredItem> mutSelected) {
    return (RequiredItem item) -> {
      // if the property doesn't care about the state of this item, remove it from the item list
      if (item.getRequired() == ItemRequired.Either){
        mutArr.removeValue(item, false);
        mutSelected.remove(item.item.getID());
      }

      boolean found = false;
      // look for this toggle item
      for (RequiredItem ti : mutArr) {
        // nice, update the required state
        if (ti.getItem().getID() == item.getItem().getID()) {
          ti.setRequired(item.getRequired());
          found = true;
        }
      }

      // didn't find it, must be a new requirement
      if (!found){
        mutArr.add(item);
        mutSelected.put(item.item.getID(), item);
      }
    };
  }

  public void setOnChanged(RequiredItemChanged callback){
    callBack = callback;
  }

  private class RequireButton extends ImageButton {
    ItemRequired req;
    private ChangeListener buttonListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        updateProperty();
      }
    };
    public RequireButton(String style, ItemRequired setsRequired) {
      super(AbstractScreen.skin, style);
      this.req = setsRequired;
      this.addListener(buttonListener);
      setProgrammaticChangeEvents(false);
    }
  }

  public RequiredItemEntry(Item item, ItemRequired required) {
    this.item = item;
    slot = new ItemSlot(item);
    label = new SmallLabel(item.getItemName());
    buttonGroup = new ButtonGroup<>();
    buttonGroup.setMaxCheckCount(1);
    buttonGroup.setMinCheckCount(0);

    checkBtn = new RequireButton("toggleRequired", ItemRequired.Yes);
    forbidBtn = new RequireButton("toggleForbidden", ItemRequired.Forbidden);

    checkBtn.setChecked(required == ItemRequired.Yes);
    forbidBtn.setChecked(required == ItemRequired.Forbidden);

    buttonGroup.add(checkBtn, forbidBtn);
  }

  // Called anytime that any button changes
  private void updateProperty() {
    RequiredItem ti = new RequiredItem(item, ItemRequired.Either);
    RequireButton tb = buttonGroup.getChecked();
    if (tb != null){
      ti.setRequired(tb.req);
    }
    callBack.onChange(ti);
  }
}
