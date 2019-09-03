package core.pages.examplePage;

import core.base.PageObjectBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ExamplePageWeb extends PageObjectBase {

  @FindBy(css = "div.logo")
  private WebElement logo;

  @FindBy(css = "div.search-input-wrapper > input")
  private WebElement searchInput;

  @FindBy(css = "div.video-player-container.player-container")
  private WebElement neatGif;

  @FindBy(css = "button.search-button")
  private WebElement search;

  @FindBy(className = "grid-gfy-item")
  private List<WebElement> gifs;

  public void assertPagePresent() {
    assertDisplayed(logo, 5);
  }

  public void openNeatGif() {
    searchInput.sendKeys("Neat");
    search.click();
    waitForListLoad(gifs, 5);
    gifs.get(0).click();

    // DO NOT USE THIS. It is only meant to show the gif that we found and clicked on as a visual
    // since it is so fast :)
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
