package group24.escaperoom.entities.properties;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;

/**
 * A base class for properties which are true or false
 */
public abstract class BooleanProperty
    extends ItemProperty<group24.escaperoom.entities.properties.BooleanProperty.BooleanValue> {

  /**
   * The current value of this property
   */
  public boolean currentValue = false;

  /**
   * A wrapper around a boolean that is also a {@link ItemPropertyValue}
   */
  public static class BooleanValue implements ItemPropertyValue {
    boolean inner = false;

    /**
     * @param inner the inner boolean value
     */
    public BooleanValue(boolean inner) {
      this.inner = inner;
    }

    /**
     * @return whether this value is true
     */
    public boolean isTrue(){
      return inner;
    }

    @Override
    public MenuEntry getDisplay(Menu parent) {
      return null;
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("value", currentValue);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    currentValue = jsonData.getBoolean("value", false);
  }

  @Override
  public Class<BooleanValue> getValueClass() {
    return BooleanValue.class;
  }

  @Override
  public Array<BooleanValue> getPotentialValues() {
    return Array.with(new BooleanValue(true), new BooleanValue(false));
  }

  @Override
  public void set(BooleanValue value) {
    currentValue = value.inner;
  }

  @Override
  public MenuType getInputType() {
    return MenuType.Toggleable;
  }

  @Override
  public BooleanValue getCurrentValue() {
    return new BooleanValue(currentValue);
  }

  @Override
  protected Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  public boolean isTrue() {
    return currentValue;
  }

}
