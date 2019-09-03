package core.utilities;

import org.apache.commons.text.CaseUtils;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Tools {

  private Tools() {
    throw new IllegalStateException("Utility Class");
  }

  public static String getRandomString(int size) {
    return generateRandom("QWERTYUIOPASDFGHJKLZXCVBNM", size);
  }

  public static String getRandomIntString(int size) {
    return generateRandom("123456789", size);
  }

  public static Integer getRandomInt(int size) {
    Random rand = new Random();
    return rand.nextInt(size);
  }

  public static String removeSpecialCharacters(String value) {
    return value.replaceAll("[^a-zA-Z]", "");
  }

  private static String generateRandom(String characters, int count) {
    StringBuilder builder = new StringBuilder();

    while (count-- != 0) {
      int character = (int) (Math.random() * characters.length());
      builder.append(characters.charAt(character));
    }
    return builder.toString();
  }

  public static String getDate(String dateFormat, int daysToAddOrSubstract) {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, daysToAddOrSubstract);
    return formatter.format(calendar.getTime());
  }

  public static String toCamelCase(String string) {
    return CaseUtils.toCamelCase(string, false);
  }

  public static List<String> buildStringListFromElemList(List<WebElement> elements) {
    return elements.stream().map(WebElement::getText).distinct().collect(Collectors.toList());
  }

  public static List<String> buildSubstringTargetsList(List<String> strings, List<String> targets) {
    List<String> newList = new ArrayList<>();

    targets.stream()
        .map(target -> buildSubstringListFromStringList(strings, target))
        .forEach(newList::addAll);
    return newList;
  }

  private static List<String> buildSubstringListFromStringList(
      List<String> list, String targetText) {

    list =
        list.stream()
            .map(
                string -> {
                  try {
                    return getSubstringTargetText(string, targetText);
                  } catch (IndexOutOfBoundsException ignored) {
                    // Ignored since not all strings in list will contain targetText
                  }
                  return "";
                })
            .distinct()
            .collect(Collectors.toList());

    list.removeAll(Collections.singleton(""));
    return list;
  }

  public static List<String> buildSubstringListFromStringList(
      List<String> stringList, int startIndex, int endIndex) {

    return stringList.stream()
        .map(string -> string.substring(startIndex, endIndex))
        .distinct()
        .collect(Collectors.toList());
  }

  public static String getSubstringTargetText(String text, String substringTarget) {
    try {
      int firstIndex = text.indexOf(substringTarget);
      int lastIndex = text.lastIndexOf(substringTarget);
      String string = text.substring(firstIndex, lastIndex).concat(substringTarget);

      boolean dupeSubstrings = string.length() > substringTarget.length();
      if (dupeSubstrings) string = string.substring(0, substringTarget.length());
      return string;

    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException(String.format("Target [%s] Not Found", substringTarget));
    }
  }

  public static String getSubstringBetween(
      String textToGetSubstringFrom, String firstIndexTarget, String lastIndexTarget) {
    try {
      int firstIndex = textToGetSubstringFrom.indexOf(firstIndexTarget) + 1;
      int lastIndex = textToGetSubstringFrom.lastIndexOf(lastIndexTarget);
      return textToGetSubstringFrom.substring(firstIndex, lastIndex);

    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException(
          String.format(
              "Target [%s] or [%s] was Not Found in string [%s]",
              firstIndexTarget, lastIndexTarget, textToGetSubstringFrom));
    }
  }

  public static String setFromCLIifNotSet(String property, String sysProperty) {
    if (property.equals("notSet") && !sysProperty.equals("notSet")) property = sysProperty;
    return property;
  }
}
