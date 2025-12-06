package group24.escaperoom.editor.item.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;

import dev.lyze.flexbox.FlexBox;
import group24.escaperoom.engine.control.CursorManager;
import group24.escaperoom.engine.control.CursorManager.CursorType;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.screens.ItemEditor;
import group24.escaperoom.ui.widgets.G24Label;
import io.github.orioncraftmc.meditate.enums.YogaEdge;
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection;
import io.github.orioncraftmc.meditate.enums.YogaWrap;

public class PropertyBank extends ScrollPane {
  public static class PropertyPill<V extends ItemPropertyValue, P extends ItemProperty<V>> extends G24Label {
    public PropertyPill(P property) {
      super(property.getDescription().name, "bubble", 0.65f);
      addListener(CursorManager.hoverHelper(CursorType.Hand));
      ItemEditor.get().getDragAndDrop().addSource(new DragAndDrop.Source(this) {
        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
          Payload pl = new Payload();
          pl.setObject(property.getType());
          G24Label l = new G24Label(property.getDescription().name, "bubble", 0.65f);
          l.pack();
          pl.setDragActor(l);

          G24Label il = new G24Label(property.getDescription().name, "bubble", 0.65f);
          il.setColor(1, 0, 0, 1);
          il.pack();
          pl.setInvalidDragActor(il);

          G24Label vl = new G24Label(property.getDescription().name, "bubble", 0.65f);
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
