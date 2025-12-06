package group24.escaperoom.ui.editorTools;

import java.util.Optional;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.data.Types.IntVector2;
import group24.escaperoom.entities.Item;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.screens.editor.ActionHistory.EditorAction;
import group24.escaperoom.screens.editor.ToolManager.ToolType;
import group24.escaperoom.ui.editorTools.SelectionTool.Selection;

public class DeletionTool extends EditorTool {
  IntVector2 touchDown = new IntVector2(-1,-1);
  boolean deleting;

  public DeletionTool(LevelEditorScreen editor) {
    super(editor);
  }
  //----------------------------------------------------------------------------
  // Public API
  //----------------------------------------------------------------------------
  
  /**
   * Whether this tool is currently "deleting"
   *
   * That is, the tool has been used to delete a single item 
   * and the user has not yet lifted the mouse button
   */
  public boolean isDeleting(){
    return deleting;
  }
  
  public void handleDrag(float x, float y){
     if (touchDown.equals((int)x, (int)y)) return;

     touchDown.set(x, y);
     deleteAt(x, y);
  }

  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void select() {
    CursorManager.setCursor(CursorType.Delete);
    editor.addListener(deleteListener);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(deleteListener);
  }

  //----------------------------------------------------------------------------
  // Deletion logic
  //----------------------------------------------------------------------------

  private boolean deleteAt(float x, float y) {
    // Maybe delete an entire selection
    Optional<Selection> selection = editor.getSelectionTool().getSelection();
    if (selection.isPresent() && selection.get().getArea().contains(new Vector2(x, y))) {
      editor.getSelectionTool().deleteSelected();
      return true;
    }

    // Delete the priority item on this square
    Optional<Item> oTarget = editor.priorityItemAt((int) x, (int) y);
    if (oTarget.isPresent()) {
      oTarget.get().remove();
      editor.recordEditorAction(new Deletion(editor, oTarget.get()));
      deleting = true;
      return true;
    }
    return false;
  }

  //----------------------------------------------------------------------------
  // Input handling
  //----------------------------------------------------------------------------

  private boolean handleTouchDown(float x, float y){
      touchDown.set(x,y);
      deleteAt(x, y);
      return true;
  }

  /**
   * Listener on the stage for deletion tool
   *
   */
  private InputListener deleteListener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      return handleTouchDown(x, y);
    }
    public void touchDragged (InputEvent event, float x, float y, int pointer) {
      handleDrag(x, y);
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      deleting = false;
    };
  };



  /**
   * Represents a deletion operation
   */
  public static class Deletion implements EditorAction {
    Array<Item> objs;
    LevelEditorScreen editor;

    public Deletion(LevelEditorScreen editor, Item... objs) {
      this.objs = Array.with(objs);
      this.editor = editor;
    }

    @Override
    public void doAction() {
      objs.forEach((o) -> o.remove());
    }

    @Override
    public void undoAction() {
      objs.forEach((o) -> editor.placeItem(o));
    }
  }

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------

  @Override
  public String getButtonStyle() {
    return "deleteButton";
  }

  @Override
  public ToolType getType(){
    return ToolType.Deletion;
  }

  @Override
  public String getName() {
    return "Delete";
  }
}
