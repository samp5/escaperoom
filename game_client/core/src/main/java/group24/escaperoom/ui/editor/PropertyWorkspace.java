package group24.escaperoom.ui.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

import dev.lyze.flexbox.FlexBox;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.ItemProperty;
import group24.escaperoom.entities.properties.ItemPropertyValue;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.screens.CursorManager;
import group24.escaperoom.screens.ItemEditor;
import group24.escaperoom.screens.CursorManager.CursorType;
import group24.escaperoom.ui.SmallLabel;
import io.github.orioncraftmc.meditate.YogaNode;
import io.github.orioncraftmc.meditate.enums.YogaEdge;
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection;
import io.github.orioncraftmc.meditate.enums.YogaWrap;

public class PropertyWorkspace extends ScrollPane {
  private HashMap<PropertyType, YogaNode> nodes = new HashMap<>();
  private HashSet<PropertyPill<?,?>> pills = new HashSet<>();
  private FlexBox flexBox;

  public class PropertyPill<V extends ItemPropertyValue, P extends ItemProperty<V>> extends SmallLabel {

    P property;

    public P getProperty(){
      return property;
    }

    public PropertyPill(P property) {
      super(property.getDescription().name, "bubble", 0.65f);
      addListener(CursorManager.hoverHelper(CursorType.Hand));
      this.property = property;

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

          PropertyPill.this.setVisible(true);

          return pl;
        }
        public void dragStop (InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
          if (target != null){
            ItemEditor.get().getDragAndDrop().removeSource(this);
            ItemEditor.get().getNewItem().removeProperty(property.getType());
            PropertyPill.this.remove();
            flexBox.remove(nodes.get(property.getType()));
            nodes.remove(property.getType());
            ItemEditor.get().markModified();
          } else {
            PropertyPill.this.setVisible(true);
          }
        }
      });
    }
  }

  private void addPill(ItemProperty<?> property){
      PropertyPill<?,?> pill = new PropertyPill<>(property);
      YogaNode node = flexBox.add(pill).setPadding(YogaEdge.ALL, 10);

      nodes.put(property.getType(), node);
      pills.add(pill);
      flexBox.pack();
      ItemEditor.get().repack();
  }  

  public Set<PropertyPill<?,?>> getPills(){
    return pills;
  }

  public void populateFor(Item item){
    for (PropertyType type : nodes.keySet()){
      flexBox.remove(nodes.get(type));
    }
    flexBox.clear();
    flexBox.pack();

    nodes.clear();

    ItemEditor.get().repack();

    for (ItemProperty<?> prop : item.getProperties()){
      addPill(prop);
    }
  }

  public PropertyWorkspace() {
    super(null, AbstractScreen.skin);
    flexBox = new FlexBox();
    setActor(flexBox);
    flexBox.getRoot()
      .setFlexDirection(YogaFlexDirection.ROW)
      .setWrap(YogaWrap.WRAP);


    ItemEditor.get().getDragAndDrop().addTarget(new DragAndDrop.Target(this) {

      @Override
      public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        PropertyType type = (PropertyType) payload.getObject();
        if (ItemEditor.get().getNewItem().hasProperty(type) || 
            ItemEditor.get().getNewItem().getProperties().stream().anyMatch(
              (prop) -> { 
                return prop.getDescription().mutallyExclusiveWith.contains(type); 
              }
            )
        ) {
          return false;
        }
        return true;
      }

      @Override
      public void drop(Source source, Payload payload, float x, float y, int pointer) {
        PropertyType type = (PropertyType) payload.getObject();
        ItemProperty<? extends ItemPropertyValue> prop = type.getEmptyProperty();
        ItemEditor.get().getNewItem().addProperty(prop);
        prop.apply(ItemEditor.get().getNewItem());
        addPill(prop);
        ItemEditor.get().markModified();
      }
    });

  }
}
