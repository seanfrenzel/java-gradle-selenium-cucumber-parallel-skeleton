package core.utilities;

import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Tools {

  private Tools() {
    throw new IllegalStateException("Utility Class");
  }

  /**
   * logger method that will get the current class where used. Chain off this for other log options
   *
   * @return the declaring class to log with
   */
  public static Logger logger() {
    String declaringClass = Thread.currentThread().getStackTrace()[2].getClassName();
    return LogManager.getLogger(declaringClass);
  }

  /** gets random string */
  public static String getRandomString(int size) {
    return generateRandom("QWERTYUIOPASDFGHJKLZXCVBNM", size);
  }

  /** gets random int string */
  public static String getRandomIntString(int size) {
    return generateRandom("123456789", size);
  }

  /** gets a random int */
  public static Integer getRandomInt(int size) {
    Random rand = new Random();
    return rand.nextInt(size);
  }

  /** removes all special characters */
  public static String removeSpecialCharacters(String value) {
    return value.replaceAll("[^a-zA-Z]", "");
  }

  /** generates a random string */
  private static String generateRandom(String characters, int count) {
    StringBuilder builder = new StringBuilder();

    while (count-- != 0) {
      int character = (int) (Math.random() * characters.length());
      builder.append(characters.charAt(character));
    }
    return builder.toString();
  }

  /**
   * gets current date with desired SimpleDateFormat and adds or substracts dates based on
   * daysToAddOrSubstract param
   *
   * @param dateFormat SimpleDateFormat to use
   * @param daysToAddOrSubstract how many days from the current date we want to use
   * @return the created date formate
   */
  public static String getDate(String dateFormat, int daysToAddOrSubstract) {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, daysToAddOrSubstract);
    return formatter.format(calendar.getTime());
  }

  /**
   * camel cases strings
   *
   * @param string to camel case
   * @return camel cased string
   */
  public static String toCamelCase(String string) {
    return CaseUtils.toCamelCase(string, false);
  }

  /**
   * builds string list from element list
   *
   * @param elements list of elements to build string list from
   * @return bulit string list
   */
  public static List<String> buildStringListFromElemList(List<WebElement> elements) {
    return elements.stream().map(WebElement::getText).distinct().collect(Collectors.toList());
  }

  /**
   * builds list from substring targets
   *
   * @param strings list of strings to build substring from
   * @param targets list of targets to build substrings from
   * @return the list of substrings
   */
  public static List<String> buildSubstringTargetsList(List<String> strings, List<String> targets) {
    List<String> newList = new ArrayList<>();

    targets.stream()
        .map(target -> buildSubstringListFromStringList(strings, target))
        .forEach(newList::addAll);
    return newList;
  }

  /**
   * builds substring list from string list
   *
   * @param list string list to build substrings from
   * @param targetText target substring to find and build with
   * @return built substring list
   */
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

  /**
   * gets substring with substringTarget and returns found substring
   *
   * @param text text we want to get the substring from
   * @param substringTarget target substring we want to find in text
   * @return the target substring
   */
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

  /**
   * sets from command line if data is notSet in json
   *
   * @param property the property we want to set
   * @param sysProperty sys property to use if not set in property
   * @return the set property
   */
  public static String setFromCLIifNotSet(String property, String sysProperty) {
    if (property.equals("notSet") && !sysProperty.equals("notSet")) property = sysProperty;
    return property;
  }

  /** prints out horizontal dash line to pad text */
  public static void printBorderLine() {
    System.out.println(
        format(
            "%n--------------------------------------------------------------------------------------------------------------%n"));
  }

  /**
   * puts a border (dash horizaontal lines) around text with new lines
   *
   * @param stringToBorder string we want to vorder
   * @return bordered text that will be formatted to be on a new line
   */
  public static String border(String stringToBorder) {
    return String.format(
        "%n%s %n%s %n%s%n",
        "--------------------------------------------------------------------------------------------------------------",
        stringToBorder,
        "--------------------------------------------------------------------------------------------------------------");
  }

  /**
   * puts a border (dash horizaontal lines) around FORMATTED text from args with new lines
   *
   * @param stringToBorder string we want to vorder
   * @args the args to format a string with.
   * @return bordered text that will be formatted to be on a new line
   */
  /** prints out horizontal dash line to pad text */
  public static String border(String stringToBorder, Object... args) {
    return String.format(
        "%n%s %n%s %n%s%n",
        "--------------------------------------------------------------------------------------------------------------",
        format(stringToBorder, args),
        "--------------------------------------------------------------------------------------------------------------");
  }
}
