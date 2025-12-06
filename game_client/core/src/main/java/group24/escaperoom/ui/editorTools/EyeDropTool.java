package group24.escaperoom.ui.editorTools;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import group24.escaperoom.control.ControlsManager;
import group24.escaperoom.control.ControlsManager.InputType;
import group24.escaperoom.control.Input;
import group24.escaperoom.control.InputOverride;
import group24.escaperoom.data.Types.IntVector2;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.screens.BackManager;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.screens.editor.DragManager.PlacementAction;
import group24.escaperoom.screens.editor.ToolManager.ToolType;
import group24.escaperoom.utils.Notifier;

public class EyeDropTool extends EditorTool {

  private enum State {
    Idle,       // active, but no item copied
    Previewing, // item copied, ghost item following cursor
    Placing     // actively brushing/dragging
  }

  private State state = State.Idle;

  // immutable template
  private Item template = null;

  // ghost preview item (position updates with mouse)
  private Item preview = null;

  // could be itembrush or tiledbrush depending on item
  private ItemBrush brush;

  public EyeDropTool(LevelEditorScreen editor) {
    super(editor);
  }


  //----------------------------------------------------------------------------
  // Public API
  //----------------------------------------------------------------------------

  public void copyItem(Item source) {
    destroyPreview();

    template = source.copy();
    createPreviewAt(source.getPosition());

    Notifier.info("Copied " + template.getItemName());

    state = State.Previewing;
    CursorManager.setCursor(CursorType.CopyPaste);

    BackManager.addBack(() -> {
      cancel();
      return false;
    });
  }

  public void pasteAt(Vector2 at) {
    if (state == State.Idle) return;
    attemptPlacement(at.x, at.y);
    state = State.Previewing;
  }

  //----------------------------------------------------------------------------
  // Preview creation / destruction
  //----------------------------------------------------------------------------

  private void createPreviewAt(IntVector2 pos) {
    preview = template.clone(false);
    preview.setAlpha(0.5f);
    preview.setPosition(pos);
    editor.placeItem(preview);

    if (preview.hasProperty(PropertyType.TiledBrushable)){
      brush = new TiledBrush(editor, preview);
    } else {
      brush = new ItemBrush(editor, preview);
    }

    brush.setItem(preview);
  }

  private void destroyPreview() {
    if (preview != null)
      preview.remove();
    preview = null;
    template = null;
    state = State.Idle;
  }

  //----------------------------------------------------------------------------
  // Input handling
  //----------------------------------------------------------------------------
  
  private final static HashSet<Input> overridenInputs = new HashSet<>(Set.of(
    Input.ROTCW,
    Input.ROTCCW,
    Input.MIRROR_V,
    Input.MIRROR_H
  ));

  InputOverride inputOverride = new InputOverride() {
    @Override
    public boolean handleInput(Input input, InputType type) {
      if (type != InputType.PRESSED) return false;

      if (state != State.Previewing) return false;

      switch (input){
        case ROTCW:
          preview.rotateBy(90);
          return true;
        case ROTCCW:
          preview.rotateBy(-90);
          return true;
        case MIRROR_H:
          preview.mirrorHorizontal();
          return true;
        case MIRROR_V:
          preview.mirrorVertical();
          return true;
        default: 
          return false;
      }
    }

    @Override
    public Set<Input> getOverriddenInputs() {
        return overridenInputs;
    }
  };

  private final InputListener listener = new InputListener() {

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
      if (state != State.Previewing || preview == null)
        return false;

      IntVector2 pos = new IntVector2(x, y);

      if (editor.canPlace(preview, pos)) {
        CursorManager.setCursor(CursorType.CopyPaste);
        preview.moveTo(pos.x, pos.y);
        preview.setColor(1, 1, 1, 0.5f);
      } else {
        CursorManager.setCursor(CursorType.InvalidCopyPaste);
        preview.setColor(1, 0.5f, 0.5f, 0.5f);
      }
      return false;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      switch (state) {
        case Idle: {
          if (button != Buttons.LEFT) return false;

          Optional<Item> target = editor.priorityItemAt((int) x, (int) y);
          if (target.isEmpty()) return false;

          copyItem(target.get());
          return true;
        }

        case Previewing: {
          if (button == Buttons.LEFT) {
            return attemptPlacement(x, y);
          }
          if (button == Buttons.RIGHT) {
            // recopy at cursor
            Optional<Item> t = editor.priorityItemAt((int) x, (int) y);
            if (t.isPresent())
              copyItem(t.get());
            return true;
          }
          return false;
        }

        case Placing:
          return false;
      }
      return false;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      if (state != State.Placing)
        return;
      brush.handleDrag(x, y);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      if (state == State.Placing) {
        brush.handleTouchUp();
        state = State.Previewing;
      }
    }
  };

  //---------------------------------------------------------------------------
  // Placement logic
  //---------------------------------------------------------------------------

  private boolean attemptPlacement(float x, float y) {
    if (preview == null)
      return false;

    IntVector2 pos = new IntVector2(x, y);

    if (!editor.canPlace(preview, pos)) {
      Notifier.warn("Invalid placement for " + template.getItemName());
      return false;
    }

    Item placed = preview.clone(false);
    placed.setPosition(pos);
    editor.placeItem(placed);
    editor.recordEditorAction(new PlacementAction(placed, editor));

    // maybe start brushing
    brush.handleTouchDown(x, y);
    state = State.Placing;

    return true;
  }

  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void select() {
    CursorManager.setCursor(CursorType.EyeDrop);
    editor.addListener(listener);
    ControlsManager.pushOverride(inputOverride);
  }

  @Override
  public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(listener);
    destroyPreview();
    ControlsManager.popOverride(inputOverride);
  }

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------

  @Override
  public ToolType getType() {
    return ToolType.EyeDrop;
  }

  @Override
  public String getName() {
    return "Eye Dropper";
  }

  @Override
  public String getButtonStyle() {
    return "eyedropper";
  }
}
