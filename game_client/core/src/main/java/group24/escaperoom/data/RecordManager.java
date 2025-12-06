package group24.escaperoom.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import group24.escaperoom.utils.FileUtils;

public class RecordManager {
  Logger log = Logger.getLogger(RecordManager.class.getName());
  private static RecordManager recordManager;
  private String manifestPath;

  private ClearManifest manifest = new ClearManifest();
  private static class ClearManifest extends HashSet<String> implements Json.Serializable {
    @Override
    public void write(Json json) {
      json.writeArrayStart("cleared");
      for (String id : this){
        json.writeValue(id);
      }
      json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
      JsonValue completed = jsonData.get("cleared");
      if (completed != null) {
        for (JsonValue v : completed) {
          add(v.asString());
        }
      }
    }
  }

  private RecordManager() {

    File recordDir = new File(FileUtils.getAppDataDir(), "record");
    if (!recordDir.exists()) {
      recordDir.mkdir();
    }

    File manifestFile = new File(recordDir, "manifest.json");
    if (!manifestFile.exists()) {
      try {
        manifestFile.createNewFile();
      } catch (IOException ioe) {
        log.severe("Failed to create record manifest file...");
      }
    }

    manifestPath = manifestFile.getAbsolutePath();

    try {
      String jsonStr = Files.readString(manifestFile.toPath());
      JsonReader reader = new JsonReader();
      JsonValue jsonValue = reader.parse(jsonStr);
      manifest.read(new Json(), jsonValue);
    } catch (IOException ioe) {
      log.severe("Failed to read record manifest file...");
    } catch (Exception e) {
      log.severe("Failed to parse record manifest file...");
    }
  }

  public static RecordManager get() {
    if (recordManager == null) {
      recordManager = new RecordManager();
    }
    return recordManager;
  }

  public boolean registerClear(String mapID) {
    if (manifest.contains(mapID)) {
      return false;
    }
    manifest.add(mapID);
    return saveManifest();
  }

  private boolean saveManifest() {
    File f = new File(manifestPath);
    if (!f.exists()) {
      return false;
    }

    try {
      FileOutputStream fout = new FileOutputStream(f);
      Json j = new Json();
      j.setOutputType(JsonWriter.OutputType.json);
      fout.write(j.toJson(manifest).getBytes());
      fout.close();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public int getUniqueClears() {
    return manifest.size();
  }

}
