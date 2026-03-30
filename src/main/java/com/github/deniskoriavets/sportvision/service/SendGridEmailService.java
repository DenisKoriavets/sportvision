package com.github.deniskoriavets.sportvision.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendGridEmailService implements EmailService {

    @Value("${app.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.verification-url}")
    private String verificationUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        Email from = new Email("no-reply@sportvision.com");
        String subject = "Підтвердження реєстрації SportVision";
        Email target = new Email(to);
        
        String fullUrl = verificationUrl + "?token=" + token;
        
        Content content = new Content("text/html",
            "<h1>Вітаємо у SportVision!</h1>" +
            "<p>Будь ласка, підтвердіть ваш email, перейшовши за посиланням:</p>" +
            "<a href=\"" + fullUrl + "\">Підтвердити пошту</a>"
        );

        Mail mail = new Mail(from, subject, target, content);
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw new RuntimeException("Помилка відправки email через SendGrid", ex);
        }
    }
}