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

import static core.utilities.Tools.getDate;
import static core.utilities.Tools.logger;
import static java.lang.String.format;

public class Hooks {

  public Scenario currentScenario;
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
   * @param scenario current scenario instance
   * @throws MalformedURLException
   */
  @Before(order = 1)
  public void beforeScenario(Scenario scenario) throws MalformedURLException {
    if (!scenario.getName().equals("Help")) {
      Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
      manageResults();

      config = new Config();
      config.setLogLevel(config.logLevel);
      config.setCapabilities();
      softAssert = new SoftAssertions();
      setupEnvironment(scenario);

      if (config.parallel)
        System.out.printf(
            "[Thread %2d] Running -> [Scenario: %s]%n",
            Thread.currentThread().getId(), currentScenario.getName());

      new CreateSharedDrivers();
      logger().traceExit();
    }
  }

  /**
   * logic performed after scenario is complete
   *
   * @param scenario current scenario instance
   */
  @After(order = 2)
  public void afterScenario(Scenario scenario) {
    logger().traceEntry();

    try {
      if (scenario.isFailed()) {

        if (config.parallel) {
          System.out.printf(
              "[Thread %2d] Running -> [Scenario: %s] - FAILED%n",
              Thread.currentThread().getId(), currentScenario.getName());
        }

        takeScreenshot();
      }

      if (!softAssert.errorsCollected().isEmpty()) softAssert.assertAll();

    } finally {

      if (config.parallel && !scenario.isFailed()) {
        System.out.printf(
            "[Thread %2d] Running -> [Scenario: %s] - PASSED%n",
            Thread.currentThread().getId(), currentScenario.getName());
      }

      if (getDriver() != null) {
        getDriver().manage().deleteAllCookies();
        getDriver().executeScript("window.sessionStorage.clear();");
        getDriver().executeScript("window.localStorage.clear();");
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
   * @param scenario instanced use of Scenario to get state of current scenario
   * @throws MalformedURLException
   */
  private void setupEnvironment(Scenario scenario) throws MalformedURLException {
    if (!setup) {
      currentScenario = scenario;

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
  public void takeScreenshot() {
    logger().traceEntry();
    fileScreenshot();
    embedScreenshot();
    logger().traceExit();
  }

  /** takes screenshot in file format */
  private void fileScreenshot() {
    logger().traceEntry();
    try {
      File scrFile = ((TakesScreenshot) Hooks.getDriver()).getScreenshotAs(OutputType.FILE);
      String fileName =
          format(
                  "./TestResults/ScreenShots/Feature_%s_Line%s_Time[%s].png",
                  currentScenario.getName(), currentScenario.getLine(), getDate("hh-mm-ss", 0))
              .replaceAll(" ", "-");

      FileUtils.copyFile(scrFile, new File(fileName));
    } catch (WebDriverException | IOException | NullPointerException e) {
      System.out.println("Failed to take file Screenshot");
    }
  }

  /** takes screenshot and embeds it */
  private void embedScreenshot() {
    logger().traceEntry();
    try {
      final byte[] screenshot =
          ((TakesScreenshot) Hooks.getDriver()).getScreenshotAs(OutputType.BYTES);
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
      logger().traceExit();
    }
  }

  /** skips a scenario if not valid for current run */
  private void skipScenario(Scenario scenario, String errorReason) {
    logger().traceEntry();
    try {
      Assume.assumeTrue(false);
    } catch (AssumptionViolatedException e) {
      throw new AssumptionViolatedException(
          Tools.border("- Scenario: %s%n- Was skipped for: %s", scenario.getName(), errorReason));
    }
    logger().traceExit();
  }
  // </editor-fold>

  // <editor-fold desc="@Tag Hooks">
  /** Skip Scenario if Tagged @wip */
  @Before("@wip")
  public void wipSkip(Scenario scenario) {
    logger().traceEntry();
    skipScenario(scenario, "being @wip");
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
    logger().info(String.format("Created and Added Driver: [%s]", Hooks.getDriver()));
  }

  public static RemoteWebDriver getDriver() {
    logger().traceEntry();
    logger().traceExit(drivers);
    return drivers.get();
  }
  // </editor-fold>
}
