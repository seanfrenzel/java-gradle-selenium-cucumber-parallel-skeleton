package core.utilities.setup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.getProperty;

public class Config {
  public static final String WORKSPACE = getProperty("user.dir");
  public static String env = getProperty("env", "https://gfycat.com/");
  public static final String USER = getProperty("user", "user1");
  public static String USERNAME = getProperty("username", "notSet");
  public static String PASSWORD = getProperty("password", "notSet");

  private String deviceName;
  private String url;
  private Map<String, Object> capabilities;

  public Config() {
    Logger.getLogger("org.openqa.core.remote").setLevel(Level.OFF);
    setWebCapabilities();
  }

  private void setWebCapabilities() {
    deviceName = getProperty("deviceName", "chrome");
    url = getProperty("seleniumGrid", "http://0.0.0.0:4444/wd/hub");
    capabilities = getDeviceCapabilities(deviceName);
  }

  private HashMap<String, Object> getDeviceCapabilities(String device) {
    InputStream file;
    BufferedReader reader;
    Type hashType;
    JsonElement jsonElement;

    file = getClass().getResourceAsStream("/jsonData/devices.json");
    reader = new BufferedReader(new InputStreamReader(file));
    hashType = new TypeToken<HashMap<String, Object>>() {}.getType();
    jsonElement = new JsonParser().parse(reader).getAsJsonObject().get(device);

    return new Gson().fromJson(jsonElement, hashType);
  }

  //////////////////
  // Get and Sets //
  //////////////////

  public Map<String, Object> getCapabilities() {
    return capabilities;
  }

  public String getUrl() {
    return url;
  }

  public static String getEnv() {
    return env;
  }

  public static void setEnv(String env) {
    Config.env = env;
  }

  String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }
}
