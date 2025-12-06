package group24.escaperoom.data;

import java.io.File;
import java.io.FileOutputStream;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import group24.escaperoom.entities.Item;
import group24.escaperoom.utils.FileUtils;

public class ItemSaver {

  public static boolean saveCustomItem(Item item, MapMetadata metadata){
    // create the app data directory if it doesn't exist
    File mapDir = new File(FileUtils.getAppDataDir());
    if (!FileUtils.tryCreateFolder(mapDir)) return false;

    // create this maps folder if it doesn't exist
    File dir = new File(metadata.locations.mapBasePath);
    if (!FileUtils.tryCreateFolder(dir)) return false; 

    // create this maps content folder if it doesn't exist
    dir = new File(metadata.locations.mapContentPath);
    if (!FileUtils.tryCreateFolder(dir)) return false; 

    // create this maps object folder if it doesn't exist
    dir = new File(metadata.locations.mapContentPath + "/objects");
    if (!FileUtils.tryCreateFolder(dir)) return false;

    // create this items category folder if it doesn't exist
    dir = new File(metadata.locations.mapContentPath + "/objects/" + item.getType().category);
    if (!FileUtils.tryCreateFolder(dir)) return false;

    String fileName = Integer.toString(item.getType().name.hashCode()) + ".json";
    File itemFile = new File(dir, fileName);

    // write the itemData to the map file
    try {
      FileOutputStream fout = new FileOutputStream(itemFile);
      Json j = new Json();
      j.setOutputType(JsonWriter.OutputType.json);
      fout.write(j.prettyPrint(item.getType()).getBytes());
      fout.close();
    } catch (Exception e) {
      e.printStackTrace(); 
      return false;
    }

    return true;
  }

}
