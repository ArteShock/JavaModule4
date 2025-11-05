package app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.dto.EmailMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class EmailProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_EMAIL_TOPIC = "user-email-topic";

    public EmailProducer(KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserCreate(String userEmail, String username) {
        String subject = "Добро пожаловать в наше приложение!";
        String body = String.format(
                "Уважаемый %s,\n\nВы успешно зарегистрировались в нашем приложении.\n\nС уважением,\nКоманда приложения",
                username
        );

        EmailMessage emailMessage = new EmailMessage(
                userEmail,
                subject,
                body
        );

        sendEmailMessage(emailMessage);
    }

    public void sendUserDelete(String userEmail, String username) {
        String subject = "Ваш аккаунт был удален";
        String body = String.format(
                "Уважаемый %s,\n\nВаш аккаунт был успешно удален из нашей системы.\n\nС уважением,\nКоманда приложения",
                username
        );

        EmailMessage emailMessage = new EmailMessage(
                userEmail,
                subject,
                body
        );

        sendEmailMessage(emailMessage);
    }

    private void sendEmailMessage(EmailMessage emailMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(emailMessage);
            kafkaTemplate.send(USER_EMAIL_TOPIC, jsonMessage);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}