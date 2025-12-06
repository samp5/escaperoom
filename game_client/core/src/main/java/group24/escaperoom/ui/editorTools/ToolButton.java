package group24.escaperoom.ui.editorTools;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.ui.SmallLabel;
import group24.escaperoom.ui.Tooltip;

/**
 * A {@link ToolButton}
 */
public class ToolButton extends ImageButton {
  public ToolButton(EditorTool tool) {
    super(AbstractScreen.skin, tool.getButtonStyle());

    String label = tool.getName();
    if (tool.getType().getHotKey() != null){
      label += "\nHotKey(s): " + String.join("\n",tool.getType().getHotKey().description().bindings);
    }
    SmallLabel l = new SmallLabel( label, "bubble_gray", 0.65f);
    l.pack();
    new Tooltip.Builder(l).target(this, () -> Optional.of(this.getStage())).build();

    addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (isChecked()) {
          tool.getEditor().setActiveTool(tool);
        } else {
          tool.cancel();
          tool.getEditor().deselectTools();
        }
      }
    });
  }
}
