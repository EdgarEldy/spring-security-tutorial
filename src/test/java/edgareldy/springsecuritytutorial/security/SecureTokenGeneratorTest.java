package edgareldy.springsecuritytutorial.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SecureTokenGenerator}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
class SecureTokenGeneratorTest {

    private final SecureTokenGenerator generator = new SecureTokenGenerator();

    @Test
    void generateReturnsNonBlankValue() {
        assertThat(generator.generate()).isNotBlank();
    }

    @Test
    void generateReturnsDifferentValuesEachTime() {
        String first = generator.generate();
        String second = generator.generate();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void generateProducesUrlSafeTokensAcrossManyCalls() {
        boolean allUrlSafe = IntStream.range(0, 100)
                .mapToObj(i -> generator.generate())
                .allMatch(token -> token.matches("[A-Za-z0-9_-]+"));

        assertThat(allUrlSafe).isTrue();
    }
}
