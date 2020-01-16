package core.setup;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import static core.utilities.Tools.logger;

public class CreateSharedDrivers {

  /** initialize this class to create a driver if driver is not null */
  public CreateSharedDrivers() {
    if (Hooks.getDriver() == null) {
      createAndSetAddedDriver();
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

                          try {
                            driver.quit();
                          } catch (Exception e) {
                            driver = null;
                          }
                        })));
  }

  /**
   * creates a driver, adds it to to storedDrivers which sets the driver to the current driver being
   * added
   */
  private void createAndSetAddedDriver() {
    logger().traceEntry();
    createDriver();
    logger().traceExit();
  }

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
          Hooks.addDriver(new ChromeDriver(new ChromeOptions().addArguments("start-maximized")));
          Hooks.getDriver().manage().window().maximize();
          break;
        case "chromeHeadless":
          WebDriverManager.chromedriver().setup();
          System.setProperty("webdriver.chrome.silentOutput", "true");
          Hooks.addDriver(
              new ChromeDriver(
                  new ChromeOptions()
                      .setHeadless(true)
                      .addArguments("no-sandbox")
                      .addArguments("window-size=1920,1080")));
          Hooks.getDriver().manage().window().maximize();
          break;
        case "firefox":
          WebDriverManager.firefoxdriver().setup();
          System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "false");
          System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
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
        default:
          throw new IllegalStateException("Unexpected value: " + Config.getDeviceName());
      }
    }
  }
}
