package core.setup;

import core.test.data.TestData;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.openqa.grid.internal.utils.configuration.StandaloneConfiguration;
import org.openqa.grid.selenium.GridLauncherV3;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.server.SeleniumServer;
import org.seleniumhq.jetty9.util.log.JettyLogHandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static core.utilities.Tools.getDate;
import static core.utilities.Tools.logger;
import static java.lang.String.format;

public class Hooks {
  private static Scenario currentScenario;
  private boolean setup = false;
  private static boolean reportsCreated = false;
  private static boolean isFirstServerRun = false;
  private static RemoteWebDriver driver;
  private Config config;
  private static TestData testData;
  private static SoftAssertions softAssert;
  private URL url;
  private DesiredCapabilities capabilities;

  public Hooks() {
    logger().traceEntry();
    logger().traceExit();
  }

  /**
   * logic performed before scenario start
   *
   * @param scenario current scenario instance
   * @throws MalformedURLException
   */
  @Before(order = 1)
  public void beforeAll(Scenario scenario) throws MalformedURLException {
    if (!scenario.getName().equals("Help")) {
      manageResults();

      config = new Config();
      config.setLogLevel(config.logLevel);
      config.setCapabilities();
      softAssert = new SoftAssertions();

      setupEnvironment(scenario);
      startStandaloneServerIfLocal();
      createDriver();
      logger().traceExit();
    }
  }

  /**
   * logic performed after scenario is complete
   *
   * @param scenario current scenario instance
   */
  @After(order = 1)
  public void afterAll(Scenario scenario) {
    logger().traceEntry();
    try {

      if (!scenario.getName().equals("Help")) {
        if (scenario.isFailed()) takeScreenshot();
        if (!softAssert.errorsCollected().isEmpty()) softAssert.assertAll();
      }

    } finally {

      if (driver != null) {
        logger().info(String.format("Driver:[%s] was quit", driver));
        driver.quit();
      }

      setup = false;
      logger().traceExit();
    }
  }

  // <editor-fold desc="Non Annotation Methods">
  /**
   * Sets up environment and capabilities for given properties and data.
   *
   * @param scenario instanced use of Scenario to get state of current scenario
   * @throws MalformedURLException
   */
  private void setupEnvironment(Scenario scenario) throws MalformedURLException {
    if (!setup) {
      currentScenario = scenario;

      this.url = new URL(config.getUrl());
      logger().info(String.format("URL is:%s", this.url));

      this.capabilities = new DesiredCapabilities(config.getCapabilities());
      logger().info(String.format("Capabilities are:%s", this.capabilities));

      setup = true;
    }

    testData = new TestData(Config.USER);
    logger().traceExit();
  }

  /** starts up the selenium-server-standalone */
  private void startStandaloneServerIfLocal() {
    boolean firstRunAndNotRemote = !isFirstServerRun && !Config.IS_REMOTE;

    if (firstRunAndNotRemote) {
      logger().traceEntry();

      DriverManagerType driverType;
      String deviceName = Config.getDeviceName().toUpperCase();

      try {
        driverType = DriverManagerType.valueOf(deviceName);

      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format(
                "Incorrect [%s] Driver type! Must be one of the following:%n*   %s%n*   %s%n*   %s%n*   %s%n*   %s%n*   %s%n*   %s%n",
                deviceName,
                "chrome",
                "firefox",
                "opera",
                "edge",
                "phantomjs",
                "iexplorer",
                "selenium_server_standalone"));
      }

      WebDriverManager.getInstance(driverType).setup();

      SeleniumServer server = new SeleniumServer(new StandaloneConfiguration());

      if (!server.isStarted()) {
        GridLauncherV3.main(new String[] {});
        isFirstServerRun = true;
        JettyLogHandler.config();
        silenceDriverLogs();
      }

      logger().traceExit(server.getUrl());
    }
  }

  private void silenceDriverLogs() {
    switch (Config.getDeviceName().toLowerCase()) {
      case "chrome":
        System.setProperty("webdriver.chrome.silentOutput", "true");
        break;
      case "firefox":
        System.setProperty("webdriver.firefox.marionette", "true");
        System.setProperty("webdriver.firefox.logfile", "/dev/null");
        break;
    }
  }

  /** creates diver with given capabilities */
  private void createDriver() {
    logger().traceEntry();

    setDriver(new RemoteWebDriver(url, capabilities));
    logger().traceExit();
  }

  /** takes screenshot in multiple formats */
  public static void takeScreenshot() {
    logger().traceEntry();

    fileScreenshot();
    embedScreenshot();

    logger().traceExit();
  }

  /** takes screenshot in file format */
  private static void fileScreenshot() {
    logger().traceEntry();

    try {
      File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
      String fileName =
          format(
                  "./TestResults/ScreenShots/Feature_%s_Line%s_Time[%s].png",
                  currentScenario.getName(), currentScenario.getLine(), getDate("hh-mm-ss", 0))
              .replaceAll(" ", "-");

      FileUtils.copyFile(scrFile, new File(fileName));
    } catch (WebDriverException | IOException | NullPointerException e) {
      System.out.println("Failed to take file Screenshot");
    }

    logger().traceExit();
  }

  /** takes screenshot and embeds it */
  public static void embedScreenshot() {
    logger().traceEntry();

    try {
      final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      currentScenario.embed(screenshot, "image/png");
    } catch (WebDriverException | NullPointerException e) {
      System.out.println("Failed to take embed Screenshot");
    }

    logger().traceExit();
  }

  /** removes previously created reports and temp files */
  private void manageResults() {
    logger().traceEntry();

    if (!reportsCreated) {
      try {
        FileUtils.deleteDirectory(new File("./TestResults"));
      } catch (IOException e) {
        // Ignored files not present
      }

      try {
        FileUtils.forceMkdir(new File("./TestResults/ScreenShots"));
      } catch (IOException e) {
        System.err.println("Failed to create TestResults file directory");
      }
      reportsCreated = true;
    }

    logger().traceExit();
  }
  // </editor-fold>

  // <editor-fold desc="Get And Sets">
  public static SoftAssertions getSoftAssert() {
    logger().traceEntry();
    logger().traceExit(softAssert);
    return softAssert;
  }

  public static TestData getTestData() {
    logger().traceEntry();
    logger().traceExit(testData);
    return testData;
  }

  public static RemoteWebDriver getDriver() {
    logger().traceEntry();
    logger().traceExit(driver);
    return driver;
  }

  private static void setDriver(RemoteWebDriver driver) {
    logger().traceEntry();
    logger().traceExit(driver);
    Hooks.driver = driver;
  }
  // </editor-fold>
}
