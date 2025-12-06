package group24.escaperoom.entities.properties.conditionals;

import group24.escaperoom.data.GameContext;

/**
 * A {@link Conditional} which is true only when all of its children are true
 */
public class AllOf extends KaryConditional {
	@Override
	public boolean evaluate(GameContext ctx) {
    for (Conditional c : children){
      if (!c.evaluate(ctx)){
        return false;
      }
    }
    return true;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.AllOf;
	}

	@Override
	public String getName() {
    return "all of...";
	}
}
