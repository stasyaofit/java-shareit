package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class
UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> jacksonTester;
    private final UserDto dto = UserDto.builder().id(1L).name("User").email("valid@test.ru").build();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Создание валидного пользователя.")
    void createValidUser() throws IOException {
        JsonContent<UserDto> json = jacksonTester.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("User");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("valid@test.ru");
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем.")
    void createUserWithEmptyName() {
        dto.setName("");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "notmail.ru"})
    @DisplayName("Создание пользователя с пустой или некорректной почтой.")
    void createUserWithIncorrectEmail(String email) {
        dto.setEmail(email);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
}

