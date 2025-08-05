package ru.practicum.shareit.item;

import booking.BookingCreateDto;
import booking.BookingDto;
import booking.enums.BookingStatus;
import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
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
import java.util.Arrays;
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

    @Test
    void postComment_afterApprovedAndEndedBooking_shouldSucceed() {
        // 1. Создаём бронирование (в прошлом)
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), start, end);
        HttpEntity<BookingCreateDto> bookingRequest = new HttpEntity<>(bookingDto, createHeaders(booker.getId()));

        ResponseEntity<BookingDto> bookingResponse = restTemplate.postForEntity("/bookings", bookingRequest, BookingDto.class);
        assertThat(bookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int bookingId = bookingResponse.getBody().getId();

        // 2. Владелец одобряет бронирование
        HttpEntity<Void> approveRequest = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto> approveResponse = restTemplate.exchange(
                "/bookings/{bookingId}?approved=true",
                HttpMethod.PATCH,
                approveRequest,
                BookingDto.class,
                bookingId
        );

        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approveResponse.getBody().getStatus()).isEqualTo(BookingStatus.APPROVED);

        // 3. Пользователь оставляет комментарий
        CommentCreateDto commentDto = new CommentCreateDto("Отличная вещь, всё работает!");
        HttpEntity<CommentCreateDto> commentRequest = new HttpEntity<>(commentDto, createHeaders(booker.getId()));

        ResponseEntity<CommentDto> commentResponse = restTemplate.postForEntity(
                "/items/{itemId}/comment",
                commentRequest,
                CommentDto.class,
                item.getId()
        );

        // 4. Проверяем результат
        assertThat(commentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CommentDto returnedComment = commentResponse.getBody();
        assertThat(returnedComment).isNotNull();
        assertThat(returnedComment.getText()).isEqualTo("Отличная вещь, всё работает!");
        assertThat(returnedComment.getAuthorName()).isEqualTo("booker");
        assertThat(returnedComment.getCreated()).isNotNull();
    }

    @Test
    void postComment_onWaitingBooking_shouldReturnBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), start, end);
        HttpEntity<BookingCreateDto> bookingRequest = new HttpEntity<>(bookingDto, createHeaders(booker.getId()));
        restTemplate.postForEntity("/bookings", bookingRequest, BookingDto.class);

        // Попытка оставить комментарий (ещё не было использования)
        CommentCreateDto commentDto = new CommentCreateDto("Хорошая вещь");
        HttpEntity<CommentCreateDto> commentRequest = new HttpEntity<>(commentDto, createHeaders(booker.getId()));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/items/{itemId}/comment",
                commentRequest,
                String.class,
                item.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("не пользовался вещью");
    }

    @Test
    void findItemsByOwnerId_shouldReturnItemsWithBookingsAndComments() {
        UserDto owner = createUser("Owner", "owner@test.com");
        ItemDto item = createItem("Дрель", "Мощная дрель", owner.getId());
        UserDto booker = createUser("Booker", "booker@test.com");
        BookingDto bookingDto = createBooking(item.getId(), booker.getId());

        approveBooking(bookingDto.getId(), owner.getId(), true);

        // 4. Оставляем комментарий (только после завершённого бронирования)
        postComment(item.getId(), booker.getId(), "Отличная дрель, всё работает!");

        // 5. Запрашиваем вещи владельца
        HttpHeaders headers = createHeaders(owner.getId());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ItemOwnerDto[]> response = restTemplate.exchange(
                "/items",
                HttpMethod.GET,
                request,
                ItemOwnerDto[].class
        );

        // 6. Проверяем результат
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ItemOwnerDto> items = Arrays.asList(response.getBody());
        assertThat(items.size()).isEqualTo(1);

        ItemOwnerDto returnedItem = items.get(0);
        assertThat(returnedItem.getId()).isEqualTo(item.getId());
        assertThat(returnedItem.getName()).isEqualTo("Дрель");
        assertThat(returnedItem.getComments().size()).isEqualTo(1);
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

    private void approveBooking(int bookingId, int ownerId, boolean approved) {
        HttpHeaders headers = createHeaders(ownerId);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                "/bookings/{id}?approved=" + approved,
                HttpMethod.PATCH,
                request,
                Void.class,
                bookingId
        );
    }

    private void postComment(int itemId, int userId, String text) {
        CommentCreateDto commentDto = new CommentCreateDto(text);
        HttpEntity<CommentCreateDto> request = new HttpEntity<>(commentDto, createHeaders(userId));
        restTemplate.postForEntity("/items/{itemId}/comment", request, CommentDto.class, itemId);
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
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
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
