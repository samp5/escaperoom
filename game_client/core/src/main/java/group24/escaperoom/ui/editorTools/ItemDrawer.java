package group24.escaperoom.ui.editorTools;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

import java.util.function.Predicate;

import group24.escaperoom.AssetManager;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.screens.LevelEditorScreen;
import group24.escaperoom.ui.ItemSlot;
import group24.escaperoom.ui.SmallLabel;

public class ItemDrawer extends Tree<group24.escaperoom.ui.editorTools.ItemDrawer.ItemCategory, Item> {

  LevelEditorScreen editor;
  Array<Item> prototypes = new Array<>();

  private static class ItemDrawerEntry extends Table {
    public ItemDrawerEntry(String categoryName) {
      add(new SmallLabel(categoryName));
    }
    public ItemDrawerEntry(Item item) {
      padLeft(0);
      defaults().pad(5).left();
      ItemSlot s  = new ItemSlot(item);
      s.setTouchable(Touchable.disabled);
      add(s);
      SmallLabel label = new SmallLabel(item.getItemName());
      label.setTouchable(Touchable.disabled);
      add(label).minWidth(100);

      if (item.hasProperty(PropertyType.Brushable) || item.hasProperty(PropertyType.TiledBrushable)) {
        Image brushImg = new Image(AssetManager.instance().getRegion("brush"));
        brushImg.setScaling(Scaling.contain);
        add(brushImg).height(25).width(25);
      }
      setTouchable(Touchable.enabled);  
    }

  }

  public class ItemCategory extends Tree.Node<ItemNode, Item, ItemDrawerEntry> {
    Optional<Brush> brush = Optional.empty();
    protected ItemCategory(ItemDrawerEntry entry) {
      super(entry);
      setSelectable(false);
    }

    public ItemCategory(String categoryName) {
      this(new ItemDrawerEntry(categoryName));
    }

    public Optional<Brush> getBrush(){
      return brush;
    }


  }

  public Array<Item> getItemPrototypes(){
    return prototypes;
  }

  public class ItemNode extends ItemCategory {
    public ItemNode(Item item) {
      this(item, false);
    }

    public ItemNode(Item item, boolean selectable) {
      super(new ItemDrawerEntry(item));
      prototypes.add(item);
      setValue(item);
      makeDraggable(this);
      setSelectable(selectable);

      if(item.hasProperty(PropertyType.Brushable)){
        setSelectable(true);
        brush = Optional.of(new ItemBrush(editor, item));
      } else if (item.hasProperty(PropertyType.TiledBrushable)){
        setSelectable(true);
        brush = Optional.of(new TiledBrush(editor, item));
      }
    }
  }

  /**
   * Convience method to configure an object node as a drag source
   */
  private void makeDraggable(ItemNode node) {
    node.getActor().addListener(CursorManager.hoverHelper(CursorType.Hand));
    editor.getDragAndDrop().addSource(new DragAndDrop.Source(node.getActor()) {
      @Override
      public Payload dragStart(InputEvent event, float x, float y, int pointer) {

        CursorManager.setCursor(CursorType.Hand);
        if (node.getValue().hasProperty(PropertyType.Unique)) {
          Predicate<Item> pred = new Predicate<Item>() {
            public boolean test(Item arg0) {
              return arg0.hasProperty(PropertyType.Unique)
                  && arg0.getType().name.equals(node.getValue().getType().name);
            }
          };
          if (editor.containsItemWhere(pred)) {
            return null;
          }
        }

        getSelection().clear();
        Payload p = new DragAndDrop.Payload();
        p.setObject(node.getValue());
        SmallLabel l = new SmallLabel(Item.class.cast(node.getValue()).getItemName(), "default", 0.65f);
        p.setDragActor(l);
        return p;
      }

      public void dragStop (InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
        CursorManager.restoreDefault();
      }
    });
  }

  public ItemDrawer(LevelEditorScreen editor) {
    super(LevelEditorScreen.skin);
    setIndentSpacing(0);
    setPadding(5, 5);
    this.editor = editor;
  }
}
