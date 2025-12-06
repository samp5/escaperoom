package group24.escaperoom.ui.editorTools;


import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import group24.escaperoom.entities.Item;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.screens.editor.ToolManager.ToolType;

/**
 * Base class for Brushes
 */
public abstract class Brush extends EditorTool {

  /**
   * @param editor on which this brush will operate
   */
  public Brush(LevelEditorScreen editor){
    super(editor);
  }

  @Override
  public void select() {
    CursorManager.setCursor(CursorType.Brush);
    editor.addListener(listener);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(listener);
  }

  private InputListener listener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      return handleTouchDown(x, y);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      handleTouchUp();
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      handleDrag(x, y);
    }
  };


  /**
   * @param x coord in game units where this brush should draw
   * @param y coord in game units where this brush should draw
   * @return the newly drawn item
   */
  abstract public Optional<Item> draw(int x , int y);

  /**
   * @param x touchdown coord
   * @param y touchdown coord
   * @return whether the brush is handling this event
   */
  abstract public boolean handleTouchDown(float x , float y);


  /**
   * Handle a touchup event
   */
  abstract public void handleTouchUp();

  /**
   * @param x drag coord
   * @param y drag coord
   */
  abstract public void handleDrag(float x, float y);

  /**
   * @return item that is drawn with this brush
   */
  abstract public Item getItem();

  /**
   * @return whether this brush is currently drawing
   */
  abstract public boolean isDrawing();

  @Override
  public ToolType getType() {
    return ToolType.Brush;
  }
}
