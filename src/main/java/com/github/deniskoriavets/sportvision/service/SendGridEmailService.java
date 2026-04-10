package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.exception.EmailSendingException;
import com.github.deniskoriavets.sportvision.service.interfaces.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailService implements EmailService {

    private final SendGrid sendGrid;

    @Value("${app.sendgrid.from-email}")
    private String fromEmail;

    @Value("${app.verification-url}")
    private String verificationUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String fullUrl = verificationUrl + "?token=" + token;

        Email from = new Email(fromEmail);
        Email recipient = new Email(to);
        String subject = "Please verify your email";

        Content content = new Content("text/html",
            "<h1>Welcome!</h1><p>Please click <a href=\"" + fullUrl + "\">here</a> to verify your account.</p>");

        Mail mail = new Mail(from, subject, recipient, content);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error: Status {} Body {}", response.getStatusCode(), response.getBody());
                throw new EmailSendingException("Failed to send email via SendGrid");
            }
        } catch (IOException ex) {
            log.error("IO error while sending email to {}", to, ex);
            throw new EmailSendingException("Error connecting to email service");
        }
    }
}