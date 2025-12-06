package group24.escaperoom.ui.editorTools;

import java.util.Optional;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.Poll;
import group24.escaperoom.screens.BackManager;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.screens.editor.ToolManager.ToolType;
import group24.escaperoom.ui.editorTools.DeletionTool.Deletion;

public class ItemSelectionTool extends SelectionTool implements Poll {


  public ItemSelectionTool(Stage uiStage, LevelEditorScreen editor) {
    super(editor);
  }

  /**
   * Get the selected objects held in this tool
   *
   */
  @Override
  public Optional<Selection> getSelection() {
    if (selection == null){
      return Optional.empty();
    }
    if (selection.getItems().isEmpty()){
      return Optional.empty();
    }
    return Optional.of(selection);
  }

  public void setItems(Array<Item> items){
    if (selection == null){
      selection = new Selection(new Rectangle(), new Array<>(), editor);
    }
    selection.setItems(items);
  }


  @Override
  public void startSelection(float x, float y) {
      super.startSelection(x, y);
      editor.addPollable(this);
  }

  @Override
  public void endSelection() {
    selecting = false;
    if (getSelection().isEmpty()){
      editor.removeOverlayDrawable(this);
    } else {
      selection.minimizeArea();
    }

    BackManager.addBack(() -> {
      if (getSelection().isEmpty()){ 
        return false;
      };

      editor.removeOverlayDrawable(this);
      selection.clearSelectedItems();
      selection = null;
      return true;
    });
  }

  public void deleteSelected(){
    getSelection().ifPresent((selection) -> {
      Array<Item> items = selection.getItems();
      Item[] itemsArr = new Item[items.size];
      for (int i = 0; i < items.size; i++) {
        Item item = items.get(i);
        item.remove();
        itemsArr[i] = item;
      }
      editor.recordEditorAction(new Deletion(editor, itemsArr));
      selection.clearSelectedItems();
    });
    editor.removeOverlayDrawable(this);
  }


  private void selectItems() {
    for (Item item : editor.getItemsIn(selection.area)) {
      item.setSelected(true);
      selection.addItem(item);
    }
  }

  @Override
  public String getButtonStyle() {
    return "selectButton";
  }

  @Override
  public ToolType getType() {
    return ToolType.ItemSelect;
  }

  @Override
  public boolean poll(float delta){
    if (selecting){
      selection.clearSelectedItems();
      this.selectItems();
      return true;
    }
    return false;
  }
}
