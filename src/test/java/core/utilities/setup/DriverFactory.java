package core.utilities.setup;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class DriverFactory {
  private final URL url;
  private final DesiredCapabilities capabilities;

  public DriverFactory(String url, Map<String, Object> map) throws MalformedURLException {
    this.url = new URL(url);
    this.capabilities = new DesiredCapabilities(map);
  }

  RemoteWebDriver createDriver() {
    RemoteWebDriver remoteWebDriver = null;
    try {
      remoteWebDriver = new RemoteWebDriver(url, capabilities);
    } catch (UnreachableBrowserException e) {
      System.out.println(
          "\n---------------------------------------------------------------------------------\n"
              + "Make sure \"selenium-standalone start\" was used and the server is up!!!"
              + "\n---------------------------------------------------------------------------------\n");
      e.printStackTrace();
    }

    return remoteWebDriver;
  }
}
