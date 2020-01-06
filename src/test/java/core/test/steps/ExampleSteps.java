package core.test.steps;

import core.test.pages.pages.ExamplePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ExampleSteps {

  private ExamplePage page;

  public ExampleSteps(ExamplePage page) {
    this.page = page;
  }

  @Given("the user navigates to Giphy")
  public void theUserNavigatesToTheSite() {
    page.navigateHere();
  }

  @Then("verifies {string} is displayed")
  public void verifiesIsDisplayed(String elementField) {
    page.assertDisplayed(page.getElement(elementField), 5);
  }

  @And("opens a Neat GIF")
  public void opensANeatGIF() {
    page.openNeatGif();
  }

  @And("enters {string} text into {string} on ExamplePage")
  public void entersTextIntoOnExamplePage(String text, String elementField) {
    page.getElement(elementField).sendKeys(text);
  }

  @And("clicks {string} on ExamplePage")
  public void clicksOnExamplePage(String elementField) {
    page.click(page.getElement(elementField));
  }

  @Then("waits {int}s to verify {string} is displayed on ExamplePage")
  public void waitsSToVerifyIsDisplayedOnExamplePage(int waitSec, String elementField) {
    page.assertDisplayed(page.getElement(elementField), waitSec);
  }
}
