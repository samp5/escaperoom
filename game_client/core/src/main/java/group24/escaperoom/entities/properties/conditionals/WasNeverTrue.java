package group24.escaperoom.entities.properties.conditionals;

import group24.escaperoom.data.GameContext;

public class WasNeverTrue extends UnaryConditional {

  public boolean wasTrue = false;

	@Override
	public boolean evaluate(GameContext ctx) {
    return !wasTrue;
	}

  @Override 
  public boolean requiresPoll(){
    return true;
  }

  @Override
  public boolean poll(GameContext ctx) {
    if (wasTrue){
      return false;
    }

    child.poll(ctx);

    if (child.evaluate(ctx)){
      wasTrue = true;
      return false;
    }
    return true;
  }

	@Override
	public ConditionalType getType() {
    return ConditionalType.WasNeverTrue;
	}

	@Override
	public String getName() {
    return "was never true...";
	}

}
