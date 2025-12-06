package group24.escaperoom.entities.objects;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.AssetManager;
import group24.escaperoom.utils.FileUtils;
import group24.escaperoom.utils.Notifier;

public class ObjectLoader {
  public static Logger log = Logger.getLogger(ObjectLoader.class.getName());
  public static class LoadedObjects {
    public static class ItemMap extends HashMap<String, HashMap<String, ObjectTypeData>>{}
    // category -> {type name -> type data}
    private static ItemMap itemTypes = new ItemMap();
    private static ItemMap userItems = new ItemMap();

    private static boolean validName(String name){
      return !(name == null || name.isEmpty() || name.isBlank());
    }
    public static Optional<ObjectTypeData> getItem(String category, String name){
      if (!validName(category) || !validName(name)){
        return Optional.empty();
      }

      HashMap<String, ObjectTypeData>  map = userItems.get(category);

      if (map != null){
        ObjectTypeData data = map.get(name);
        if (data != null){
          return Optional.of(data);
        }
      } 

      map = itemTypes.get(category);
      if (map != null){
        ObjectTypeData data = map.get(name);
        if (data != null){
          return Optional.of(data);
        }
      } 

      return Optional.empty();
    }

    public static HashSet<String> getUserCategories(){
      HashSet<String> set = new HashSet<>();
      set.addAll(userItems.keySet());
      return set;
    }

    public static HashSet<String> getCategories(){
      HashSet<String> set = new HashSet<>();
      set.addAll(userItems.keySet());
      set.addAll(itemTypes.keySet());
      return set;
    }

    public static HashSet<ObjectTypeData> getUserItems(String category) {
      HashSet<ObjectTypeData> set = new HashSet<>();
      HashMap<String, ObjectTypeData>  map = userItems.get(category);
      if (map != null){
        set.addAll(map.values());
      }
      return set;
    }

    public static HashSet<ObjectTypeData> getItems(String category) {
      HashSet<ObjectTypeData> set = new HashSet<>();
      HashMap<String, ObjectTypeData>  map = userItems.get(category);
      if (map != null){
        set.addAll(map.values());
      }

      map = itemTypes.get(category);
      if (map != null){
        set.addAll(map.values());
      }
      return set;
    }
    public static void clearUserItems(){
      userItems.clear();
      AssetManager.instance().clearUserTextures();
    }

  }



  public static void LoadAllObjects() {
    loadTypes("objects", LoadedObjects.itemTypes);
  }

  public static void LoadUserObjects(String objectDir) {
    loadTypes(objectDir, LoadedObjects.userItems);
  }

  private static void loadTypes(String objectFolder, LoadedObjects.ItemMap itemMap) {
    for (String category : FileUtils.getFolders(objectFolder)) {

      if (!itemMap.containsKey(category)){
        itemMap.put(category, new HashMap<>());
      }

      for (String filename : FileUtils.getFiles(objectFolder + "/" + category)) {
        JsonValue object;
        try {
          FileReader fileReader = new FileReader(objectFolder + "/" + category + "/" + filename);
          JsonReader reader = new JsonReader();
          object = reader.parse(fileReader);
        } catch (Exception e) {
          Notifier.error(String.format("Failed to load item from %s", filename));
          continue;
        }
        ObjectTypeData data = new ObjectTypeData();
        data.read(new Json(), object);
        data.category = category;
        itemMap.get(category).put(data.name, data);
      }
    }
  }

}
