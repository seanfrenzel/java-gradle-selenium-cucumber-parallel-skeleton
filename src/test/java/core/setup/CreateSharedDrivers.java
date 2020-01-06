package core.setup;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import static core.utilities.Tools.logger;

public class CreateSharedDrivers {

  /** initialize this class to create a driver if driver is not null */
  public CreateSharedDrivers() {
    if (Hooks.getDriver() == null) {
      createDriver();
    }
  }

  /** quits all storedDrivers with a shutdown hook */
  static {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () ->
                    Hooks.storedDrivers.forEach(
                        driver -> {
                          logger()
                              .info(
                                  String.format(
                                      "Stored Driver Count: [%s]",
                                      String.valueOf(Hooks.storedDrivers.size())));

                          logger().info(String.format("Driver [%s] will be quit", driver));

                          driver.quit();
                        })));
  }

  /**
   * creates a driver and adds it to the storedDrivers. If you want to run this on a
   * selenium-standalone server then use -DisRemote="true" to create the RemoteWebDriver with given
   * capabilities from the json
   */
  private void createDriver() {
    if (Config.IS_REMOTE) {
      try {
        Hooks.addDriver(new RemoteWebDriver(Hooks.url, Hooks.capabilities));
        Hooks.getDriver().manage().deleteAllCookies();
      } catch (ElementNotInteractableException e) {
        // Ignore Exception
      }

    } else {
      logger().traceEntry();

      switch (Config.getDeviceName()) {
        case "chrome":
          WebDriverManager.chromedriver().setup();
          System.setProperty("webdriver.chrome.silentOutput", "true");
          Hooks.addDriver(new ChromeDriver());
          Hooks.getDriver().manage().window().maximize();
          break;
        case "firefox":
          WebDriverManager.firefoxdriver().setup();
          System.setProperty("webdriver.firefox.silentOutput", "true");
          Hooks.addDriver(new FirefoxDriver());
          Hooks.getDriver().manage().window().maximize();
          break;
        case "edge":
          WebDriverManager.edgedriver().setup();
          System.setProperty("webdriver.edge.silentOutput", "true");
          Hooks.addDriver(new EdgeDriver());
          Hooks.getDriver().manage().window().maximize();
          break;
        case "internet explorer":
          WebDriverManager.iedriver().setup();
          Hooks.addDriver(new InternetExplorerDriver());
          Hooks.getDriver().manage().window().maximize();
          break;
      }
    }
  }
}
