package com.bms.find;

import com.bms.config.ConfigReader;
import com.bms.heroku.HerokuApplication;
import com.bms.notify.NotifyUser;
import com.bms.find.SearchForMovie.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchForMovie {

    private static Logger logger = LoggerFactory.getLogger(SearchForMovie.class);

    private int count = 0;

    private String key;

    private boolean isTaskDone =false;

    private ChromeDriver driver;

    private  WebDriverWait wait ;

    private String savedURL=null;

    public boolean searchWithDetails() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--allow-file-access-from-files",
                "--use-fake-ui-for-media-stream",
                "--allow-file-access",
                "--use-file-for-fake-audio-capture=/Users/subrat.thakur/Downloads/avengers-endgame-ringtone.mp3",
                "--use-fake-device-for-media-stream",
                "--use-file-for-fake-audio-capture=/Users/subrat.thakur/Downloads/avengers-endgame-ringtone.mp3");
        driver  = new ChromeDriver(options);
        wait =  new WebDriverWait(driver, 40);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logger.warn("================== Search Started "+dtf.format(now)+" ==========================");
        if(searchForURL(key) !=null) {
            initiateUrlLevelSearch(true);
        } else{
            initiateDefaultSearch();
        }
        wait = null;
        driver.close();
        now = LocalDateTime.now();
        logger.warn("================== Search End "+dtf.format(now)+" ==========================");

        return isTaskDone;
    }

    private void initiateDefaultSearch() {
        logger.warn("Default Level Search initiated");

        boolean isCategoryAvailable = false;
        driver.navigate().to(ConfigReader.getProperty("MOVIE_URL"));

        driver.manage().window().maximize();

        waitForJStoLoad();

        logger.warn("Check for Confirmation window for personalize experience");
        if(driver.findElements(By.id("wzrk-confirm")).size()>0) {
            driver.findElement(By.id("wzrk-confirm")).click();
        }

        // Click the "Movies" button (after it's loaded)
        wait.until(ExpectedConditions.elementToBeClickable(By.className("more-showtimes")));
        driver.findElement(By.className("more-showtimes")).click();

        String movcat= "//*[@id=\"lang-English\"]/div[2]/a";

        List<WebElement> moviescategoryList = driver.findElements(By.xpath(movcat));

        // Print the languages
        for (WebElement categoryElement : moviescategoryList) {
            String categoryName = categoryElement.getText();
            if(categoryName.equals(ConfigReader.getProperty("MOVIE_SCREEN_CATEGORY"))) {
                categoryElement.click();
                isCategoryAvailable = true;
                break;
            }
        }

        if(isCategoryAvailable) {
            //Wait Page to load properly
            wait.until(ExpectedConditions.elementToBeClickable(By.className("phpShowtimes")));
            String currentURL = driver.getCurrentUrl();
            validateAndSaveURL(currentURL);
        }
    }

    private void validateAndSaveURL(String currentURL) {
        if(currentURL != null) {
            int index=currentURL.lastIndexOf('/');
            String baseUrl = currentURL.substring(0,index);
            savedURL = baseUrl;
            logger.warn("Base URL is "+baseUrl);
            initiateUrlLevelSearch(false);
        }
    }


    private void initiateUrlLevelSearch(boolean isDirect) {
        logger.warn("URL Level Search initiated");

        String searchDate = ConfigReader.getProperty("SEARCH_DATE");
        logger.warn("Search for Date: "+searchDate);
        driver.navigate().to(savedURL+"/"+searchDate);
        wait.until(ExpectedConditions.elementToBeClickable(By.className("phpShowtimes")));

        if(isDirect) {
            logger.warn("Check for Select City window ");
            driver.findElement(By.xpath("//*[@id=\"navbar\"]/div[2]/div[2]/div[2]/div[2]/ul/li[4]/div/div[3]/div[2]/ul/li[4]/a")).click();
        }
        String currentURL = driver.getCurrentUrl();
        int index=currentURL.lastIndexOf('/');
        String urlDate = currentURL.substring(index+1,currentURL.length());
        logger.warn("Date in currentURL: "+urlDate);
        if(urlDate.equals(searchDate)) {
            searchForTheater();
        }

    }

    private void searchForTheater() {
        String theaterClassName = "__venue-name";
        List<WebElement> theaterNameList = driver.findElements(By.className(theaterClassName));
        List<String> subscriber = getNumbers();
        for (WebElement theater : theaterNameList) {
            String theaterName = theater.getText();
            String theaterExpectedName = ConfigReader.getProperty("SEARCH_UNIQUE_THEATER_NAME");
            if(theaterName.contains(theaterExpectedName)) {
                logger.warn(theaterName);
                for(String num: subscriber) {
                    logger.warn("Notifying User with Phone Number"+ num);
                    NotifyUser.getInstance().notifyUser(driver, wait, num, createMsgToSend(theaterName));

                    driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);
                    isTaskDone= true;
                }
            }
        }
    }

    private String searchForURL(String key) {
        return savedURL;
    }

    private List<String> getNumbers() {
        List<String> subscriber = new ArrayList<String>();
        String subscriber_number = ConfigReader.getProperty("SUBSCRIBERS_NUMBER");
        String[] numbers =subscriber_number.split(",");
        for(int i=0;i<numbers.length;i++){
            if(isValidMobileNumber(numbers[i])){
                subscriber.add(numbers[i]);
            }
        }
        return subscriber;
    }

    private boolean isValidMobileNumber(String num){
        // The given argument to compile() method
        // is regular expression. With the help of
        // regular expression we can validate mobile
        // number.
        // 1) Begins with 0 or 91
        // 2) Then contains 7 or 8 or 9.
        // 3) Then contains 9 digits
        Pattern p = Pattern.compile("(0/91)?[6-9][0-9]{9}");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression
        Matcher m = p.matcher(num);
        return (m.find() && m.group().equals(num));
    }


    private String createMsgToSend(String theaterName) {
        String str="Hurry!! Tickets Open For Your Movie at";
        str = str.concat(theaterName).concat(" For Date ");

        String currentURL = driver.getCurrentUrl();
        int index=currentURL.lastIndexOf('/');
        String date = currentURL.substring(index+1,currentURL.length());
        str = str.concat(date).concat("  ").concat(currentURL);
        try{
           str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex){
            logger.warn(ex.getMessage());
        }

        logger.warn(str);
        return str;
    }


    public boolean waitForJStoLoad() {

        WebDriverWait wait = new WebDriverWait(driver, 30);

// wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return ((Long) ((JavascriptExecutor)driver).executeScript("return jQuery.active") == 0);
                } catch (Exception e) {
                    return true;
                }
            }
        };

// wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
            }
        };

        return wait.until(jQueryLoad) && wait.until(jsLoad);
    }

/*
    public static void main(String[] args){
        logger.warn(new SearchForMovie().getNumbers());
    }*/

}
