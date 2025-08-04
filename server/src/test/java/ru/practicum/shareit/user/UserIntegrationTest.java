package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dao.UserRepository;
import user.UserCreateDto;
import user.UserDto;
import user.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private UserRepository userRepository;

    private UserCreateDto newUser;

    @BeforeEach
    void setUp() {
        newUser = new UserCreateDto("username", "email@gmail.com");
        template.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @AfterEach
    void clear() {
        userRepository.deleteAll();
    }

    @Test
    public void create_shouldSaveNewUser() {
        ResponseEntity<UserDto> response = template.postForEntity("/users", newUser, UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserDto userDto = response.getBody();
        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isNotNull();
        assertThat(userDto.getName()).isEqualTo(newUser.getName());

        ResponseEntity<UserDto> getResponse = template.getForEntity(
                "/users/" + response.getBody().getId(),
                UserDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    public void update_shouldUpdateExistingUser() {
        ResponseEntity<UserDto> response = template.postForEntity("/users", newUser, UserDto.class);
        UserUpdateDto updateDto = new UserUpdateDto("Updated Name", "updated@test.com");
        template.exchange(
                "/users/1",
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto),
                UserDto.class
        );
    }

    @Test
    void createUser_withDuplicateEmail_shouldReturnConflict() {
        template.postForEntity("/users", newUser, UserDto.class);

        UserCreateDto secondUser = new UserCreateDto("second user", newUser.getEmail());
        ResponseEntity<String> response = template.postForEntity(
                "/users",
                secondUser,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("email already exist");
    }


}
