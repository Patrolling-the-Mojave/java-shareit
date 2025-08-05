package ru.practicum.shareit.request;

import item.ItemCreateDto;
import item.ItemDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.user.dao.UserRepository;
import user.UserCreateDto;
import user.UserDto;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private UserDto requester;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setup() {
        requester = createUser("requester", "requester@example.com");
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    public void createRequest_shouldReturnCreatedRequest() {
        RequestCreateDto newRequest = RequestCreateDto
                .builder()
                .description("need an item")
                .build();
        ResponseEntity<ItemRequestDto> response = restTemplate.exchange("/requests",
                HttpMethod.POST,
                new HttpEntity<>(newRequest, createHeaders(requester.getId())),
                ItemRequestDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("need an item");

    }

    @Test
    public void findByRequesterId_shouldReturnUsersRequestsWithComments() {
        RequestCreateDto newRequest = RequestCreateDto
                .builder()
                .description("need an item")
                .build();

        ResponseEntity<ItemRequestDto> response = restTemplate.exchange("/requests",
                HttpMethod.POST,
                new HttpEntity<>(newRequest, createHeaders(requester.getId())),
                ItemRequestDto.class);
        ItemCreateDto newItem = new ItemCreateDto("item", "requestedItem", true, response.getBody().getId());
        UserDto userDto = createUser("itemOwner", "owner@example.com");
        restTemplate.exchange("/items", HttpMethod.POST, new HttpEntity<>(newItem, createHeaders(userDto.getId())), ItemDto.class);


        Collection<RequestWithAnswersDto> requestsWithAnswers = restTemplate.exchange(
                "/requests",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(requester.getId())),
                new ParameterizedTypeReference<Collection<RequestWithAnswersDto>>() {
                }
        ).getBody();


        assertThat(requestsWithAnswers).isNotNull();
        assertThat(requestsWithAnswers.size()).isEqualTo(1);
    }

    @Test
    public void findById_shouldReturnRequestById() {
        RequestCreateDto newRequest = RequestCreateDto
                .builder()
                .description("need an item")
                .build();
        ResponseEntity<ItemRequestDto> response = restTemplate.exchange("/requests",
                HttpMethod.POST,
                new HttpEntity<>(newRequest, createHeaders(requester.getId())),
                ItemRequestDto.class);
        ItemCreateDto newItem = new ItemCreateDto("item", "requestedItem", true, response.getBody().getId());
        UserDto userDto = createUser("itemOwner", "owner@example.com");
        restTemplate.exchange("/items", HttpMethod.POST, new HttpEntity<>(newItem, createHeaders(userDto.getId())), ItemDto.class);

        ResponseEntity<RequestWithAnswersDto> responseEntity = restTemplate.getForEntity("/requests/1", RequestWithAnswersDto.class);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getDescription()).isEqualTo("need an item");
    }

    private UserDto createUser(String name, String email) {
        UserCreateDto userCreateDto = new UserCreateDto(name, email);
        return restTemplate.postForEntity("/users", userCreateDto, UserDto.class).getBody();
    }

    private HttpHeaders createHeaders(int userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
