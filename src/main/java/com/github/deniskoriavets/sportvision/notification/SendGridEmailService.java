package com.github.deniskoriavets.sportvision.notification;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import com.github.deniskoriavets.sportvision.exception.EmailSendingException;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailService implements EmailService, NotificationStrategy {

    private final SendGrid sendGrid;
    private final TemplateEngine templateEngine;
    private final ParentRepository parentRepository;

    @Value("${app.sendgrid.from-email}")
    private String fromEmail;

    @Value("${app.verification-url}")
    private String verificationUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        Parent parent = parentRepository.findByEmail(to)
            .orElseThrow(() -> new EmailSendingException("Parent with email " + to + " not found"));
        String fullUrl = verificationUrl + "?token=" + token;
        Map<String, Object> variables = Map.of(
            "firstName", parent.getFirstName(),
            "verificationUrl", fullUrl
        );
        sendHtmlEmail(to, "Підтвердження реєстрації - SportVision", "verification-email", variables);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process(templateName, context);

        Email from = new Email(fromEmail);
        Email recipient = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, recipient, content);

        executeSend(mail, to);
    }

    @Override
    public boolean supports(NotificationPreference preference) {
        return preference == NotificationPreference.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        if (message.templateName() != null && message.templateVariables() != null) {
            sendHtmlEmail(message.email(), message.subject(), message.templateName(), message.templateVariables());
            return;
        }

        Email from = new Email(fromEmail);
        Email recipient = new Email(message.email());
        Content content = new Content("text/html", message.content());
        Mail mail = new Mail(from, message.subject(), recipient, content);

        executeSend(mail, message.email());
    }

    private void executeSend(Mail mail, String recipientEmail) {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error: Status {} Body {}", response.getStatusCode(), response.getBody());
                throw new EmailSendingException("Failed to send email to " + recipientEmail);
            }
            log.info("Email sent successfully to: {}", recipientEmail);
        } catch (IOException ex) {
            log.error("IO error while sending email to {}", recipientEmail, ex);
            throw new EmailSendingException("Error connecting to email service");
        }
    }
}