package core.steps;

import core.pages.examplePage.ExamplePageWeb;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class ExampleSteps {

  private ExamplePageWeb page;

  public ExampleSteps(ExamplePageWeb page) {
    this.page = new ExamplePageWeb();
  }

  @Given("the user navigates to the website/app")
  public void theUserNavigatesToTheSite() {
    page.assertPagePresent();
  }

  @Then("searches for a neat gif")
  public void searchesForANeatGif() {
    page.openNeatGif();
  }

  @Then("verifies {string} is displayed")
  public void verifiesIsDisplayed(String elementField) {
    page.assertDisplayed(page.getElement(elementField), 5);
  }
}
