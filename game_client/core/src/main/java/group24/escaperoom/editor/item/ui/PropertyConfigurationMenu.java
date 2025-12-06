package group24.escaperoom.editor.item.ui;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.screens.ItemEditor;
import group24.escaperoom.ui.widgets.G24Label;

public class PropertyConfigurationMenu extends Menu {
  private class SpecialLittleTinyBabyLabel extends G24Label implements HandlesMenuClose {
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
              conflicts.addActor(new G24Label(p.getEmptyProperty().getDescription().name, "bubble", 0.65f));
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
