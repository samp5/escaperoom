package group24.escaperoom.entities.properties;


public class Unique extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Unique",
    "Only one allowed on the map",
    "Unique items only have a single instance allowed on the map",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

	@Override
	public String getDisplayName() {
    return "Unique";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Unique;
	}
}
