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
        String path = "/auth/register/verify";
        String message = "Click the link to verify your email: " + path + "?token=" + verificationToken;
        sendEmail(email, verificationToken,subject,path,message);
    }

    public void sendForgotPasswordEmail(String email, String verificationToken){
        String subject = "Forgot Password";
        String path = "/auth/forgot-password/verify";
        String message = "Click the link to reset your password: " + path + "?token=" + verificationToken;
        sendEmail(email, verificationToken,subject,path,message);
    }

    private void sendEmail(String email, String verificationToken, String subject, String path, String message) {
        try{
            String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", verificationToken)
                    .toUriString();

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

}
