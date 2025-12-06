package group24.escaperoom.entities.properties;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.entities.player.PlayerAction;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * A {@code PhantomProperty} is to be used as a marker object to represent simple object behavior.
 *
 * e.g. {@link ContainableProperty} is a PhantomProperty in that an object is able to be placed in a container
 *
 * Extend this class to create marker objects
 *
 * - These properties will not show up in the {@link group24.escaperoom.screens.LevelEditorScreen}
 */
public abstract class PhantomProperty extends ItemProperty<group24.escaperoom.entities.properties.PhantomProperty.PhantomPropertyValue> {
  public static class PhantomPropertyValue implements ItemPropertyValue { }

  @Override
  public Array<PhantomPropertyValue> getPotentialValues() {
    return new Array<>();
  }

  @Override
  public Class<PhantomPropertyValue> getValueClass(){
      return PhantomPropertyValue.class;
  }

  @Override
  public MenuType getInputType() {
    return MenuType.None;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  @Override
  public void write(Json json) { }

  @Override
  public void read(Json json, JsonValue jsonData) { }
}
