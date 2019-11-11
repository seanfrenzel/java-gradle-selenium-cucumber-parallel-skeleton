package core.test.steps;

import core.setup.Config;
import io.cucumber.java.en.And;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.utilities.Tools.logger;

public class HelpSteps {

  public HelpSteps(Config config) {
    logger().traceEntry();
    logger().traceExit();
  }

  @And("Print out project info to console")
  public void printOutProjectInfoToConsole() {
    logger().traceEntry();
    printCucumberTags();
    printFeatureTags();
    printCommandLineExamples();
    logger().traceExit();
  }

  /** tags to run for project */
  private static void printCucumberTags() {
    logger().traceEntry();
    List<String> properties = new ArrayList<>();
    properties.add("     * @NeatGifTest  --> Runs Neat GIF Test");
    properties.add("     * @Web          --> Run all web tests");

    System.out.printf(
        "%n--> Cucumber Tag options - Example: -Dcucumber.options=\"-t @NeatGifTest\"%n");
    properties.forEach(System.out::println);
    logger().traceExit();
  }

  /** sys property options to use for this project. Can be printed with help arg */
  private static void printCommandLineExamples() {
    logger().traceEntry();
    List<String> examples = new ArrayList<>();
    examples.add(
        "     * -DsystemPropertyHere=\"setHere\"" + "\n         > Set a system property\n");
    examples.add("     * -DdeviceName=\"setHere\"" + "\n         > Set the device/driver name\n");
    examples.add("     * -Dusername=\"setHere\"" + "\n         > Set username\n");
    examples.add("     * -Dpassword=\"setHere\"" + "\n         > Set password\n");
    examples.add("     * -DisRemote=\"setHere\"" + "\n         > Set if remote or not\n");
    examples.add(
        "     * -logLevel=\"trace\""
            + "\n         > Log level to be set. Default level is [WARN] and other options are: [all, debug, error, fatal, warn, info, off, trace]. Log files are saved under reports!!!\n");
    examples.add(
        "     * -Dcucumber.options=\"-t @tagsToRun\" clean cucumber"
            + "\n         > Set cucumber options\n");
    examples.add(
        "     * gradlew cucumber -Dusername=\"username\" -Dpassword=\"password\" -Denv=\"urlOrEnvHere\" -Dcucumber.options=\"-t @NeatGifTest\" clean"
            + "\n         > Run [cucumber] test with [system properties] and [cucumber options] with a different user than the one in json\n");

    System.out.printf("%n--> Command line use examples:%n");
    examples.forEach(System.out::println);
    logger().traceExit();
  }

  /** arg options to use for this project. Can be printed with help arg */
  private static void printFeatureTags() {
    logger().traceEntry();
    try (Stream<Path> walk =
        Files.walk(Paths.get(Config.WORKSPACE + "/src/test/resources/features/"))) {

      List<String> results =
          walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());

      System.out.printf("%n--> Possible full feature tags that can be used:%n");
      results.forEach(
          result -> {
            String string = "     * @" + result.substring(result.lastIndexOf("\\") + 1);

            System.out.println(string.substring(string.indexOf(" "), string.indexOf('.')));
          });
    } catch (IOException e) {
      logger().traceExit();
      e.printStackTrace();
    }
  }
}
