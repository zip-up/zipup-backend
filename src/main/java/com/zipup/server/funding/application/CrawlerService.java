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
    WebDriver driver = new ChromeDriver(options);

    driver.get(url);

    String IMAGE_XPATH = "//img[@class='rf-configuration-hero-image']";
    String TITLE_XPATH = "//h1";
    String PRICE_XPATH = "//span[@class='rc-prices-fullprice']";

    List<WebElement> images = driver.findElements(By.xpath(IMAGE_XPATH));
    List<WebElement> titles = driver.findElements(By.xpath(TITLE_XPATH));
    List<WebElement> prices = driver.findElements(By.xpath(PRICE_XPATH));

    List<CrawlerResponse> response = new ArrayList<>();

    final String[] imageUrl = {""};
    final String[] title = {""};
    final int[] price = {0};

    prices.forEach(p -> price[0] = Integer.parseInt(p.getText().substring(1).replaceAll(",", "")));
    images.forEach(image -> imageUrl[0] = image.getAttribute("src"));
    titles.forEach(t -> title[0] = t.getText());

    response.add(new CrawlerResponse(imageUrl[0], title[0], price[0]));

    driver.quit();

    return response;
  }
}
