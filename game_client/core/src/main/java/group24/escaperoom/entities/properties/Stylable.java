package group24.escaperoom.entities.properties;

import java.util.logging.Logger;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.AssetManager;
import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.player.PlayerAction;
import group24.escaperoom.ui.editor.Menu;
import group24.escaperoom.ui.editor.Menu.MenuEntry;
import group24.escaperoom.ui.editor.Menu.MenuEntryBuilder;

public class Stylable extends ItemProperty<group24.escaperoom.entities.properties.Stylable.Style> {
  private Array<Style> availableStyles = new Array<>();
  private Style currentStyle;
  private Logger log = Logger.getLogger(Stylable.class.getName());

  private static final PropertyDescription description = new PropertyDescription(
    "Stylable",
    "Has multiple available textures",
    "Stylable items can have multiple textures. Primarily a convienence to avoid creating multiple items that differ only in their texture.",
    null
  );

  public PropertyDescription getDescription() {
    return description;
  };

  public class Style implements ItemPropertyValue, Json.Serializable {
    private String name, texture;

    /**
     * Empty constructor for {@link Json.Serializable} compatability 
     */
    public Style() {}

    public Style(String name, String texture){
      this.name = name;
      this.texture = texture;
    }

    public String getStyleName(){
      return this.name;
    }

    public void apply(Item to) {
      log.fine("applying texture: " + this.getTexture() + " to item " + to.getItemName() +  " id: " + to.getID());
      AtlasRegion ar = new AtlasRegion(this.getTexture());
      ar.setRegionWidth(to.getTexture().getRegionWidth());
      ar.setRegionHeight(to.getTexture().getRegionHeight());
      to.setTexture(ar);
    }

    @Override
    public void write(Json json) {
      json.writeValue("name", name);
      json.writeValue("texture", texture);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
      this.name = jsonData.getString("name", "");
      this.texture = jsonData.getString("texture", owner.getType().texture);
      int pngInd = this.texture.lastIndexOf(".png");
      if (pngInd != -1){
        this.texture = this.texture.substring(0, pngInd);
      }
    }

    public AtlasRegion getTexture() {
      return AssetManager.instance().getRegion(this.texture);
    }


    @Override
    public MenuEntry getDisplay(Menu parent){
      return new MenuEntryBuilder(parent, getStyleName()).onSelect(() -> {
        apply(owner);
      }).build();
    }
  }

  @Override
  public void write(Json json) {
    if (currentStyle == null) buildDefaultStyle();

    json.writeArrayStart("available");
    availableStyles.forEach((s) -> {
      json.writeObjectStart();
      s.write(json);
      json.writeObjectEnd();
    });
    json.writeArrayEnd();
    json.writeValue("current", currentStyle);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue available = jsonData.get("available");
    available.forEach((s) -> {
      Style style = new Style();
      style.read(json, s);
      availableStyles.add(style);
    });
    this.currentStyle = new Style();
    JsonValue current = jsonData.get("current");
    if (current != null) {
      currentStyle.read(json, current);
    } else {
      currentStyle.name = "Default";
      currentStyle.texture = owner.getType().texture;
    }
    currentStyle.apply(owner);
  }

  @Override
  public String getDisplayName() {
    return "Stylable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Stylable;
  }

  @Override
  public void set(Style value) {
    currentStyle = value;
    currentStyle.apply(owner);
  }


  @Override
  public MenuType getInputType() {
    return MenuType.SelectOne;
  }

  private void buildDefaultStyle(){
    currentStyle = new Style("Default", owner.getType().texture);
    availableStyles.add(currentStyle);
  }

  @Override
  public Style getCurrentValue() {
    if (currentStyle == null) buildDefaultStyle();
    return currentStyle;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  @Override
  public Array<Style> getPotentialValues() {
    return availableStyles;
  }

  @Override
  public Class<Style> getValueClass() {
    return Style.class;
  }
}
