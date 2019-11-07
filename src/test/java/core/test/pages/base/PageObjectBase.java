package core.test.pages.base;

import core.setup.Config;
import core.setup.Hooks;
import core.test.data.TestData;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static core.utilities.Tools.*;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;

public abstract class PageObjectBase {
  public RemoteWebDriver driver;
  public TestData data;
  public SoftAssertions soft = Hooks.getSoftAssert();

  public PageObjectBase() {
    this.driver = Hooks.getDriver();
    this.data = Hooks.getTestData();
    try {
      driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    } catch (ClassCastException e) {
      // ignores log cast exception
    }
    setAjaxDecorator();
  }

  private void setAjaxDecorator() {
    AjaxElementLocatorFactory decorator = new AjaxElementLocatorFactory(driver, 1);
    PageFactory.initElements(decorator, this);
  }

  /**
   * implement logic needed to assert page was navigated to. IMPORTANT: Put this inside of
   * navigateHere() after the logic to nav to the page has been performed
   */
  public abstract void trait();

  /** implement logic needed to navigate to the page this method is present on */
  public abstract void navigateHere();

  /** Modules Initialization */
  public ModuleInitializations module() {
    logger().traceEntry();
    ModuleInitializations moduleInitializations = new ModuleInitializations();
    logger().traceExit(moduleInitializations);
    return moduleInitializations;
  }

  // <editor-fold desc="Action Methods">
  /** @param url to load with defined ENVIRONMENT */
  public void loadEnv(String url) {
    logger().traceEntry();
    Assert.assertTrue("The ENV given was incorrect or not found", url.contains(Config.getEnv()));
    driver.get(url);
    assertEquals(url, driver.getCurrentUrl());
    logger().traceExit(url);
  }

  /** @param element to scroll into view */
  public void scrollIntoView(WebElement element) {
    logger().traceEntry();
    driver.executeScript("arguments[0].scrollIntoView(true);", element);
    logger().traceExit();
  }

  /** @param element to click on with javascript if a regular click is not working */
  public void jsClick(WebElement element) {
    logger().traceEntry();
    JavascriptExecutor jse = driver;
    try {
      element.click();
    } catch (Exception e) {
      jse.executeScript("arguments[0].click();", element);
    }
    logger().traceExit();
  }

  /**
   * @param element to set the value from
   * @param value we want to set for the element
   */
  public void jsSetValue(WebElement element, String value) {
    logger().traceEntry();
    JavascriptExecutor js = driver;
    js.executeScript(String.format("arguments[0].value='%s';", value), element);
    logger().traceExit();
  }

  /** @param element that we want to clear the value from */
  public void jsClear(WebElement element) {
    logger().traceEntry();
    JavascriptExecutor js = driver;
    js.executeScript("arguments[0].value = '';", element);
    logger().traceExit();
  }

  /**
   * @param elems list of elements to click on
   * @param index index to start clicking from
   * @param amount how many times to click on elements Sequentially
   */
  public void clickSequentially(List<WebElement> elems, Integer index, Integer amount) {
    logger().traceEntry();
    int clicked = 0;

    while (clicked < amount) {
      elems.get(index).click();
      clicked++;
      index++;
    }
    logger().traceExit();
  }

  /**
   * @param element element that we want to click on
   * @param amount how many times to click on element
   */
  public void clickMultiple(WebElement element, Integer amount) {
    logger().traceEntry();
    int clicked = 0;

    while (clicked < amount) {
      element.click();
      clicked++;
    }
    logger().traceExit();
  }

  /**
   * Enters text from each cell for the header element(header text must be name of element on page).
   * Replaces [random] with a randomly generated string of 5 characters
   *
   * @param table DataTable provided from cucumber step
   */
  public void fillDataTableFields(DataTable table) {
    logger().traceEntry();
    table
        .asMaps()
        .forEach(
            row ->
                row.forEach(
                    (header, cell) -> {
                      String replacedRandomCellValue =
                          cell.replaceAll("(\\[random])", getRandomString(5) + "Test");

                      getElement(header).sendKeys(replacedRandomCellValue);
                    }));
    logger().traceExit();
  }

  /** Overloaded method with className param */
  public void fillDataTableFields(DataTable table, String className) {
    logger().traceEntry();
    table
        .asMaps()
        .forEach(
            row ->
                row.forEach(
                    (header, cell) -> {
                      String replacedRandomCellValue =
                          cell.replaceAll("(\\[random])", getRandomString(5) + "Test");

                      getElement(header, className).sendKeys(replacedRandomCellValue);
                    }));
    logger().traceExit();
  }
  // </editor-fold>

  // <editor-fold desc="Get Methods">
  /**
   * element field to find within the given class
   *
   * @param elementField Name of element field to find
   * @param className name of class to use
   * @return element found casted with WebElement
   */
  public WebElement getElement(String elementField, String className) {
    logger().traceEntry();
    WebElement field = (WebElement) getField(elementField, getClassByName(className));
    logger().traceExit(field);
    return field;
  }

  /**
   * element field to find within the current class instance being used
   *
   * @param elementField Name of element field to find
   * @return element found casted with WebElement
   */
  public WebElement getElement(String elementField) {
    logger().traceEntry();
    WebElement field = (WebElement) getField(elementField);
    logger().traceExit(field);
    return field;
  }

  /**
   * @param elementsField Name of element list field to find
   * @return returns found field as List<WebElement>
   */
  @SuppressWarnings("unchecked")
  public List<WebElement> getElements(String elementsField) {
    logger().traceEntry();
    List<WebElement> field = (List<WebElement>) getField(elementsField);
    logger().traceExit(field);
    return field;
  }

  /**
   * @param elementsField Name of element list field to find
   * @param className name of class to use
   * @return returns found field as List<WebElement>
   */
  @SuppressWarnings("unchecked")
  public List<WebElement> getElements(String elementsField, String className) {
    logger().traceEntry();
    List<WebElement> field = (List<WebElement>) getField(elementsField, getClassByName(className));
    logger().traceExit(field);
    return field;
  }

  /**
   * @param elementFields list of element fields to get
   * @return returns found fields as List<WebElement>
   */
  public List<WebElement> getElements(List<String> elementFields) {
    logger().traceEntry();
    List<WebElement> list =
        elementFields.stream().map(this::getElement).distinct().collect(toList());
    logger().traceExit(list);
    return list;
  }

  /**
   * @param elementFields list of element fields to get
   * @return returns found fields as List<WebElement>
   */
  public List<WebElement> getElements(List<String> elementFields, String className) {
    logger().traceEntry();
    List<WebElement> list =
        elementFields.stream()
            .map(elementField -> getElement(elementField, className))
            .distinct()
            .collect(toList());
    logger().traceExit();
    return list;
  }

  /**
   * @param fieldName Name of declared field on page that will get camel cased
   * @return Found field with param fieldName from class
   */
  private Object getField(String fieldName) {
    logger().traceEntry();
    String target = toCamelCase(fieldName);
    Class aClass = null;

    try {
      aClass = getClass();
      Field field = aClass.getDeclaredField(target);
      field.setAccessible(true);
      Object fieldFound = field.get(this);
      logger().traceExit(fieldFound);
      return fieldFound;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          String.format("Element not found: [%s] in Class [%s]", target, aClass.getSimpleName()));
    }
  }

  /**
   * @param fieldName Name of declared field on page that will get camel cased
   * @param aClass class to find field in
   * @return Found field with param fieldName from class
   */
  @SuppressWarnings("unchecked")
  private Object getField(String fieldName, Class aClass) {
    logger().traceEntry();
    String target = toCamelCase(fieldName);

    try {
      aClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      trait();
      Field field = aClass.getDeclaredField(target);
      field.setAccessible(true);
      Object fieldFound = field.get(this);
      logger().traceExit(fieldFound);
      return fieldFound;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          String.format("Element not found: [%s] in Class [%s]", target, aClass.getSimpleName()));
    }
  }

  /**
   * @param className name of class to use
   * @return found class with declared packages and provided name
   */
  private Class<?> getClassByName(String className) {
    logger().traceEntry();
    Class<?> targetClass = null;
    int index = 0;

    List<String> packages = Arrays.asList("pages", "modules", "base");
    // ^ if new packages for pages are added, they need to be put here as well

    while (targetClass == null && index <= packages.size() - 1) {

      try {
        targetClass =
            Class.forName(
                String.format(
                    "core.test.pages.%s.%s", packages.get(index), className.replaceAll("\\s", "")));
      } catch (ClassNotFoundException e) {
        index++;
      }
    }

    Assert.assertNotNull(String.format("[%s] was not found", className), targetClass);
    logger().traceExit(targetClass);
    return targetClass;
  }

  /**
   * Get element with text
   *
   * @param elements list of elements to search through
   * @param text text to look for in elements
   * @return element found with text
   */
  public WebElement getElementWithText(List<WebElement> elements, String text) {
    logger().traceEntry();
    WebElement webElement =
        elements.stream()
            .filter(elem -> elem.getText().trim().equalsIgnoreCase(text))
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        String.format("Element Target Text was not found: [%s]", text)));
    logger().traceExit(webElement);
    return webElement;
  }

  /**
   * Get element with text
   *
   * @param elements list of elements to search through
   * @param attrValue text to look for in elements
   * @return element found with text
   */
  public WebElement getElementWithAttribute(
      List<WebElement> elements, String attribute, String attrValue) {
    logger().traceEntry();
    WebElement webElement =
        elements.stream()
            .filter(elem -> elem.getAttribute(attribute).trim().equalsIgnoreCase(attrValue))
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        String.format("Element Attribute was not found: [%s]", attrValue)));
    logger().traceExit(webElement);
    return webElement;
  }

  /**
   * Get elements with text
   *
   * @param elems the elements to iterate through and get with matching text
   * @param strings the text list to iterate through and get an element with the target text
   * @return the elements found with target text
   */
  public List<WebElement> getElementsFromTextList(List<WebElement> elems, List<String> strings) {
    logger().traceEntry();
    List<WebElement> list =
        strings.stream().map(text -> getElementWithText(elems, text)).distinct().collect(toList());
    logger().traceExit(list);
    return list;
  }
  // </editor-fold>

  // <editor-fold desc="Assert Methods">
  /**
   * Assert Element Displayed and return found elements
   *
   * @param element the WebElement we want to wait for to be displayed
   * @param waitSec how many seconds to wait
   * @return element to be chained off of EX:assertDisplayed(element, 5).click
   */
  public WebElement assertDisplayed(WebElement element, int waitSec) {
    logger().traceEntry();
    fluentWait(waitSec, 1)
        .until(
            ExpectedConditions.or(
                ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)),
                ExpectedConditions.visibilityOf(element)));

    logger().traceExit();
    return element;
  }

  /**
   * Assert all Element Displayed and return found elements
   *
   * @param elements the WebElements we want to wait for to be displayed
   * @return elements to be chained off of EX:assertAllDisplayed(element, 5).get(0);
   */
  public List<WebElement> assertAllDisplayed(List<WebElement> elements) {
    logger().traceEntry();

    List<WebElement> list =
        elements.stream().map(element -> assertDisplayed(element, 1)).distinct().collect(toList());

    return logger().traceExit(list);
  }

  /**
   * assert all elements with attribute
   *
   * @param element list of elements to search through
   * @param attribute attribute to look in
   * @param attrValue text to look for in elements
   * @return element found with text
   */
  public boolean doesElementContainAttribute(
      WebElement element, String attribute, String attrValue) {
    logger().traceEntry();
    boolean contains = element.getAttribute(attribute).contains(attrValue);
    return logger().traceExit(contains);
  }

  /**
   * assert all elements with attribute
   *
   * @param elements list of elements to search through
   * @param attribute attribute to look in
   * @param attrValue text to look for in elements
   * @return element found with text
   */
  public boolean assertElementsContainAttribute(
      List<WebElement> elements, String attribute, String attrValue) {
    logger().traceEntry();
    boolean result =
        elements.stream().allMatch(elem -> elem.getAttribute(attribute).contains(attrValue));
    return logger().traceExit(result);
  }

  /** @param element to verify as non existing */
  public void assertElementDoesNotExist(WebElement element) {
    logger().traceEntry();
    try {
      element.isDisplayed();
      throw new IllegalArgumentException("Element was unexpectedly present");
    } catch (NoSuchElementException | IndexOutOfBoundsException | ElementNotVisibleException e) {
      // Element does not exist
    }
    logger().traceExit();
  }

  /**
   * @param elemFoundBy By to find element with. We have to use a By to avoid an unwanted wait with
   *     the global implicit for an element NOT EXISTING
   */
  public void assertElementDoesNotExist(By elemFoundBy) {
    logger().traceEntry();
    try {
      driver.findElement(elemFoundBy).isDisplayed();
      throw new IllegalArgumentException("Element was unexpectedly present");
    } catch (NoSuchElementException | IndexOutOfBoundsException | ElementNotVisibleException e) {
      // Element does not exist
    }
    logger().traceExit();
  }

  /**
   * @param elements to search through
   * @param text to verify is not found within list
   * @return true that the text was not found
   */
  public boolean assertTextNotFound(List<WebElement> elements, String text) {
    logger().traceEntry();
    Assert.assertTrue(
        String.format("Text was found: [%s]", text),
        elements.stream().noneMatch(element -> element.getText().equalsIgnoreCase(text)));
    logger().traceExit();
    return true;
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @return true that the text was found
   */
  public boolean assertTextFound(List<WebElement> elements, String text) {
    logger().traceEntry();
    Assert.assertTrue(
        String.format("Text was not found: [%s]", text),
        elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
    logger().traceExit();
    return true;
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @return true that the text was found
   */
  public void assertTextFoundSoftly(List<WebElement> elements, String text) {
    logger().traceEntry();
    assertIsTrueSoftly(
        String.format("Text was not found: [%s]", text),
        elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
    logger().traceExit();
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @param errMsg custom error message to use
   * @return true that the text was found
   */
  public void assertTextFoundSoftly(List<WebElement> elements, String text, String errMsg) {
    logger().traceEntry();
    assertIsTrueSoftly(
        errMsg, elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
    logger().traceExit();
  }

  /**
   * @param strings to search through
   * @param text to verify is not found within list
   */
  public void assertTextNotFoundSoftly(List<String> strings, String text) {
    logger().traceEntry();
    soft.assertThat(strings.stream().noneMatch(string -> string.equalsIgnoreCase(text)));
    logger().traceExit();
  }

  /**
   * @param strings to search through
   * @param text to verify is not found within list
   */
  public void assertTextNotFoundInStringList(List<String> strings, String text) {
    logger().traceEntry();
    Assert.assertTrue(
        String.format("Text was found: [%s]", text),
        strings.stream().noneMatch(string -> string.equalsIgnoreCase(text)));
    logger().traceExit();
  }

  /**
   * @param elements to search through
   * @param text substring to find in elements
   */
  public void assertSubstringFoundInList(List<WebElement> elements, String text) {
    logger().traceEntry();
    List<String> targetText = Collections.singletonList(text);
    List<String> elementsText = buildStringListFromElemList(elements);
    List<String> substrings = buildSubstringTargetsList(elementsText, targetText);

    valuesContained(substrings, targetText);
    logger().traceExit();
  }

  /**
   * @param strings to search through
   * @param text to find contained within strings
   */
  public void assertTextContains(List<String> strings, String text) {
    logger().traceEntry();
    String substringBetweenBrackets;
    String formattedList;
    String errorMsg;

    substringBetweenBrackets = getSubstringBetween(strings.toString(), "[", "]");
    formattedList = substringBetweenBrackets.replaceAll("(\\s*,\\s*)", "\n* ");
    errorMsg =
        String.format(
            "%n----------%nActual List: %n* %s%n%nDid not contain expected: %n* %s%n----------%n",
            formattedList, text);

    Assert.assertTrue(errorMsg, strings.stream().anyMatch(string -> string.contains(text)));
    logger().traceExit();
  }

  /**
   * @param strings to search through
   * @param text to find contained within strings
   */
  public void assertTextContainsSoftly(List<String> strings, String text) {
    logger().traceEntry();
    assertIsTrueSoftly(
        String.format("List %s did not contain [%s]", strings, text),
        strings.stream().anyMatch(string -> string.contains(text)));
    logger().traceExit();
  }

  /**
   * assert the expected object equals the actual object
   *
   * @param expected
   * @param actual
   */
  public void assertEquals(Object expected, Object actual) {
    logger().traceEntry();
    String errorMsg =
        String.format(
            "Expected and Actual were not equal%n%nExpected:[%s]%nActual:  [%s]%n%n",
            expected, actual);
    Assert.assertEquals(errorMsg, expected, actual);
    logger().traceExit();
  }

  /**
   * This is a soft assert that will only be hard failed by a soft.assertAll after the scenario has
   * been completed in the Hooks
   *
   * @param condition boolean condition that we want to verify is TRUE
   * @param errorMsg error message to use for this specifc soft assert if failed
   */
  public void assertIsTrueSoftly(String errorMsg, boolean condition) {
    logger().traceEntry();
    String trace = null;

    if (!condition) {
      Hooks.takeScreenshot();
      Throwable throwable = new Throwable();
      trace =
          String.format(
              "[Class -> %s]%n[Method -> %s]%n[Line -> %d]",
              throwable.getStackTrace()[1].getClassName(),
              throwable.getStackTrace()[1].getMethodName(),
              throwable.getStackTrace()[1].getLineNumber());
    }
    soft.assertThat(condition)
        .withFailMessage(String.format("%nError: %s%n%s", errorMsg, trace))
        .isTrue();
    logger().traceExit();
  }

  /**
   * @param actualValues list of strings to check against
   * @param expectedValues to find contained within actualValues
   */
  public void valuesContained(List<String> actualValues, List<String> expectedValues) {
    logger().traceEntry();
    List<String> values = new ArrayList<>(expectedValues);

    String errorMsg =
        String.format(
            "%n%s - actual values did not contain %n%s - expected values ", actualValues, values);

    Assert.assertTrue(errorMsg, actualValues.containsAll(values));
    logger().traceExit();
  }

  /**
   * @param actualValues list of strings to check against
   * @param expectedValues to find NOT contained within actualValues
   */
  public void valuesNotContained(List<String> actualValues, List<String> expectedValues) {
    logger().traceEntry();
    List<String> values = new ArrayList<>(expectedValues);

    String errorMsg =
        String.format(
            "%n%s -  actual values contained %n%s -  unexpected values ", actualValues, values);

    Assert.assertFalse(errorMsg, actualValues.containsAll(values));
    logger().traceExit();
  }
  // </editor-fold>

  // <editor-fold desc="Wait Methods">
  /**
   * waits for a list to be populated
   *
   * @param elements list we will wait for to be populated
   * @param waitForSeconds time to wait for
   */
  public void waitForListLoad(List<WebElement> elements, Integer waitForSeconds) {
    logger().traceEntry();
    boolean elementIsEmpty = false;

    try {
      elementIsEmpty = elements.isEmpty();
      Assert.assertFalse(elementIsEmpty);

    } catch (AssertionError | Exception e) {
      AtomicInteger count = new AtomicInteger(0);

      while (elementIsEmpty && count.get() <= waitForSeconds) {
        try {
          elementIsEmpty = elements.isEmpty();
          Assert.assertFalse(elementIsEmpty);
        } catch (AssertionError | Exception ex) {
          logger()
              .info(
                  String.format(
                      "Waiting [%ss] for list to load", waitForSeconds - count.getAndIncrement()));
          sleep(1);
        }
      }
    }

    Assert.assertFalse(
        String.format("List did not load after waiting [%s]", waitForSeconds.toString()),
        elementIsEmpty);
    logger().traceExit();
  }

  /** seconds to sleep thread -> ONLY USE THIS WHEN ABSOLUTELY NECESSARY. KEEP AS PRIVATE!!! */
  private void sleep(int seconds) {
    logger().traceEntry();
    int sec = seconds * 1000;
    try {
      Thread.sleep(sec);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger().traceExit();
  }

  /**
   * waits for an element to become stale for 1 second. If it does not become stale it throws the
   * exception and continues.
   *
   * @param element to wait for staleness of
   */
  public void waitForStale(WebElement element, int seconds) {
    logger().traceEntry();
    fluentWait(seconds, 1)
        .until(
            ExpectedConditions.or(
                stalenessOf(element), ExpectedConditions.not(stalenessOf(element))));
    logger().traceExit();
  }

  /**
   * @param by to wait for invisibility of
   * @param seconds time to wait
   */
  public void waitForInvisibility(By by, int seconds) {
    logger().traceEntry();
    fluentWait(seconds, 1).until(ExpectedConditions.invisibilityOfElementLocated(by));
    logger().traceExit();
  }

  /**
   * @param element to wait for invisibility of
   * @param seconds time to wait
   */
  public void waitForInvisibility(WebElement element, int seconds) {
    logger().traceEntry();
    fluentWait(seconds, 1).until(invisibilityOfElement(element));
    logger().traceExit();
  }

  /**
   * An expectation for checking that an element is either invisible or not present on the DOM.
   *
   * @param element used to find the element
   * @return true if the element is not displayed or the element doesn't exist or stale element
   */
  public static ExpectedCondition<Boolean> invisibilityOfElement(final WebElement element) {
    logger().traceEntry();
    return driver -> {
      try {
        boolean isDisplayed = !element.isDisplayed();
        logger().traceExit(isDisplayed);
        return isDisplayed;
      } catch (NoSuchElementException | StaleElementReferenceException e) {
        logger().traceExit(true);
        return true;
      }
    };
  }

  /**
   * @param element the element to wait for
   * @param attribute attribute to use
   * @param attributeToBe attribute value we are looking to be contained
   * @param secondsToWait amount of time to wait
   */
  public void waitForAttributeToBeContained(
      WebElement element, String attribute, String attributeToBe, int secondsToWait) {
    logger().traceEntry();
    fluentWait(secondsToWait, 1)
        .until(ExpectedConditions.attributeContains(element, attribute, attributeToBe));
    logger().traceExit();
  }

  /**
   * @param element the element to wait for
   * @param attribute attribute to use
   * @param attributeToBe attribute value we are looking to NOT be contained
   * @param secondsToWait amount of time to wait
   */
  public void waitForAttributeToNotBeContained(
      WebElement element, String attribute, String attributeToBe, int secondsToWait) {
    logger().traceEntry();
    fluentWait(secondsToWait, 1)
        .until(not(ExpectedConditions.attributeContains(element, attribute, attributeToBe)));
    logger().traceExit();
  }

  /**
   * @param seconds seconds to wait
   * @param pollTime how often the condition should be evaluated
   * @return chain of returned wait. IMPORTANT! -> must have .until(ExpectedConditions) or it will
   *     not wait
   */
  public FluentWait<WebDriver> fluentWait(Integer seconds, Integer pollTime) {
    logger().traceEntry();
    assertWaitLimit(seconds);

    FluentWait<WebDriver> fluentWait =
        new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofSeconds(pollTime))
            .ignoring(AssertionError.class)
            .ignoring(IndexOutOfBoundsException.class)
            .ignoring(WebDriverException.class);

    if (seconds == 180)
      fluentWait.withMessage(
          "Time waited reached [3 minute] mark. Test was failed for taking too long.");

    logger()
        .info(
            String.format(
                "Waiting:[%ss] and pollingEvery:[%ss] for condition to be met", seconds, pollTime));
    logger().traceExit(fluentWait);
    return fluentWait;
  }

  private void assertWaitLimit(int seconds) {
    boolean timeToWaitIsLessThan3min = 0 < seconds && seconds < 181;

    if (!timeToWaitIsLessThan3min) {
      Assert.fail("Time waited needs to be greater than 0 and less than 3 minutes");
    }
  }
  // </editor-fold>
}
