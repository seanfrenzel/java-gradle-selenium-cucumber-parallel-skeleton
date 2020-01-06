package core.setup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static core.utilities.Tools.border;
import static core.utilities.Tools.logger;
import static java.lang.System.getProperty;

public class Config {
  public static final String WORKSPACE = getProperty("user.dir");
  public String logLevel = getProperty("logLevel", "notSet");
  public static String USER = getProperty("user", "user1");
  public static String USERNAME = getProperty("username", "notSet");
  public static String PASSWORD = getProperty("password", "notSet");
  public static String PROD_USER = getProperty("user", "user1Prod");
  private static String env = getProperty("env", "https://gfycat.com/");
  Boolean parallel = Boolean.parseBoolean(getProperty("parallel", "false"));
  public static final Boolean IS_REMOTE = Boolean.parseBoolean(getProperty("isRemote", "false"));

  private static String deviceName;
  private String url;
  private Map<String, Object> capabilities;

  /**
   * set platform property to -> Android, iOS, or Web and then sets desired capabilities based off
   * set platform
   */
  public Config() {
    logger().traceEntry();
    logger().traceExit();
  }

  /** sets Web Desired Capabilities */
  void setCapabilities() {
    logger().traceEntry();
    deviceName = getProperty("deviceName", "chrome");
    capabilities = getDeviceCapabilities(deviceName);
    url = getProperty("seleniumGrid", "http://localhost:4444/wd/hub");
    logger().traceExit();
  }

  /**
   * Deserialize json into desired capabilities map
   *
   * @param device
   * @return desired capabilities map created from json
   */
  private HashMap<String, Object> getDeviceCapabilities(String device) {
    logger().traceEntry();
    InputStream file;
    BufferedReader reader;
    Type hashType;
    JsonElement jsonElement;

    file = getClass().getResourceAsStream("/jsonData/devices.json");
    reader = new BufferedReader(new InputStreamReader(file));
    hashType = new TypeToken<HashMap<String, Object>>() {}.getType();
    jsonElement = new JsonParser().parse(reader).getAsJsonObject().get(device);

    HashMap<String, Object> stringObjectHashMap = new Gson().fromJson(jsonElement, hashType);
    logger().traceExit(stringObjectHashMap);
    return stringObjectHashMap;
  }

  /**
   * sets log level during runtime if a value is provided through the CLI. Default will be WARN
   *
   * @param logLevel log level we want to use
   */
  void setLogLevel(String logLevel) {
    String name = LogManager.getRootLogger().getName();

    if (!logLevel.equals("notSet")) {

      switch (logLevel.toLowerCase()) {
        case "all":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.ALL);
          break;
        case "debug":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.DEBUG);
          break;
        case "error":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.ERROR);
          break;
        case "fatal":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.FATAL);
          break;
        case "warn":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.WARN);
          break;
        case "info":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.INFO);
          break;
        case "off":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.OFF);
          break;
        case "trace":
          Configurator.setAllLevels(name, org.apache.logging.log4j.Level.TRACE);
          break;
        default:
          throw new IllegalArgumentException(
              border(
                  "[%s] is not one of the valid log levels: [all, debug, error, fatal, warn, info, off, trace]",
                  logLevel));
      }
    }
    logger().traceExit();
  }

  // <editor-fold desc="Get and Sets">
  Map<String, Object> getCapabilities() {
    logger().traceEntry();
    logger().traceExit(capabilities);
    return capabilities;
  }

  String getUrl() {
    logger().traceEntry();
    logger().traceExit(url);
    return url;
  }

  public static String getEnv() {
    logger().traceEntry();
    logger().traceExit(env);
    return env;
  }

  static void setEnv(String env) {
    logger().traceEntry();
    logger().traceExit(env);
    Config.env = env;
  }

  static String getDeviceName() {
    return deviceName;
  }
  // </editor-fold>

}
