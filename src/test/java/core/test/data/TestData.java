package core.test.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import core.setup.Config;
import core.utilities.Tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class TestData {
  public final User user;

  public TestData(String username) {
    this.user = setUserData(username);
    setDataFromCliIfNotSet();
  }

  public static class User {
    public String username;
    public String password;
    public String placeholderForOtherData;
    public List<String> placeholderForListData;
    public PlaceholderForChildClassData childClass = new PlaceholderForChildClassData();

    public static class PlaceholderForChildClassData {
      public String childClassDataEX;
      public String childClassDataEX2;
    }
  }

  /**
   * parses and sets data from json
   *
   * @param username the name of the json file
   * @return created gson from parsed json
   */
  private User setUserData(String username) {
    InputStream file;
    BufferedReader reader;
    JsonElement jsonElement;

    file = getClass().getResourceAsStream(String.format("/jsonData/%s.json", username));
    reader = new BufferedReader(new InputStreamReader(file));
    jsonElement = new JsonParser().parse(reader).getAsJsonObject();

    return new Gson().fromJson(jsonElement, User.class);
  }

  /** sets the data from the CLI data is entered there */
  private void setDataFromCliIfNotSet() {
    user.username = Tools.setFromCLIifNotSet(user.username, Config.USERNAME);
    user.password = Tools.setFromCLIifNotSet(user.password, Config.PASSWORD);
  }
}
