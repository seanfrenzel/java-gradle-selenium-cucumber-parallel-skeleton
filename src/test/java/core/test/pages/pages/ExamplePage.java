package core.test.pages.pages;

import core.setup.Config;
import core.test.pages.base.PageObjectBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ExamplePage extends PageObjectBase {

  @FindBy(css = "div.logo")
  private static WebElement logo;

  @FindBy(css = "div.search-input-wrapper > input")
  private static WebElement searchInput;

  @FindBy(css = "div.video-player-container.player-container")
  private static WebElement neatGif;

  @FindBy(css = "button.search-button")
  private static WebElement search;

  @FindBy(className = "grid-gfy-item")
  private static List<WebElement> gifs;

  @Override
  public void trait() {
    assertDisplayed(logo, 5);
  }

  @Override
  public void navigateHere() {
    loadEnv(Config.getEnv());
    trait();
  }

  public void openNeatGif() {
    assertDisplayed(gifs.get(0), 5).click();

    /**
     * DO NOT USE THIS. It is only meant to show the gif that we found and clicked on as a visual
     * since it is so fast :)
     */
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
