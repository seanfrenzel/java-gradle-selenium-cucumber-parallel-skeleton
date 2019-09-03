package core.base;

import core.data.TestData;
import core.utilities.Tools;
import core.utilities.setup.Config;
import core.utilities.setup.Hooks;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static core.utilities.Tools.toCamelCase;
import static java.lang.String.format;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;

public abstract class PageObjectBase {
  public RemoteWebDriver driver;
  public TestData data;
  public SoftAssertions soft = Hooks.getSoftAssert();

  public PageObjectBase() {
    this.driver = Hooks.getDriver();
    this.data = Hooks.getTestData();
    setAjaxDecorator();
  }

  private void setAjaxDecorator() {
    AjaxElementLocatorFactory decorator = new AjaxElementLocatorFactory(driver, 3);
    PageFactory.initElements(decorator, this);
  }

  /** Modules Initialization */
  public ModuleInitializations module() {
    return new ModuleInitializations();
  }

  // <editor-fold desc="Action Methods">
  /** @param url to load with defined env */
  public void loadEnv(String url) {
    Assert.assertTrue("The ENV given was incorrect or not found", url.contains(Config.getEnv()));
    driver.get(url);
    assertEquals(url, driver.getCurrentUrl());
  }

  /** @param element to scroll into view */
  public void scrollIntoView(WebElement element) {
    driver.executeScript("arguments[0].scrollIntoView(true);", element);
  }

  public void jsClick(WebElement element) {
    JavascriptExecutor jse = driver;
    try {
      element.click();
    } catch (Exception e) {
      jse.executeScript("arguments[0].click();", element);
    }
  }

  public void jsSetValue(WebElement element, String value) {
    JavascriptExecutor js = driver;
    js.executeScript(format("arguments[0].value='%s';", value), element);
  }

  public void jsClear(WebElement element) {
    JavascriptExecutor js = driver;
    js.executeScript("arguments[0].value = '';", element);
  }

  /**
   * @param elems list of elements to click on
   * @param index index to start clicking from
   * @param amount how many times to click on elements Sequentially
   */
  public void clickSequentially(List<WebElement> elems, Integer index, Integer amount) {
    int clicked = 0;

    while (clicked < amount) {
      elems.get(index).click();
      clicked++;
      index++;
    }
  }

  /**
   * @param element element that we want to click on
   * @param amount how many times to click on element
   */
  public void clickMultiple(WebElement element, Integer amount) {
    int clicked = 0;

    while (clicked < amount) {
      element.click();
      clicked++;
    }
  }
  // </editor-fold>

  // <editor-fold desc="Get Methods">
  /**
   * @param fieldName Name of declared field on page that will get camel cased
   * @return Found field with param fieldName from class
   */
  private Object getField(String fieldName) {
    String target = toCamelCase(fieldName);
    Class aClass = null;

    try {
      aClass = getClass();
      Field field = aClass.getDeclaredField(target);
      field.setAccessible(true);
      return field.get(this);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          format("Element not found: [%s] in Class [%s]", target, aClass.getSimpleName()));
    }
  }

  /**
   * @param elementField Name of element field to find
   * @return returns found field as WebElement
   */
  public WebElement getElement(String elementField) {
    return (WebElement) getField(elementField);
  }

  /**
   * @param elementsField Name of element list field to find
   * @return returns found field as List<WebElement>
   */
  public List<WebElement> getElements(String elementsField) {
    return (List<WebElement>) getField(elementsField);
  }

  /**
   * @param elementFields list of element fields to get
   * @return returns found fields as List<WebElement>
   */
  public List<WebElement> getElements(List<String> elementFields) {
    return elementFields.stream().map(this::getElement).distinct().collect(Collectors.toList());
  }

  /**
   * Get element with text
   *
   * @param elements list of elements to search through
   * @param text text to look for in elements
   * @return element found with text
   */
  public WebElement getElementWithText(List<WebElement> elements, String text) {
    return elements.stream()
        .filter(elem -> elem.getText().trim().equalsIgnoreCase(text))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    format("Element Target Text was not found: [%s]", text)));
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
    return elements.stream()
        .filter(elem -> elem.getAttribute(attribute).trim().equalsIgnoreCase(attrValue))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    format("Element Attribute was not found: [%s]", attrValue)));
  }

  /**
   * Get elements with text
   *
   * @param elems the elements to iterate through and get with matching text
   * @param strings the text list to iterate through and get an element with the target text
   * @return the elements found with target text
   */
  public List<WebElement> getElementsFromTextList(List<WebElement> elems, List<String> strings) {
    return strings.stream()
        .map(text -> getElementWithText(elems, text))
        .distinct()
        .collect(Collectors.toList());
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
    fluentWait(waitSec, 1)
        .until(
            ExpectedConditions.or(
                ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)),
                ExpectedConditions.visibilityOf(element)));

    return element;
  }

  /**
   * Assert all Element Displayed and return found elements
   *
   * @param elements the WebElements we want to wait for to be displayed
   * @return elements to be chained off of EX:assertAllDisplayed(element, 5).get(0);
   */
  public List<WebElement> assertAllDisplayed(List<WebElement> elements) {
    return elements.stream()
        .map(element -> assertDisplayed(element, 1))
        .distinct()
        .collect(Collectors.toList());
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
    return element.getAttribute(attribute).contains(attrValue);
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
    return elements.stream().allMatch(elem -> elem.getAttribute(attribute).contains(attrValue));
  }

  /** @param element to verify as non existing */
  public void assertElementDoesNotExist(WebElement element) {
    try {
      element.isDisplayed();
      throw new IllegalArgumentException("Element was unexpectedly present");
    } catch (NoSuchElementException | IndexOutOfBoundsException | ElementNotVisibleException e) {
      // Element does not exist
    }
  }

  /**
   * @param elemFoundBy By to find element with. We have to use a By to avoid an unwanted wait with
   *     the global implicit for an element NOT EXISTING
   */
  public void assertElementDoesNotExist(By elemFoundBy) {
    try {
      driver.findElement(elemFoundBy).isDisplayed();
      throw new IllegalArgumentException("Element was unexpectedly present");
    } catch (NoSuchElementException | IndexOutOfBoundsException | ElementNotVisibleException e) {
      // Element does not exist
    }
  }

  /**
   * @param elements to search through
   * @param text to verify is not found within list
   * @return true that the text was not found
   */
  public boolean assertTextNotFound(List<WebElement> elements, String text) {
    Assert.assertTrue(
        format("Text was found: [%s]", text),
        elements.stream().noneMatch(element -> element.getText().equalsIgnoreCase(text)));
    return true;
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @return true that the text was found
   */
  public boolean assertTextFound(List<WebElement> elements, String text) {
    Assert.assertTrue(
        format("Text was not found: [%s]", text),
        elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
    return true;
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @return true that the text was found
   */
  public void assertTextFoundSoftly(List<WebElement> elements, String text) {
    assertIsTrueSoftly(
        format("Text was not found: [%s]", text),
        elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
  }

  /**
   * @param elements to search through
   * @param text to verify is found within list
   * @param errMsg custom error message to use
   * @return true that the text was found
   */
  public void assertTextFoundSoftly(List<WebElement> elements, String text, String errMsg) {
    assertIsTrueSoftly(
        errMsg, elements.stream().anyMatch(element -> element.getText().equalsIgnoreCase(text)));
  }

  /**
   * @param strings to search through
   * @param text to verify is not found within list
   */
  public void assertTextNotFoundInStringList(List<String> strings, String text) {
    Assert.assertTrue(
        format("Text was found: [%s]", text),
        strings.stream().noneMatch(string -> string.equalsIgnoreCase(text)));
  }

  /**
   * @param elements to search through
   * @param text substring to find in elements
   */
  public void assertSubstringFoundInList(List<WebElement> elements, String text) {
    List<String> targetText = Collections.singletonList(text);
    List<String> elementsText = Tools.buildStringListFromElemList(elements);
    List<String> substrings = Tools.buildSubstringTargetsList(elementsText, targetText);

    valuesContained(substrings, targetText);
  }

  /**
   * @param strings to search through
   * @param text to find contained within strings
   */
  public void assertTextContains(List<String> strings, String text) {
    Assert.assertTrue(
        format("List %s did not contain [%s]", strings, text),
        strings.stream().anyMatch(string -> string.contains(text)));
  }

  /**
   * @param strings to search through
   * @param text to find contained within strings
   */
  public void assertTextContainsSoftly(List<String> strings, String text) {
    assertIsTrueSoftly(
        format("List %s did not contain [%s]", strings, text),
        strings.stream().anyMatch(string -> string.contains(text)));
  }

  /**
   * assert the expected object equals the actual object
   *
   * @param expected
   * @param actual
   */
  public void assertEquals(Object expected, Object actual) {
    String errorMsg =
        format(
            "Expected and Actual were not equal%n%nExpected:[%s]%nActual:  [%s]%n%n",
            expected, actual);
    Assert.assertEquals(errorMsg, expected, actual);
  }

  /**
   * This is a soft assert that will only be hard failed by a soft.assertAll after the scenario has
   * been completed in the Hooks
   *
   * @param condition boolean condition that we want to verify is TRUE
   * @param errorMsg error message to use for this specifc soft assert if failed
   */
  public void assertIsTrueSoftly(String errorMsg, boolean condition) {
    String trace = null;

    if (!condition) {
      Hooks.embedScreenshot();
      Throwable throwable = new Throwable();
      trace =
          String.format(
              "[Class -> %s]%n[Method -> %s]%n[Line -> %d]",
              throwable.getStackTrace()[1].getClassName(),
              throwable.getStackTrace()[1].getMethodName(),
              throwable.getStackTrace()[1].getLineNumber());
    }
    soft.assertThat(condition).withFailMessage(format("%nError: %s%n%s", errorMsg, trace)).isTrue();
  }

  /**
   * @param actualValues list of strings to check against
   * @param expectedValues to find contained within actualValues
   */
  public void valuesContained(List<String> actualValues, List<String> expectedValues) {
    List<String> values = new ArrayList<>(expectedValues);

    String errorMsg =
        format(
            "%n%s - actual values did not contain %n%s - expected values ", actualValues, values);

    Assert.assertTrue(errorMsg, actualValues.containsAll(values));
  }

  /**
   * @param actualValues list of strings to check against
   * @param expectedValues to find NOT contained within actualValues
   */
  public void valuesNotContained(List<String> actualValues, List<String> expectedValues) {
    List<String> values = new ArrayList<>(expectedValues);

    String errorMsg =
        format("%n%s -  actual values contained %n%s -  unexpected values ", actualValues, values);

    Assert.assertFalse(errorMsg, actualValues.containsAll(values));
  }
  // </editor-fold>

  // <editor-fold desc="Wait Methods">
  /**
   * @param elements we will wait to be found
   * @param waitForSeconds time to wait for
   */
  public void waitForListLoad(List<WebElement> elements, Integer waitForSeconds) {
    try {
      Assert.assertFalse(elements.isEmpty());
    } catch (AssertionError | Exception e) {
      try {
        fluentWait(waitForSeconds, 1).until(ExpectedConditions.visibilityOf(elements.get(0)));
      } catch (Exception ex) {
        throw new IndexOutOfBoundsException(
            format("List did not load after waiting [%s]", waitForSeconds.toString()));
      }
    }
  }

  /**
   * @param by to wait for invisibility of
   * @param seconds time to wait
   */
  public void waitForInvisibility(By by, int seconds) {
    fluentWait(seconds, 1).until(ExpectedConditions.invisibilityOfElementLocated(by));
  }

  /**
   * @param element to wait for invisibility of
   * @param seconds time to wait
   */
  public void waitForInvisibility(WebElement element, int seconds) {
    fluentWait(seconds, 1).until(invisibilityOfElement(element));
  }

  /**
   * An expectation for checking that an element is either invisible or not present on the DOM.
   *
   * @param element used to find the element
   * @return true if the element is not displayed or the element doesn't exist or stale element
   */
  public static ExpectedCondition<Boolean> invisibilityOfElement(final WebElement element) {
    return driver -> {
      try {
        return !(element.isDisplayed());
      } catch (NoSuchElementException | StaleElementReferenceException e) {
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
    fluentWait(secondsToWait, 1)
        .until(ExpectedConditions.attributeContains(element, attribute, attributeToBe));
  }

  /**
   * @param element the element to wait for
   * @param attribute attribute to use
   * @param attributeToBe attribute value we are looking to NOT be contained
   * @param secondsToWait amount of time to wait
   */
  public void waitForAttributeToNotBeContained(
      WebElement element, String attribute, String attributeToBe, int secondsToWait) {
    fluentWait(secondsToWait, 1)
        .until(not(ExpectedConditions.attributeContains(element, attribute, attributeToBe)));
  }

  /**
   * @param seconds seconds to wait
   * @param pollTime how often the condition should be evaluated
   * @return chain of returned wait. IMPORTANT! -> must have .until(ExpectedConditions) or it will
   *     not wait
   */
  public FluentWait<WebDriver> fluentWait(Integer seconds, Integer pollTime) {
    assertWaitLimit(seconds);

    FluentWait<WebDriver> fluentWait =
        new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofSeconds(pollTime))
            .ignoring(WebDriverException.class);

    if (seconds == 180)
      fluentWait.withMessage(
          "Time waited reached [3 minute] mark. Test was failed for taking too long.");
    return fluentWait;
  }

  private void assertWaitLimit(int seconds) {
    boolean timeToWaitIsLessThan3min = 0 < seconds && seconds < 181;

    if (!timeToWaitIsLessThan3min) {
      Assert.fail("Time waited needs to be greater than 0 and less than 3 minutes");
    }
  }

  private void assertWaitLimitTimeout(int secondsWaited) {
    boolean timeLimitReached = secondsWaited == 180;

    if (timeLimitReached)
      Assert.fail(
          format(
              "Time waited [%s] reached [3 minute] mark. Test was failed for taking too long.",
              secondsWaited));
  }
  // </editor-fold>
}
