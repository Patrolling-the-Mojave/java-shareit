package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.service.UserService;
import user.UserCreateDto;
import user.UserDto;
import user.UserUpdateDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserCreateDto user;

    @BeforeEach
    void setUp() {
        user = new UserCreateDto("alex", "alexEmail@gmail.com");
    }

    @Test
    public void testCreateUser() throws Exception {
        UserCreateDto newUser = user;

        when(userService.create(newUser)).thenReturn(
                UserDto
                        .builder()
                        .id(1)
                        .name(newUser.getName())
                        .email(newUser.getEmail())
                        .build());

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newUser))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(newUser.getName())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())));

    }

    @Test
    public void testUpdateUser() throws Exception {
        UserUpdateDto updatedUser = new UserUpdateDto("newName", "newEmail@gmail.com");

        when(userService.update(updatedUser, 1))
                .thenReturn(new UserDto(1, updatedUser.getName(), updatedUser.getEmail()));


        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedUser))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @Test
    public void testFindAll() throws Exception {
        when(userService.findAll())
                .thenReturn(List.of(new UserDto(1, user.getName(), user.getEmail()), new UserDto(2, "john", "johnEmail@gmail.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("alex"))
                .andExpect(jsonPath("$[1].name").value("john"));
    }

    @Test
    public void getUser() throws Exception {
        when(userService.findById(1))
                .thenReturn(new UserDto(1, user.getName(), user.getEmail()));

        mockMvc.perform(get("/users/1"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    public void testGetUserById() throws Exception {
        when(userService.findById(1))
                .thenReturn(new UserDto(1, user.getName(), user.getEmail()));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    public void deleteTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService).delete(1);
    }


}
