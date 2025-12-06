package group24.escaperoom.editor.events;

import com.badlogic.gdx.utils.Null;

import group24.escaperoom.game.entities.Item;

public class EditorEvent {

  public enum EventType {
    ItemRemoved,
  }

  public final EventType type;
  public final @Null Item source;

  public EditorEvent(EventType type,  @Null Item source){
    this.type = type;
    this.source = source;
  }

}
