package com.zipup.server.funding.application;

import com.zipup.server.funding.dto.CrawlerResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;

@Service
@Slf4j
public class CrawlerService {

  @Value("${web-driver.chrome}")
  private String chromeDriver;

  @Value("${selenium.port}")
  private int seleniumPort;

  @Value("${selenium.host}")
  private String seleniumHost;

  @Value("${selenium.path}")
  private String seleniumPath;

  public CrawlerResponse crawlingProductInfo(String url) {
    WebDriver driver = setChromeDriver();

    try {
      driver.get(url);
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//meta[@property='og:image']")));

      WebElement ogTitleElement = driver.findElement(By.xpath("//meta[@property='og:title']"));
      String ogTitle = ogTitleElement.getAttribute("content");

      WebElement ogImageUrlElement = driver.findElement(By.xpath("//meta[@property='og:image']"));
      String ogImageUrl = ogImageUrlElement.getAttribute("content");

      return new CrawlerResponse(ogImageUrl, ogTitle);
    } catch(Exception ex){
      log.error(ex.getMessage());
      return null;
    } finally {
      driver.quit();
    }
  }

  private WebDriver setChromeDriver() {
    try {
      String seleniumUrl = seleniumHost + ":" + seleniumPort + seleniumPath;

      DesiredCapabilities capabilities = new DesiredCapabilities();
      capabilities.setBrowserName("chrome");
      return new RemoteWebDriver(new URL(seleniumUrl), capabilities);
    } catch (Exception ex) {
      log.info("setChromeDriver");
      log.error(ex.getMessage());
      return null;
    }
  }

}
