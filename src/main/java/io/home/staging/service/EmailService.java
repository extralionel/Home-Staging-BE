package io.home.staging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import io.home.staging.api.MailgunApiClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailgunApiClient mailgunApiClient;

    @Value("classpath:templates/email-verification.html")
    private Resource emailTemplate;

    public void sendVerificationEmail(String toEmail, String verificationUrl) {
        log.info("Sending verification email to: {}", toEmail);
        try {
            String template = StreamUtils.copyToString(emailTemplate.getInputStream(), StandardCharsets.UTF_8);
            String htmlContent = template.replace("{{verificationUrl}}", verificationUrl);
            
            mailgunApiClient.sendEmail(
                toEmail, 
                "Please verify your email address", 
                htmlContent
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read email template", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email via Mailgun", e);
        }
    }
}
