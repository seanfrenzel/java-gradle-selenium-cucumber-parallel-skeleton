package core.setup;

import core.test.data.TestData;
import core.utilities.Tools;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static core.utilities.Tools.logger;

public class Hooks {

  public static ThreadLocal<Scenario> scenarios = new ThreadLocal<>();
  private boolean setup = false;
  private static boolean reportsCreated = false;

  private Config config;
  private static TestData testData;
  private static SoftAssertions softAssert;

  static URL url;
  static DesiredCapabilities capabilities;

  // drivers and storedDrivers needed for parallel runs with multiple threads.
  private static ThreadLocal<RemoteWebDriver> drivers = new ThreadLocal<>();
  static List<RemoteWebDriver> storedDrivers = new ArrayList<>();

  public Hooks() {
    logger().traceEntry();
    logger().traceExit();
  }

  /**
   * logic performed before scenario start
   *
   * @throws MalformedURLException
   */
  @Before(order = 1)
  public void beforeScenario(Scenario scenario) throws MalformedURLException {
    Hooks.setScenario(scenario);
    Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
    manageResults();

    config = new Config();
    config.setLogLevel(config.logLevel);
    config.setCapabilities();
    softAssert = new SoftAssertions();
    setupEnvironment();

    if (config.parallel)
      System.out.printf(
          "[Thread %2d] Running -> [Scenario: %s]%n",
          Thread.currentThread().getId(), Hooks.getScenario().getName());

    new CreateSharedDrivers();
    logger().traceExit();
  }

  /** logic performed after scenario is complete */
  @After(order = 2)
  public void afterScenario() {
    logger().traceEntry();

    try {
      if (!softAssert.errorsCollected().isEmpty()) softAssert.assertAll();

      if (Hooks.getScenario().isFailed()) {
        if (config.parallel) {
          System.out.printf(
              "[Thread %2d] Running -> [Scenario: %s] - FAILED - (*_*)%n",
              Thread.currentThread().getId(), Hooks.getScenario().getName());
        }
        takeScreenshot();
      }

      if (!softAssert.errorsCollected().isEmpty()) softAssert.assertAll();

    } finally {

      if (config.parallel && !Hooks.getScenario().isFailed()) {
        System.out.printf(
            "[Thread %2d] Running -> [Scenario: %s] - PASSED - (^_^)%n",
            Thread.currentThread().getId(), Hooks.getScenario().getName());
      }

      RemoteWebDriver driver = getDriver();

      if (driver != null) {
        try {
          driver.executeScript("window.sessionStorage.clear();");
          driver.executeScript("window.localStorage.clear();");
          driver.manage().deleteAllCookies();
        } catch (Exception e) {
          System.out.printf(
              "[Thread %2d] Driver [%s] will be quit. Storage clear failed",
              Thread.currentThread().getId(), driver);
          Hooks.storedDrivers.remove(driver);
          driver.quit();
        }
      }

      /* drivers are shutdown when the test run is completed from shutdown hook in CreateSharedDrivers */
      setup = false;
      logger().traceExit();
    }
  }

  // <editor-fold desc="Non Annotation Methods">

  /**
   * Sets up environment and capabilities for given properties and data.
   *
   * @throws MalformedURLException
   */
  private void setupEnvironment() throws MalformedURLException {
    if (!setup) {

      url = new URL(config.getUrl());
      logger().trace(String.format("URL is:%s", url));

      capabilities = new DesiredCapabilities(config.getCapabilities());
      logger().trace(String.format("Capabilities are:%s", capabilities));

      setup = true;
    }

    testData = new TestData(Config.USER);

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
      File scrFile = ((TakesScreenshot) Hooks.getDriver()).getScreenshotAs(OutputType.FILE);
      String fileName =
          String.format(
                  "./TestResults/ScreenShots/Feature_%s_Line%s_Time[%s].png",
                  Hooks.getScenario().getName(),
                  Hooks.getScenario().getLine(),
                  Tools.getDate("hh-mm-ss", 0))
              .replaceAll(" ", "-");

      FileUtils.copyFile(scrFile, new File(fileName));
    } catch (WebDriverException | IOException | NullPointerException e) {
      System.out.println("Failed to take file Screenshot");
    }
  }

  /** takes screenshot and embeds it */
  public static void embedScreenshot() {
    logger().traceEntry();
    try {
      final byte[] screenshot =
          ((TakesScreenshot) Hooks.getDriver()).getScreenshotAs(OutputType.BYTES);
      Hooks.getScenario().embed(screenshot, "image/png");
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
      } catch (IOException | IllegalArgumentException e) {
        // Ignored files not present
      }

      try {
        FileUtils.forceMkdir(new File("./TestResults/ScreenShots"));
      } catch (IOException | IllegalArgumentException e) {
        System.err.println("Failed to create TestResults file directory");
      }

      reportsCreated = true;
      logger().traceExit();
    }
  }

  /** skips a scenario if not valid for current run */
  private void skipScenario(String errorReason) {
    logger().traceEntry();
    try {
      Assume.assumeTrue(false);
    } catch (AssumptionViolatedException e) {
      throw new AssumptionViolatedException(
          Tools.border(
              "- Scenario: %s%n- Was skipped for: %s", Hooks.getScenario().getName(), errorReason));
    }
    logger().traceExit();
  }
  // </editor-fold>

  // <editor-fold desc="@Tag Hooks">
  /** Skip Scenario if Tagged @wip */
  @Before("@wip")
  public void wipSkip() {
    logger().traceEntry();
    skipScenario("being @wip");
    logger().traceExit();
  }
  // </editor-fold>-

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

  public static void addDriver(RemoteWebDriver driver) {
    storedDrivers.add(driver);
    drivers.set(driver);
    System.out.printf(
        "[Thread %2d] Created and Added Driver: [%s]%n",
        Thread.currentThread().getId(), Hooks.getDriver());
  }

  public static RemoteWebDriver getDriver() {
    logger().traceEntry();
    logger().traceExit(drivers);
    return drivers.get();
  }

  public static void setScenario(Scenario scenario) {
    scenarios.set(scenario);
  }

  public static Scenario getScenario() {
    return scenarios.get();
  }
  // </editor-fold>
}
