package core.test.pages.modules;

import core.test.pages.base.PageObjectBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ExampleModuleWeb extends PageObjectBase {

  // <editor-fold desc="Page Elements">
  @FindBy(css = "placeholder")
  private WebElement moduleTrait;
  // </editor-fold>

  public void trait() {
    assertDisplayed(moduleTrait, 1);
  }

  public void navigateHere() {
    // use actions to get to this module here. leave blank if its not something that can be
    // navigated too
    trait();
  }
}
