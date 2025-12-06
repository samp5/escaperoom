package group24.escaperoom.control.bindings;

import group24.escaperoom.control.ControlsManager.InputPair;
import group24.escaperoom.control.ControlsManager.InputMethod;
import group24.escaperoom.control.InputModifier;

public abstract class InputBinding {
  protected int key;
  protected InputModifier[] modifiers;
  protected InputMethod method;


  public abstract boolean wasPressed();
  public abstract boolean isDown();

  public InputPair getPair() {
    return new InputPair(key, method);
  }

  public InputModifier[] getModifiers() {
    return modifiers;
  }

  public int numModifiers() {
    return modifiers.length;
  }

  public String modifierString(){
    String ret = "";
    for (InputModifier mod : modifiers){
      ret += mod.toString() + "-";
    }
    return ret;
  }
}
