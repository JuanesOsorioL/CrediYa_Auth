package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GenericDtoMapperTest {

    private GenericDtoMapper mapper;

    @BeforeEach
    void setUp() {
   mapper = new GenericDtoMapperImpl();
    }

    @Test
    void toLogin_mapsEmailAndPassword() {
        LoginDto dto = new LoginDto("john@doe.com", "1234");

        Login login = mapper.toLogin(dto);

        assertThat(login).isNotNull();
        assertThat(login.getEmail()).isEqualTo("john@doe.com");
        assertThat(login.getPassword()).isEqualTo("1234");
    }

    @Test
    void toUser_fromUserDocumentDto_mapsOnlyDocumentId() {
        UserDocumentDto doc = new UserDocumentDto("DOC-999");

        User user = mapper.toUser(doc);

        assertThat(user).isNotNull();
        assertThat(user.getDocumentId()).isEqualTo("DOC-999");
        assertThat(user.getUserId()).isNull();
        assertThat(user.getFirstName()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void toDto_fromDomainUser_mapsAllFields() {
        User domain = new User(
                "u1", "John", "Doe", "DOC-1",
                LocalDate.of(1990, 1, 1),
                "3000000000", "john@doe.com", "pass",
                "Customer", new BigDecimal("1500000.00")
        );

        UserDto dto = mapper.toDto(domain);

        assertThat(dto).isNotNull();
        assertThat(dto.userId()).isEqualTo("u1");
        assertThat(dto.firstName()).isEqualTo("John");
        assertThat(dto.lastName()).isEqualTo("Doe");
        assertThat(dto.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(dto.documentId()).isEqualTo("DOC-1");
        assertThat(dto.email()).isEqualTo("john@doe.com");
        assertThat(dto.phone()).isEqualTo("3000000000");
        assertThat(dto.password()).isEqualTo("pass");
        assertThat(dto.rolId()).isEqualTo("Customer");
        assertThat(dto.baseSalary()).isEqualByComparingTo(new BigDecimal("1500000.00"));
    }

    @Test
    void toUser_fromUserDto_mapsAllFields() {
        UserDto dto = new UserDto(
                "u2", "Jane", "Smith",
                LocalDate.of(1985, 10, 20),
                "3112223333", "jane@smith.com", "3112223333", "abcd",
                "Adviser", new BigDecimal("5000000.00")
        );

        User user = mapper.toUser(dto);

        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo("u2");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1985, 10, 20));
        assertThat(user.getDocumentId()).isEqualTo("3112223333");
        assertThat(user.getEmail()).isEqualTo("jane@smith.com");
        assertThat(user.getPhone()).isEqualTo("3112223333");
        assertThat(user.getPassword()).isEqualTo("abcd");
        assertThat(user.getRolId()).isEqualTo("Adviser");
        assertThat(user.getBaseSalary()).isEqualByComparingTo(new BigDecimal("5000000.00"));
    }

    @Test
    void nullInputs_returnNulls() {
        assertThat(mapper.toLogin(null)).isNull();
        assertThat(mapper.toUser((UserDocumentDto) null)).isNull();
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toUser((UserDto) null)).isNull();
    }
}