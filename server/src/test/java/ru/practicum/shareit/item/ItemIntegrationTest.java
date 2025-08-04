package ru.practicum.shareit.item;

import booking.BookingCreateDto;
import booking.BookingDto;
import item.ItemCreateDto;
import item.ItemDto;
import item.ItemUpdateDto;
import item.ItemWithCommentsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dao.UserRepository;
import user.UserCreateDto;
import user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", "owner@example.com");
        booker = createUser("booker", "booker@example.com");
        item = createItem("item", "description", owner.getId());
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @AfterEach
    void clear() {
        userRepository.deleteAll();
    }


    @Test
    void createItem_shouldReturnCreatedItem() {
        ItemCreateDto newItem = new ItemCreateDto(
                "newItem",
                "desc",
                true,
                null
        );

        ResponseEntity<ItemDto> response = restTemplate.exchange(
                "/items",
                HttpMethod.POST,
                new HttpEntity<>(newItem, createHeaders(owner.getId())),
                ItemDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("newItem");
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() {
        ItemUpdateDto updatedItem = new ItemUpdateDto("updatedName", "updatedDescription", false);
        ResponseEntity<ItemDto> response = restTemplate.exchange("/items/{itemId}",
                HttpMethod.PATCH,
                new HttpEntity<>(updatedItem, createHeaders(owner.getId())),
                ItemDto.class,
                item.getId());


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("updatedName");
        assertThat(response.getBody().getAvailable()).isFalse();
    }

    @Test
    void getItemById_shouldReturnItem() {
        ResponseEntity<ItemWithCommentsDto> response = restTemplate.exchange(
                "/items/{itemId}",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(owner.getId())),
                ItemWithCommentsDto.class,
                item.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(item.getId());
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        ResponseEntity<List<ItemDto>> response = restTemplate.exchange(
                "/items/search?text=item",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(booker.getId())),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get(0).getName()).isEqualTo("item");
    }

    private UserDto createUser(String name, String email) {
        UserCreateDto userDto = new UserCreateDto(name, email);
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                "/users",
                new HttpEntity<>(userDto),
                UserDto.class
        );
        return response.getBody();
    }

    private ItemDto createItem(String name, String description, int ownerId) {
        ItemCreateDto itemDto = new ItemCreateDto(name, description, true, null);
        ResponseEntity<ItemDto> response = restTemplate.exchange(
                "/items",
                HttpMethod.POST,
                new HttpEntity<>(itemDto, createHeaders(ownerId)),
                ItemDto.class
        );
        return response.getBody();
    }

    private BookingDto createBooking(int itemId, int bookerId) {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingCreateDto bookingDto = new BookingCreateDto(itemId, start, end);

        ResponseEntity<BookingDto> response = restTemplate.exchange(
                "/bookings",
                HttpMethod.POST,
                new HttpEntity<>(bookingDto, createHeaders(bookerId)),
                BookingDto.class
        );
        return response.getBody();
    }

    private HttpHeaders createHeaders(int userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
