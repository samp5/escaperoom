package group24.escaperoom.entities.properties.conditionals;

import group24.escaperoom.data.GameContext;

public class OrConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    return left.evaluate(ctx) || right.evaluate(ctx);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.OrConditional;
	}

	@Override
	public String getName() {
    return "...or...";
	}
}
