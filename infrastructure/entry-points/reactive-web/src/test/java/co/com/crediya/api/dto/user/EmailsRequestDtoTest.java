package co.com.crediya.api.dto.user;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailsRequestDtoTest {

    @Test
    void constructorAndAccessor_shouldReturnSameEmails() {
        List<String> emails = List.of("user1@crediya.com", "user2@crediya.com");
        EmailsRequestDto dto = new EmailsRequestDto(emails);

        assertThat(dto.emails()).containsExactly("user1@crediya.com", "user2@crediya.com");
    }

    @Test
    void equalsAndHashCode_shouldBeBasedOnEmails() {
        EmailsRequestDto a = new EmailsRequestDto(List.of("a@crediya.com", "b@crediya.com"));
        EmailsRequestDto b = new EmailsRequestDto(List.of("a@crediya.com", "b@crediya.com"));
        EmailsRequestDto c = new EmailsRequestDto(List.of("x@crediya.com"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toString_shouldContainEmails() {
        EmailsRequestDto dto = new EmailsRequestDto(List.of("user1@crediya.com"));
        String s = dto.toString();

        assertThat(s).contains("user1@crediya.com");
    }

    @Test
    void nullEmails_shouldBeAllowed() {
        EmailsRequestDto dto = new EmailsRequestDto(null);
        assertThat(dto.emails()).isNull();
    }

    @Test
    void underlyingListIsNotDefensivelyCopied_recordHoldsReference() {
        List<String> source = new ArrayList<>(List.of("a@crediya.com"));
        EmailsRequestDto dto = new EmailsRequestDto(source);

        source.add("b@crediya.com");

        assertThat(dto.emails()).containsExactly("a@crediya.com", "b@crediya.com");
    }
}