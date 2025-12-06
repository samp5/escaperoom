package group24.escaperoom.entities.properties.conditionals;

import group24.escaperoom.data.GameContext;

/**
 * {@link Conditional} corresponding to logical and.
 */
public class AndConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    return left.evaluate(ctx) && right.evaluate(ctx);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.AndConditional;
	}

	@Override
	public String getName() {
    return "...and...";
	}
}
