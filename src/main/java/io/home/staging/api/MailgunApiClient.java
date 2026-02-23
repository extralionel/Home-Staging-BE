package io.home.staging.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class MailgunApiClient {

  private final RestClient mailgunClient;
  private final String from;

  public MailgunApiClient(
      @Qualifier("mailgunClient") RestClient mailgunClient,
      @Value("${email.mailgun.from:hello}") String from
  ) {
    this.mailgunClient = mailgunClient;
    this.from = from;
  }

  public String sendEmail(String to, String subject, String htmlContent) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("from", this.from);
    formData.add("to", to);
    formData.add("subject", subject);
    formData.add("html", htmlContent);

    return mailgunClient.post()
        .uri("/messages")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(formData)
        .retrieve()
        .body(String.class);
  }
}
