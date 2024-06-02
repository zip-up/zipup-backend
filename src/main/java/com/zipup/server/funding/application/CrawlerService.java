package com.zipup.server.funding.application;

import com.zipup.server.funding.dto.CrawlerResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Service
@Slf4j
public class CrawlerService {

  @Value("${selenium.port}")
  private int seleniumPort;

  @Value("${selenium.host}")
  private String seleniumHost;

  @Value("${selenium.path}")
  private String seleniumPath;

  public CrawlerResponse crawlingProductInfo(String url) {
    WebDriver driver = setChromeDriver();

    try {
      if (driver == null) throw new RuntimeException("chorme driver is null");
      driver.get(url);
      WebElement ogTitleElement = driver.findElement(By.xpath("//meta[@property='og:title']"));
      String ogTitle = ogTitleElement.getAttribute("content");

      WebElement ogImageUrlElement = driver.findElement(By.xpath("//meta[@property='og:image']"));
      String ogImageUrl = ogImageUrlElement.getAttribute("content");

      return new CrawlerResponse(ogImageUrl, ogTitle);
    } catch(Exception ex) {
      log.error(ex.getMessage());
      return null;
    } finally {
      Objects.requireNonNull(driver).quit();
    }
  }

  private WebDriver setChromeDriver() {
    String seleniumUrl = seleniumHost + ":" + seleniumPort + seleniumPath;

    try {
      URL url = new URL(seleniumUrl);
      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setBrowserName("chrome");

      return new RemoteWebDriver(url, capabilities);
    } catch (MalformedURLException ex) {
      log.error("Invalid Selenium URL :: " + seleniumUrl);
      log.error(ex.getMessage());
    } catch (Exception ex) {
      log.error("Failed to create WebDriver for Selenium URL :: " + seleniumUrl);
      log.error(ex.getMessage());
    }

    return null;
  }
}
