package group24.escaperoom.entities.objects;

import java.io.Serializable;
import java.util.HashMap;

import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.entities.Item;
import group24.escaperoom.entities.properties.PropertyType;
import group24.escaperoom.utils.Types.Size;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

public class ObjectTypeData implements Serializable, Json.Serializable {

  public String name, category;
  public Size size;
  public String texture;
  public HashMap<PropertyType, JsonValue> propertyParameters = new HashMap<>();
  public int renderPriority;

  public ObjectTypeData(String name, String category, Size size, String texture, int renderPriority,
      HashMap<PropertyType, JsonValue> propertyParameters) {
    init(name, category, size, texture, renderPriority, propertyParameters);
  }

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public ObjectTypeData(){}

  private void init(String name, String category, Size size, String texture, int renderPriority,
      HashMap<PropertyType, JsonValue> propertyParameters){
    this.name = name;
    this.category = category;
    this.size = size;
    this.renderPriority = renderPriority;
    this.texture = texture;
    if (propertyParameters != null) this.propertyParameters.putAll(propertyParameters);
  }

  public ObjectTypeData copy(){
    return new ObjectTypeData(
      new String(name),
      new String(category),
      size.copy(),
      new String(texture),
      renderPriority,
      propertyParameters == null ? new HashMap<>() : new HashMap<>(propertyParameters)
    );
  }

  @Override
  public String toString() {
      return this.name;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ObjectTypeData)) {
      return false;
    }

    ObjectTypeData that = (ObjectTypeData) other;

    if (this.name.equals(that.name)
        && this.size.equals(that.size)
        && this.texture.equals(that.texture)
        && this.renderPriority == that.renderPriority
        && this.propertyParameters.equals(that.propertyParameters)) {
      return true;
    }

    return false;
  }


  @Override
  public void write(Json json) {
    json.writeValue("name", name);
    json.writeObjectStart("size");
    json.writeValue("width", size.width);
    json.writeValue("height", size.height);
    json.writeObjectEnd();

    Item item = new Item(this);

    json.writeArrayStart("properties");
    for (PropertyType type : this.propertyParameters.keySet()){
      json.writeValue(type.asJsonString());
    }
    json.writeArrayEnd();

    json.writeObjectStart("property_values");
    item.getProperties().forEach((prop) -> {
      json.writeValue(prop.getType().asJsonString(), prop);
    });
    json.writeObjectEnd();

    json.writeValue("texture",texture + ".png");
    json.writeValue("render_priority", renderPriority);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    String name = jsonData.getString("name");
    JsonValue sz = jsonData.get("size");
    Size size = new Size(sz.getInt("width"), sz.getInt("height"));

    HashMap<PropertyType, JsonValue> validProperties = new HashMap<>();

    JsonValue properties = jsonData.get("properties");
    JsonValue propertyValues = jsonData.get("property_values");

    // assume we have no prop vals
    properties.forEach((prop) -> {
      validProperties.put(PropertyType.fromString(prop.toString()), new JsonReader().parse("{}"));
    });

    if (propertyValues != null) {
      properties.forEach((prop) -> {
        JsonValue params = propertyValues.get(prop.toString());
        if (params != null) {
          validProperties.put(PropertyType.fromString(prop.toString()), params);
        }
      });
    }

    String texture = jsonData.getString("texture");
    int render_priority = jsonData.getInt("render_priority", 1);
    String texture_id = texture.substring(0, texture.lastIndexOf(".png"));

    init(name, category, size, texture_id, render_priority, validProperties);
  }
}
