package com.zipup.server.funding.application;

import com.zipup.server.funding.dto.CrawlerResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

  public List<CrawlerResponse> crawlingProductInfo(String url) {
    WebDriver driver = setChromeDriver();

    try {
      driver.get(url);

      WebElement ogTitleElement = driver.findElement(By.xpath("//meta[@property='og:title']"));
      String ogTitle = ogTitleElement.getAttribute("content");

      WebElement ogImageUrlElement = driver.findElement(By.xpath("//meta[@property='og:image']"));
      String ogImageUrl = ogImageUrlElement.getAttribute("content");

      List<CrawlerResponse> response = new ArrayList<>();

      response.add(new CrawlerResponse(ogImageUrl, ogTitle));

      return response;
    } catch(Exception ex){
      log.error(ex.getMessage());
      return null;
    } finally {
      driver.quit();
    }
  }

  private WebDriver setChromeDriver() {
    try {
//      String osName = System.getProperty("os.name").toLowerCase();
//      ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder();
//
//      if (osName.contains("mac"))
//        serviceBuilder.usingDriverExecutable(new File(chromeDriver));
//
//      else if (osName.contains("linux") && osName.contains("arm"))
//        serviceBuilder.usingDriverExecutable(new File("/usr/lib/chromium-browser/chromedriver"));
//
//      else if (osName.contains("linux"))
//        serviceBuilder.usingDriverExecutable(new File("/usr/local/bin/chromedriver"));
//
//      ChromeDriverService service = serviceBuilder.usingPort(9515).build();
//      service.start();
//
//      ChromeOptions options = new ChromeOptions()
//              .addArguments("--remote-allow-origins=*")
//              .addArguments("--headless") // headless 모드 활성화
//              .addArguments("--no-sandbox") // no-sandbox 옵션 추가
//              .addArguments("--disable-dev-shm-usage"); //  unknown error: session deleted because of page crash

//      return new ChromeDriver(service, options);
      String seleniumUrl = "http://" + seleniumHost + ":" + seleniumPort + seleniumPath;

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
