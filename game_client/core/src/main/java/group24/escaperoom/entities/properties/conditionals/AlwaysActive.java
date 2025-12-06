package group24.escaperoom.entities.properties.conditionals;


import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.data.GameContext;
import group24.escaperoom.screens.LevelEditorScreen;

/**
 * A trivially true {@link Conditional}
 */
public class AlwaysActive extends Conditional{

	@Override
	public void write(Json json) {
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
	}

	@Override
	public boolean evaluate(GameContext ctx) {
    return true;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.AlwaysActive;
	}

	@Override
	public String getName() {
    return "Always Active";
	}

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditorScreen editor){
    return Optional.empty();
  }

}
