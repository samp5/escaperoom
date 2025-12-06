package group24.escaperoom.ui.editor;

import group24.escaperoom.entities.properties.ItemProperty;
import group24.escaperoom.entities.properties.ItemPropertyValue;
import group24.escaperoom.screens.ItemEditor;
import group24.escaperoom.ui.SmallLabel;
import group24.escaperoom.ui.editor.ConfigurationMenu.HandlesMenuClose;

public class PropertyConfigurationMenu extends Menu {
  private class SpecialLittleTinyBabyLabel extends SmallLabel implements HandlesMenuClose {
    SpecialLittleTinyBabyLabel(String content){
      super(content, "default", 0.65f);
      setWrap(true);
      setWidth(150);
    }

    @Override
    public void handle() {
    }
  }

  public PropertyConfigurationMenu(ItemProperty<? extends ItemPropertyValue> property){
    super(null, property.getDescription().name, ItemEditor.screen);

    add(new SpecialLittleTinyBabyLabel(property.getDescription().shortDesc)).row();
    add(MenuEntry.divider()).row();

    property.getCustomItemConfigurationMenu().ifPresent((config) -> {
      add(new MenuEntryBuilder(this, "Configure")
        .spawns((parent) -> {
          ItemEditor.get().markModified();
          return new ConfigurationMenu<>(
            parent, 
            config,
            property.getDescription().name + " Configuration", 
            screen);
        })
        .build())
      .row();
    });

    add(new MenuEntryBuilder(this, "Help")
      .spawns((parent) -> {
        return new ConfigurationMenu<>(
          parent, 
          new SpecialLittleTinyBabyLabel(property.getDescription().longDesc),
          property.getDescription().name + " Details", 
          screen);
      })
      .build())
    .row();

    if (!property.getDescription().mutallyExclusiveWith.isEmpty()){
      add(new MenuEntryBuilder(this, "Conflicting Properties ")
        .spawns((parent) -> {

          ConfigurationMenu.VGroup conflicts = new ConfigurationMenu.VGroup();
          property.getDescription().mutallyExclusiveWith.forEach((p) -> {
            if (p != property.getType()){
              conflicts.addActor(new SmallLabel(p.getEmptyProperty().getDescription().name, "bubble", 0.65f));
            }
          });

          return new ConfigurationMenu<>(
            parent, 
            conflicts,
            property.getDescription().name + " Conflicts", 
            screen);
        })
        .build())
      .row();
    }
    pack();
  }
}
