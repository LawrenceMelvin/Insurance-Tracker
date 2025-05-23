package com.project.lawrence.insurance_tracker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String email, String verificationToken){
        String subject = "Email Verification";
        String frontend = "http://localhost:5173";
        String path = frontend+"/auth/login";
        String message = "Click the link to verify your email: " + path + "?token=" + verificationToken;
        sendEmail(email, verificationToken,subject,path,message);
    }

    public void sendForgotPasswordEmail(String email, String verificationToken){
        String subject = "Forgot Password";
        String frontend = "http://localhost:5173";
        String path = frontend+"/auth/set-new-password";
        String message = "Click the link to reset your password: " + path + "?token=" + verificationToken;
        sendEmail(email, verificationToken,subject,path,message);
    }

    private void sendEmail(String email, String verificationToken, String subject, String path, String message) {
        try{
            String actionUrl = path+"?token=" + verificationToken;

            String content = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border-radius: 8px; background-color: #f9f9f9; text-align: center;">
                         <h2 style="color: #333;">%s</h2>
                         <p style="font-size: 16px; color: #555;">%s</p>
                         <a href="%s" style="display: inline-block; margin: 20px 0; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #007bff; text-decoration: none; border-radius: 5px;">Proceed</a>
                         <p style="font-size: 14px; color: #777;">Or copy and paste this link into your browser:</p>
                         <p style="font-size: 14px; color: #007bff;">%s</p>
                         <p style="font-size: 12px; color: #aaa;">This is an automated message. Please do not reply.</p>
                    </div>
                    """.formatted(subject,message,actionUrl,actionUrl);

            MimeMessage mineMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mineMessage,true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(content,true);
            javaMailSender.send(mineMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendReminderEmail(String email, String subject, String message) {
        try {
//            String content = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border-radius: 8px; background-color: #f4f4f4; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);">
//                    <h2 style="color: #333; text-align: center;">Insurance Expiry Reminder</h2>
//                    <p style="font-size: 16px; color: #555; text-align: center;">
//                        Dear User,
//                    </p>
//                    <p style="font-size: 16px; color: #555; text-align: center;">
//                        Your insurance <strong>%s</strong> (Type: <strong>%s</strong>) is expiring on <strong>%s</strong>.
//                    </p>
//                    <p style="font-size: 16px; color: #555; text-align: center;">
//                        Please take the necessary action to renew your insurance on time.
//                    </p>
//                    <p style="font-size: 12px; color: #aaa; text-align: center;">
//                        This is an automated message. Please do not reply.
//                    </p>
//                </div>
//            """.formatted(insuranceName, insuranceType, insuranceToDate);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(message, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
