package core.data;

import java.util.List;

public class User {
  public String username;
  public String password;
  public String placeholderForOtherData;
  public List<String> placeholderForListData;
  public PlaceholderForChildClassData childClass = new PlaceholderForChildClassData();

  public class PlaceholderForChildClassData {
    public String childClassDataEX;
    public String childClassDataEX2;
  }
}
