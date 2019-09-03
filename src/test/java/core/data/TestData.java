package core.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import core.utilities.Tools;
import core.utilities.setup.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestData {
  public final User user;

  public TestData(String username) {
    this.user = setUserData(username);
    setDataFromCliIfNotSet();
  }

  /**
   * parses and sets data from json
   *
   * @param username the name of the json file
   * @return created gson from parsed json
   */
  private User setUserData(String username) {
    InputStream file = getClass().getResourceAsStream(String.format("/jsonData/%s.json", username));
    BufferedReader reader = new BufferedReader(new InputStreamReader(file));

    Gson gson = new Gson();
    JsonParser parser = new JsonParser();

    JsonElement jsonElement = parser.parse(reader).getAsJsonObject();

    return gson.fromJson(jsonElement, User.class);
  }

  /** sets the data from the CLI data is entered there */
  private void setDataFromCliIfNotSet() {
    user.username = Tools.setFromCLIifNotSet(user.username, Config.USERNAME);
    user.password = Tools.setFromCLIifNotSet(user.password, Config.PASSWORD);
  }
}
