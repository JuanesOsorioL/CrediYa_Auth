//package co.com.crediya.api.mapper;
//
//
//import co.com.crediya.api.dto.user.UserDto;
//
//import co.com.crediya.model.user.User;
//import org.junit.jupiter.api.Test;
//import org.mapstruct.factory.Mappers;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UserDtoMapperTest {
//
//    private final UserDtoMapper mapper = Mappers.getMapper(UserDtoMapper.class);
//
//    @Test
//    void shouldMapUserToUserDto() {
//
//        User user = new User();
//        user.setUserId("u01");
//        user.setFirstName("Juan");
//        user.setLastName("Pérez");
//        user.setBirthDate(LocalDate.of(1995, 5, 12));
//        user.setPhone("+573001112233");
//        user.setEmail("juan@example.com");
//        user.setBaseSalary(new BigDecimal("1200000.0"));
//
//        UserDto dto = mapper.toDto(user);
//
//        assertEquals("u01", dto.userId());
//        assertEquals("Juan", dto.firstName());
//        assertEquals("Pérez", dto.lastName());
//        assertEquals(LocalDate.of(1995, 5, 12), dto.birthDate());
//        assertEquals("+573001112233", dto.phone());
//        assertEquals("juan@example.com", dto.email());
//        assertEquals(new BigDecimal("1200000.0"), dto.baseSalary());
//    }
//
//    @Test
//    void shouldMapUserDtoToUser() {
//
//        UserDto dto = new UserDto(
//                "u02",
//                "Maria",
//                "Gonzalez",
//                LocalDate.of(1993, 8, 25),
//
//                "+573001234567",
//
//                "maria@example.com",
//                "123456789",
//                new BigDecimal("950000.0")
//        );
//
//        User user = mapper.toUser(dto);
//
//        assertEquals("u02", user.getUserId());
//        assertEquals("Maria", user.getFirstName());
//        assertEquals("Gonzalez", user.getLastName());
//        assertEquals(LocalDate.of(1993, 8, 25), user.getBirthDate());
//        assertEquals("+573001234567", user.getPhone());
//        assertEquals("maria@example.com", user.getEmail());
//        assertEquals(new BigDecimal("950000.0"), user.getBaseSalary());
//    }
//    @Test
//    void shouldReturnNullWhenUserIsNull() {
//        UserDto dto = mapper.toDto(null);
//        assertNull(dto);
//    }
//
//    @Test
//    void shouldReturnNullWhenUserDtoIsNull() {
//        User user = mapper.toUser((UserDto) null);
//        assertNull(user);
//    }
//}