package group24.escaperoom.ui.editor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;

import io.github.orioncraftmc.meditate.enums.*;
import dev.lyze.flexbox.FlexBox;
import group24.escaperoom.entities.properties.ItemProperty;
import group24.escaperoom.entities.properties.ItemPropertyValue;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.ItemEditor;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.ui.SmallLabel;

public class PropertyBank extends ScrollPane {
  public static class PropertyPill<V extends ItemPropertyValue, P extends ItemProperty<V>> extends SmallLabel {
    public PropertyPill(P property) {
      super(property.getDescription().name, "bubble", 0.65f);
      addListener(CursorManager.hoverHelper(CursorType.Hand));
      ItemEditor.get().getDragAndDrop().addSource(new DragAndDrop.Source(this) {
        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
          Payload pl = new Payload();
          pl.setObject(property.getType());
          SmallLabel l = new SmallLabel(property.getDescription().name, "bubble", 0.65f);
          l.pack();
          pl.setDragActor(l);

          SmallLabel il = new SmallLabel(property.getDescription().name, "bubble", 0.65f);
          il.setColor(1, 0, 0, 1);
          il.pack();
          pl.setInvalidDragActor(il);

          SmallLabel vl = new SmallLabel(property.getDescription().name, "bubble", 0.65f);
          vl.setColor(0, 1, 0, 1);
          vl.pack();
          pl.setValidDragActor(vl);

          return pl;
        }
    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, DragAndDrop.Target target) {
          CursorManager.restoreDefault();
        }
      });
    }
  }

  FlexBox flexBox;
  public PropertyBank(){
    super(null, AbstractScreen.skin);

    // Init flexbox
    flexBox = new FlexBox();
    flexBox.getRoot()
      .setFlexDirection(YogaFlexDirection.ROW)
      .setWrap(YogaWrap.WRAP)
      .setMargin(YogaEdge.ALL, 20)
      .setMargin(YogaEdge.BOTTOM, 40);
    setActor(flexBox);

    // Add pills
    addPropertyPills();

    // Accept all payloads
    ItemEditor.get().getDragAndDrop().addTarget(
      new DragAndDrop.Target(this) {
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) { return true; }
        public void drop(Source source, Payload payload, float x, float y, int pointer) {}
    });

  }

  @Override
  public void pack() {
      super.pack();
      flexBox.pack();
  }

  private void addPropertyPills(){
    for (PropertyType type : PropertyType.values()){
      if (type == PropertyType.InvalidProperty) continue;
      flexBox.add(new PropertyPill<>(type.getEmptyProperty())).setPadding(YogaEdge.ALL, 10);
    }
  }
}
