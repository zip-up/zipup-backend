package com.zipup.server.funding.application;

import com.zipup.server.funding.dto.CrawlerResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlerService {

  @Value("${web-driver.chrome}")
  private String chromeDriver;

  public List<CrawlerResponse> crawlingProductInfo(String url) {
    System.setProperty("webdriver.chrome.driver", chromeDriver);

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    options.addArguments("--headless");
    WebDriver driver = new ChromeDriver(options);

    driver.get(url);

    WebElement ogTitleElement = driver.findElement(By.xpath("//meta[@property='og:title']"));
    String ogTitle = ogTitleElement.getAttribute("content");

    WebElement ogImageUrlElement = driver.findElement(By.xpath("//meta[@property='og:image']"));
    String ogImageUrl = ogImageUrlElement.getAttribute("content");

    List<CrawlerResponse> response = new ArrayList<>();

    response.add(new CrawlerResponse(ogImageUrl, ogTitle));

    driver.quit();

    return response;
  }
}
