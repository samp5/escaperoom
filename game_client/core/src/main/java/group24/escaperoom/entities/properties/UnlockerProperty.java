package group24.escaperoom.entities.properties;

public class UnlockerProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Unlocker",
    "Unlocks locked items",
    "When in Player inventory, Unlocker items unlock Locked items with the key lock type.",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  /**
   * Empty constructor for {@code Json.Serializable} compatability 
   */
  public UnlockerProperty() {}

  @Override
  public String getDisplayName() {
    return "Unlocks Items";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.UnlocksProperty;
  }
}
