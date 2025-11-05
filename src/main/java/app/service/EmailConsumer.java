package app.service;

import app.dto.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public EmailConsumer(JavaMailSender mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-email-topic", groupId = "email-service")
    public void consumeEmailMessage(String jsonMessage) {
        try {
            EmailMessage emailMessage = objectMapper.readValue(jsonMessage, EmailMessage.class);

            sendRealEmail(emailMessage);

        } catch (Exception e) {
            System.err.println("Ошибка сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendRealEmail(EmailMessage emailMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailMessage.getTo());
            message.setSubject(emailMessage.getSubject());
            message.setText(emailMessage.getBody());
            message.setFrom("javamodule-noreply@yandex.ru");

            mailSender.send(message);

            System.out.println("Email отправлен: " + emailMessage.getTo());

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
