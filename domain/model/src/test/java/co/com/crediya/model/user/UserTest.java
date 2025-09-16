package co.com.crediya.model.user;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void settersAndGettersTest() {
        User user = new User();
        user.setUserId("U001");
        user.setFirstName("Ana");
        user.setLastName("Perez");
        user.setBaseSalary(new BigDecimal("2500"));

        assertEquals("U001", user.getUserId());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Perez", user.getLastName());
        assertEquals(new BigDecimal("2500"), user.getBaseSalary());
    }

    @Test
    void builderPatternTest() {
        LocalDate birthDate = LocalDate.of(1985, 10, 20);
        BigDecimal salary = new BigDecimal("5000.00");

        User user = User.builder()
                .userId("U456")
                .firstName("Jane")
                .lastName("Smith")
                .documentId("12365")
                .birthDate(birthDate)
                .phone("987654321")
                .email("jane@smith.com")
                .baseSalary(salary)
                .build();

        assertNotNull(user);
        assertEquals("U456", user.getUserId());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals(birthDate, user.getBirthDate());
        assertEquals("987654321", user.getPhone());
        assertEquals("jane@smith.com", user.getEmail());
        assertEquals(new BigDecimal("5000.00"), user.getBaseSalary());
    }

    @Test
    void toBuilderTest() {
        LocalDate birthDate = LocalDate.of(1992, 3, 10);

        User original = User.builder()
                .userId("U789")
                .firstName("Carlos")
                .lastName("Lopez")
                .documentId("12365")
                .birthDate(birthDate)
                .phone("555555555")
                .email("carlos@lopez.com")
                .baseSalary(new BigDecimal("4000.00"))
                .build();

        User modified = original.toBuilder()
                .lastName("Gomez")
                .build();

        assertEquals("U789", modified.getUserId());
        assertEquals("Carlos", modified.getFirstName());
        assertEquals("Gomez", modified.getLastName());
        assertEquals(original.getBirthDate(), modified.getBirthDate());
        assertEquals(original.getPhone(), modified.getPhone());
        assertEquals(original.getEmail(), modified.getEmail());
        assertEquals(original.getBaseSalary(), modified.getBaseSalary());
    }
}