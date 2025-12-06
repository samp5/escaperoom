package group24.escaperoom.game.entities.properties;

import group24.escaperoom.game.entities.properties.base.ConditionalProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;

public class ConditionallyActive extends ConditionalProperty {
  private static final PropertyDescription description = new PropertyDescription(
    "Condtionally Active",
    "Provides actions only under some condition",
    "Condtionally active items only provide the player with actions when their conditon is met",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Active When...";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ConditionallyActive;
  }
}
