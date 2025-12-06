package group24.escaperoom.control;

import java.util.Set;

import group24.escaperoom.control.ControlsManager.InputType;

public interface InputOverride {

  /**
   * Attempt to handle the given input.
   *
   * @return whether or not the input should be consumed
   */
  boolean handleInput(Input input, InputType type);

  /**
   *
   * {@link Input}s that this override cares about
   */
  Set<Input> getOverriddenInputs();
}
