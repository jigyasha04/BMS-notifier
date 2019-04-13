package com.bms.notify;

import com.bms.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotifyUser {

    private NotifyUser()
    {
        // private constructor
    }

    // Inner class to provide instance of class
    private static class BillPughSingleton
    {
        private static final NotifyUser INSTANCE = new NotifyUser();
    }

    public static NotifyUser getInstance()
    {
        return BillPughSingleton.INSTANCE;
    }

    public void notifyUser(ChromeDriver drvr,WebDriverWait wait, String phNum, String ticketDtl) {

        if(drvr == null){
            sendMessage(phNum, ticketDtl, 0);
        } else{
            sendNotificationThroughCall(drvr,wait, phNum, ticketDtl);
        }
    }

    private void sendMessage(String phNum, String ticketDetail, int j) {
        try {
            System.out.println("Sending Notification to "+phNum + "with message "+ ticketDetail);
            String way2SmsUrl = "https://smsapi.engineeringtgr.com/send/?Mobile=8800890565&Password=salesforcerock&Message="+ticketDetail+"&To="+phNum+"&Key=turvo2QLhRPsWfGu1epxgU5DTVNK";
            URL url = new URL(way2SmsUrl);
            URLConnection urlcon = url.openConnection();
            InputStream stream = urlcon.getInputStream();
            int i;
            String response="";
            while ((i = stream.read()) != -1) {
                response+=(char)i;
            }
            if(response.contains("success")){
                System.out.println("Successfully send SMS");
                //your code when message send success
            }else{
                System.out.println(response);
                //Retry to send msg again
                if(j < Integer.valueOf(ConfigReader.getProperty("MSG_RETRY_COUNT"))) {
                    sendMessage(phNum,ticketDetail, j++);
                }
                //your code when message not send
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendNotificationThroughCall( ChromeDriver driver, WebDriverWait wait ,String phNum, String ticketDetail) {

        System.out.println("send notification through Call");
        String url= "https://globfone.com/call-phone/";
        driver.navigate().to(url);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"countries-call-cloned_title\"]/span[1]")));

        if(!driver.findElement(By.className("action-button")).getText().equals("India")) {
        System.out.println("Country selected is not India");
            String searchText = "India";
            WebElement dropdown = driver.findElement(By.id("countries-call-cloned_msdd"));
            dropdown.click(); // assuming you have to click the "dropdown" to open it
            List<WebElement> options = dropdown.findElements(By.tagName("li"));
            for (WebElement option : options)
            {
                if (option.getText().equals(searchText))
                {
                    option.click(); // click the desired option
                    break;
                }
            }
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.id("call-number")));
        driver.findElement(By.id("call-number")).sendKeys(phNum);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("next-step")));
        driver.findElement(By.id("next-step")).click();
        driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"container_dial\"]/div[5]/div")));

    }


  /* public static void main(String[] args){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--allow-file-access-from-files",
                "--use-fake-ui-for-media-stream",
                "--allow-file-access",
                "--use-file-for-fake-audio-capture",
                "--use-fake-device-for-media-stream",
                "--use-file-for-fake-audio-capture=/Users/subrat.thakur/Downloads/avengers-endgame-ringtone.mp3");

        ChromeDriver driver  = new ChromeDriver(options);
        WebDriverWait wait =  new WebDriverWait(driver, 40);
        new NotifyUser().notifyUser(driver,wait,"6309018871","test");
    }*/


}
