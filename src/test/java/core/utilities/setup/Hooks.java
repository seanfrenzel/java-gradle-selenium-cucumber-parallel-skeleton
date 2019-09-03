package core.utilities.setup;

import core.data.TestData;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static core.utilities.Tools.getDate;
import static java.lang.String.format;

public class Hooks {
  private boolean setup = false;
  private static boolean reportsCreated = false;
  private DriverFactory factory;
  private static RemoteWebDriver driver;
  private Config config = new Config();
  private static TestData testData;
  private static SoftAssertions softAssert;
  private static Scenario currentScenario;

  public static SoftAssertions getSoftAssert() {
    return softAssert;
  }

  public static TestData getTestData() {
    return testData;
  }

  public static void setTestData(TestData testData) {
    Hooks.testData = testData;
  }

  public Hooks() {
    setDriver(driver);
    softAssert = new SoftAssertions();
  }

  public static RemoteWebDriver getDriver() {
    return driver;
  }

  private static void setDriver(RemoteWebDriver driver) {
    Hooks.driver = driver;
  }

  @Before(order = 1)
  public void beforeAll() throws MalformedURLException {
    if (!setup) {
      factory = new DriverFactory(config.getUrl(), config.getCapabilities());
      setup = true;
    }

    setTestData(new TestData(Config.USER));
    setDriver(factory.createDriver());
    try {
      driver.get(Config.env);
    } catch (Exception e) {
      System.out.println(
          "\n---------------------------------------------------------------------------------\n"
              + "Make sure \"selenium-standalone start\" was used and the server is up!!!"
              + "\n---------------------------------------------------------------------------------\n");
      e.printStackTrace();
    }
    manageResults();
  }

  @After(order = 1)
  public void afterAll(Scenario scenario) {
    boolean driverNotNull = driver != null;

    try {
      if (scenario.isFailed()) takeScreenshot();
      if (!softAssert.errorsCollected().isEmpty()) softAssert.assertAll();

    } finally {
      setup = false;
      if (driverNotNull) driver.quit();
    }
  }

  public static void takeScreenshot() {
    fileScreenshot();
    embedScreenshot();
  }

  private static void fileScreenshot() {
    try {
      File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
      String fileName =
          format(
                  "./TestResults/ScreenShots/Feature_%s_Line%s_Time[%s].png",
                  currentScenario.getName(), currentScenario.getLines(), getDate("hh-mm-ss", 0))
              .replaceAll(" ", "-");

      FileUtils.copyFile(scrFile, new File(fileName));
    } catch (WebDriverException | IOException | NullPointerException e) {
      System.out.println("Failed to take file Screenshot");
    }
  }

  public static void embedScreenshot() {
    try {
      final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      currentScenario.embed(screenshot, "image/png");
    } catch (WebDriverException | NullPointerException e) {
      System.out.println("Failed to take embed Screenshot");
    }
  }

  private void manageResults() {
    if (!reportsCreated) {
      try {
        FileUtils.deleteDirectory(new File("./TestResults"));
      } catch (IOException e) {
        // Ignored files not present
      }

      try {
        FileUtils.forceMkdir(new File("./TestResults/ScreenShots"));
        FileUtils.forceMkdir(new File("./TestResults/TempDownloads"));
      } catch (IOException e) {
        System.err.println("Failed to create TestResults file directory");
      }

      reportsCreated = true;
    }
  }
}
