package group24.escaperoom.entities.properties.conditionals;

import group24.escaperoom.data.GameContext;

public class XORConditional extends BinaryConditional {

	@Override
	public boolean evaluate(GameContext ctx) {
    boolean left = this.left.evaluate(ctx);
    boolean right = this.right.evaluate(ctx);
    return (left || right) && !(left && right);
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.XORConditional;
	}

	@Override
	public String getName() {
    return "...xor...";
	}
}

