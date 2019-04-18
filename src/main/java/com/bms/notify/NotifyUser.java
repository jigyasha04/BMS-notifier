package com.bms.notify;

import com.bms.config.ConfigReader;
import com.bms.find.SearchForMovie;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class NotifyUser {

    private static Logger logger = LoggerFactory.getLogger(NotifyUser.class);

    private static boolean textMsgSent = false;

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

        sendNotificationThroughMail(ticketDtl);

        sendMessage(phNum, ticketDtl, 0);


        //sendNotificationThroughCall(drvr,wait, phNum, ticketDtl);
    }

    private void sendMessage(String phNum, String ticketDetail, int j) {
        try {
            ticketDetail="Book_Ticket";
            logger.warn("Sending Notification to "+phNum + "with message "+ ticketDetail);
            String way2SmsUrl = "https://smsapi.engineeringtgr.com/send/?Mobile=8800890565&Password=salesforcerock&Message="+ticketDetail+"&To="+phNum+"&Key=turvo2QLhRPsWfGu1epxgU5DTVNK";
            URL url = new URL(way2SmsUrl);
            URLConnection urlcon = url.openConnection();
            InputStream stream = urlcon.getInputStream();
            int i;
            String response="";
            while ((i = stream.read()) != -1) {
                response+=(char)i;
            }
            if(response.contains("Message Sent Successfully")){
                logger.warn("Successfully send SMS");
                textMsgSent= true;
                //your code when message send success
            }else{
                logger.warn(response);
                textMsgSent= false;
                //Retry to send msg again
                if(j < Integer.valueOf(ConfigReader.getProperty("MSG_RETRY_COUNT")) && !textMsgSent) {
                    sendMessage(phNum,ticketDetail, j++);
                }
                //your code when message not send
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }

    private void sendNotificationThroughCall( ChromeDriver driver, WebDriverWait wait ,String phNum, String ticketDetail) {

        logger.warn("send notification through Call");
        String url= "https://globfone.com/call-phone/";
        driver.navigate().to(url);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"countries-call-cloned_title\"]/span[1]")));

        if(!driver.findElement(By.className("action-button")).getText().equals("India")) {
        logger.warn("Country selected is not India");
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
            logger.warn(driver.findElement(By.className("action-button")).getText());
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.id("call-number")));
        driver.findElement(By.id("call-number")).sendKeys(phNum);
        logger.warn("Phone Number Entered" + phNum);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("next-step")));
        driver.findElement(By.id("next-step")).click();

        logger.warn("Click and Wait for 50 second");
        driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"container_dial\"]/div[5]/div")));

    }

    private void sendNotificationThroughMail(String ticketDtl) {
        String emails= ConfigReader.getProperty("SUBSCRIBERS_MAIL_ID");
        String[] emailList = emails.split(",");
        for(String email: emailList){
            send(email,ticketDtl);
        }
    }

    private void send(String to,String msg){
        //Get properties object
        logger.warn("Sending Mail as notification to " + to);
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        //get Session
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("turvoshipment@gmail.com","testTurvo");
                    }
                });
        logger.warn("MailId and Password validated");
        //compose message
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject("Tickets are available for Booking!");
            message.setText(msg);
        //send message
            Transport.send(message);
            logger.warn("Mail sent successfully to "+ to);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

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
        new NotifyUser().notifyUser(driver,wait,"6309018871", "Endgame");
    }*/


}
