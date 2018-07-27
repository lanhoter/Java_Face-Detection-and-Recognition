/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition.gui;

/**
 *
 * @author Du
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Crunchify.com
 *
 */
public class SendAlert {

    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;

    public static void generateAndSendAlert() throws AddressException, MessagingException {

        String timeStamp = new SimpleDateFormat("dd/MM/yyyy    HH:mm:ss").format(Calendar.getInstance().getTime());
        // Step1
        System.out.println("\n 1st ===> Setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
        System.out.println("\n\n 2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("lanhoter@hotmail.com"));
        //generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("test2@crunchify.com"));
        generateMailMessage.setSubject("Warning, Un-authorized Person!");
        String emailBody = "This Email is automatically sent by automated Face Detection and Recognition System. "
                + "<br><hr>Event: There is a Person without an authorzied Identity trying to Login the Face Recognition System"
                + "<br>"
                + "<br>Subject: " + "<img src=\"D:\\FYPfinal\\FYProject\\Pre-IMG\\LoginPerson.jpg\">"
                + "<br>Face Numbers: 1"
                + "<br>Time: " + timeStamp
                + "<hr>"
                + "<br> Regards, "
                + "<br>Du Mengyu"
                + "<br>Automated Face Detection and Recognition System";
        generateMailMessage.setContent(emailBody, "text/html");

        System.out.println("Mail Session has been created successfully..");

        // Step3
        System.out.println("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", "feols808@gmail.com", "Dulant808227");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }

}
