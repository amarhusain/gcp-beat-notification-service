package com.beat.gcp_notification_service.email.service;

import com.beat.gcp_notification_service.email.dto.EmailCommand;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class JavaMailEmailSender implements IEmailSender {

    private final JavaMailSender mailSender;
    private final Resend resendClient;

    @Value("${email.from}")
    private String defaultFrom;

    public JavaMailEmailSender(JavaMailSender mailSender, @Value("${resend.api.key}") String resendApiKey) {
        this.mailSender = mailSender;
        this.resendClient = new Resend(resendApiKey);
    }

    @Override
    public void sendEmailSMTP(EmailCommand cmd) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(defaultFrom);
            helper.setTo(cmd.to());
            helper.setSubject(cmd.subject());
            String ct = (cmd.contentType() == null || cmd.contentType().isBlank()) ? "text/plain" : cmd.contentType();
            boolean html = ct.equalsIgnoreCase("text/html");
            helper.setText(cmd.body(), html);
            mailSender.send(message);

            // Optional: Log the email ID for tracking
            System.out.println("Email sent successfully by Resend by SMTP.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendEmailAPI(EmailCommand cmd) {
        try {
            boolean isHtml = "text/html".equalsIgnoreCase(cmd.contentType());

            CreateEmailOptions.Builder optionsBuilder = CreateEmailOptions.builder()
                    .from(defaultFrom)
                    .to(new String[]{cmd.to()})
                    .subject(cmd.subject() + " " + cmd.totalAmount() + " " + cmd.description());

            if (isHtml) {
                optionsBuilder.html(cmd.body()+ " " + cmd.totalAmount() + cmd.currency() + " " + cmd.description());
            } else {
                optionsBuilder.text(cmd.body() + " " + cmd.totalAmount()+ cmd.currency() + " " + cmd.description());
            }

            CreateEmailOptions sendEmailOptions = optionsBuilder.build();
            CreateEmailResponse response = resendClient.emails().send(sendEmailOptions);

            // Optional: Log the email ID for tracking
            System.out.println("Email sent successfully by Resend ID: " + response.getId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email using Resend", e);
        }
    }


}

