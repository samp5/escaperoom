package group24.escaperoom.ui.editorTools;


import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.editor.ToolManager.ToolType;

public abstract class EditorTool {
  LevelEditorScreen editor;

  public EditorTool(LevelEditorScreen stage) {
    this.editor = stage;
  }

  /**
   * Called when this tool is selected
   */
  public abstract void select();

  /**
   * @return the {@link ToolType} 
   */
  public abstract ToolType getType();

  /**
   * Called when the user presses Escape while this is the active tool
   */
  public abstract void cancel();

  public abstract String getName();
  public  String getButtonStyle(){
    return "default";
  };

  public LevelEditorScreen getEditor() {
    return editor;
  }

}
