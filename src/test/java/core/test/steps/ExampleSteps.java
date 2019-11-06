package core.test.steps;

import core.test.pages.pages.ExamplePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ExampleSteps {

  private ExamplePage page;

  public ExampleSteps(ExamplePage page) {
    this.page = new ExamplePage();
  }

  @Given("the user navigates to the website/app")
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
}
