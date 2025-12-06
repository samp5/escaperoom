package group24.escaperoom.entities.properties.conditionals;
import group24.escaperoom.data.GameContext;

public class NotConditional extends UnaryConditional {
  @Override
  public boolean evaluate(GameContext ctx) {
    return !child.evaluate(ctx);
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.NotConditional;
  }

  @Override
  public String getName() {
    return "not...";
  }
}
