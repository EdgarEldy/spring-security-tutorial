package edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import edgareldy.springsecuritytutorial.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Unit tests for {@link EmailServiceImpl}, with {@link JavaMailSender}
 * mocked. Runs the {@code @Async} methods synchronously (no Spring context
 * involved), asserting on the message content built before the actual send.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;
    private User user;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, BASE_URL);
        user = User.builder().id(1L).firstName("Ada").email("ada@example.com").build();
    }

    @Test
    void sendActivationEmailIncludesTokenLink() {
        emailService.sendActivationEmail(user, "raw-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("ada@example.com");
        assertThat(message.getSubject()).isEqualTo("Activate your account");
        assertThat(message.getText()).contains(BASE_URL + "/api/auth/activate-account?token=raw-token");
    }

    @Test
    void sendPasswordResetEmailIncludesTokenLink() {
        emailService.sendPasswordResetEmail(user, "reset-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("ada@example.com");
        assertThat(message.getSubject()).isEqualTo("Reset your password");
        assertThat(message.getText()).contains(BASE_URL + "/api/auth/reset-password?token=reset-token");
    }

    @Test
    void sendMethodsRunOnTheEmailTaskExecutor() throws NoSuchMethodException {
        var sendActivation = EmailServiceImpl.class.getMethod("sendActivationEmail", User.class, String.class);
        var sendReset = EmailServiceImpl.class.getMethod("sendPasswordResetEmail", User.class, String.class);

        assertThat(sendActivation.getAnnotation(org.springframework.scheduling.annotation.Async.class).value())
                .isEqualTo("emailTaskExecutor");
        assertThat(sendReset.getAnnotation(org.springframework.scheduling.annotation.Async.class).value())
                .isEqualTo("emailTaskExecutor");
    }
}
