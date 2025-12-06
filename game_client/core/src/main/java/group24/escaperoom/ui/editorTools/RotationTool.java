package group24.escaperoom.ui.editorTools;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import group24.escaperoom.entities.Item;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.editor.ActionHistory.EditorAction;
import group24.escaperoom.screens.editor.ToolManager.ToolType;

public class RotationTool extends EditorTool {

  public RotationTool(LevelEditorScreen stage) {
    super(stage);
  }

  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(rotateToolListener);
  }


  @Override
  public void select() {
    CursorManager.setCursor(CursorType.Rotate);
    editor.addListener(rotateToolListener);
  }

  //----------------------------------------------------------------------------
  // Input handling
  //----------------------------------------------------------------------------

  private InputListener rotateToolListener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      return editor.priorityItemAt((int)x, (int)y).map((item) -> {

        int rotation = item.getRotation();

        item.rotateBy(90);

        if (item.getRotation() != rotation) {
          editor.recordEditorAction(new RotationAction(item));
        }

        return true;
      }).orElse(false);
    }
  };

  /**
   * Represents a rotation operation
   */
  public static class RotationAction implements EditorAction {
    Item target;

    public RotationAction(Item obj) {
      this.target = obj;
    }

    @Override
    public void doAction() {
      target.rotateBy(90);
    }

    @Override
    public void undoAction() {
      target.rotateBy(-90);
    }
  }

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------

  @Override
  public String getButtonStyle() {
    return "rotateButton";
  }

  @Override
  public ToolType getType() {
    return ToolType.Rotation;
  }

  @Override
  public String getName() {
    return "Rotate";
  }


}
