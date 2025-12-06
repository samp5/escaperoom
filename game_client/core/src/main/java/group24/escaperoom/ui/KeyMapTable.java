package group24.escaperoom.ui;
import java.util.Arrays;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import group24.escaperoom.control.ControlsManager;
import group24.escaperoom.control.Input;
import group24.escaperoom.control.ControlsManager.InputGroupMap;
import group24.escaperoom.control.Input.MappingDescription;
import group24.escaperoom.control.MapGroup;

public class KeyMapTable extends Container<ScrollPane> {
  Table inner = new Table();
  public KeyMapTable(){
    InputGroupMap keymaps = ControlsManager.registedMappings();
    MapGroup[] groups = keymaps.keySet().toArray(new MapGroup[0]);
    Arrays.sort(groups);
    inner.defaults().left().expandX();

    for (MapGroup group : groups){
      if (group == MapGroup.DEBUG) continue;
      inner.add(new SmallLabel(group.toString(), "title", 1f)).center().colspan(2).row();
      for (Input input : keymaps.get(group)){
        MappingDescription desc = input.description();
        inner.add(new SmallLabel(desc.desc, "bubble"));
        HorizontalGroup hg = new HorizontalGroup();
        hg.space(5);
        Arrays.stream(desc.bindings).forEach((bind) -> {
          hg.addActor(new SmallLabel(bind, "bubble_gray"));
        });
        inner.add(hg).row();
      }
    }

    ScrollPane scrollPane = new ScrollPane(inner);
    scrollPane.addListener(new InputListener(){
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (getStage() == null) return;
        getStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (getStage() == null) return;
        getStage().setScrollFocus(null);
      }
    });
    setActor(scrollPane);
    maxHeight(400);
  }
}
