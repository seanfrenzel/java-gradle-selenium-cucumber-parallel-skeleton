package core.test.steps.common;

import core.test.pages.base.PageObjectBase;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.util.List;

import static core.utilities.Tools.logger;

public class CommonSteps extends PageObjectBase {

  @Override
  public void trait() {
    logger().traceEntry();
    logger().traceExit();
  } // <- ignore not used

  @Override
  public void navigateHere() {
    logger().traceEntry();
    logger().traceExit();
  } // <- ignore not used

  /*
  common_ is present on these steps for entering "common" in a feature file for easy viewing of common steps to use.

  You can remove this in the feature file and its not needed for the step!

  NOTE!!! -> elements must be `static` to use this
   */

  @And("(common_)clicks {string} on {string}")
  public void clicksOn(String elementField, String pageClassName) {
    logger().traceEntry();
    jsClick(getElement(elementField, pageClassName));
    logger().traceExit();
  }

  @And("(common_)clicks {string} sequentially {int} times from index {int} on {string}")
  public void clicksMultipleOn(
      String elementsField, int amount, int startClickingFrom, String pageClassName) {
    logger().traceEntry();
    clickSequentially(getElements(elementsField, pageClassName), startClickingFrom, amount);
    logger().traceExit();
  }

  @And("(common_)enters {string} text into {string} on {string}")
  public void entersTextInto(String text, String elementField, String pageClassName) {
    logger().traceEntry();
    getElement(elementField, pageClassName).sendKeys(text);
    logger().traceExit();
  }

  @And("(common_)fills in the following fields on {string}")
  public void fillsInTheFollowingFieldsOn(String pageClassName, DataTable table) {
    logger().traceEntry();
    fillDataTableFields(table, pageClassName);
    logger().traceExit();
  }

  @Then("(common_)verifies {string} equals the correct {string} on {string}")
  public void verifiesDisplaysTheCorrect(
      String elementField, String expectedValue, String pageClassName) {
    logger().traceEntry();

    assertDisplayed(getElement(elementField, pageClassName), 5);
    assertEquals(expectedValue, getElement(elementField, pageClassName).getText());
    logger().traceExit();
  }

  @Then("(common_)waits {int}s to verify {string} is displayed on {string}")
  public void waitsToVerifyIsDisplayedOn(int waitSec, String elementField, String pageClassName) {
    logger().traceEntry();
    assertDisplayed(getElement(elementField, pageClassName), waitSec);
    logger().traceExit();
  }

  @Then("(common_)verifies the following elements are displayed on {string}")
  public void verifiesTheFollowingElementsAreDisplayedOn(
      String pageClassName, List<String> elementFields) {
    logger().traceEntry();
    assertAllDisplayed(getElements(elementFields, pageClassName));
    logger().traceExit();
  }

  @Then("(common_)verifies {string} contains the following text values on {string}")
  public void verifiesContainsTheFollowingTextValuesOn(
      String elementsField, String pageClassName, List<String> values) {
    logger().traceEntry();

    getElementsFromTextList(getElements(elementsField, pageClassName), values);
    logger().traceExit();
  }

  @Then("(common_)verifies {string} is NOT displayed on {string}")
  public void verifiesIsNOTDisplayedOn(String elementField, String classname) {
    logger().traceEntry();
    assertElementDoesNotExist(getElement(elementField, classname));
    logger().traceExit();
  }
}
