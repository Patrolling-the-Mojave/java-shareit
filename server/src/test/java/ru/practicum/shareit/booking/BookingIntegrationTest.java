package ru.practicum.shareit.booking;

import booking.BookingCreateDto;
import booking.BookingDto;
import booking.enums.BookingStatus;
import item.ItemCreateDto;
import item.ItemDto;
import item.ItemUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.user.dao.UserRepository;
import user.UserCreateDto;
import user.UserDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private UserDto booker;
    private UserDto owner;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        booker = createUser("booker@example.com", "Booker");
        owner = createUser("owner@example.com", "Owner");
        ItemCreateDto itemCreateDto = new ItemCreateDto("Дрель", "Простая дрель", true, null);
        item = createItem(itemCreateDto, owner.getId());
    }

    @AfterEach
    void clear() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBooking_shouldCreateNewBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(item.getId(), start, end);

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(booker.getId()));
        ResponseEntity<BookingDto> response = restTemplate.postForEntity("/bookings", request, BookingDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BookingDto bookingDto = response.getBody();
        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getId()).isNotNull();
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookingDto.getItem().getId()).isEqualTo(item.getId());
        assertThat(bookingDto.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void approveBooking_shouldApproveBooking() {
        BookingDto booking = createTestBooking();
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto> response = restTemplate.exchange(
                "/bookings/" + booking.getId() + "?approved=true",
                HttpMethod.PATCH,
                request,
                BookingDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto approvedBooking = response.getBody();
        assertThat(approvedBooking).isNotNull();
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void rejectBooking_shouldRejectBooking() {
        BookingDto booking = createTestBooking();
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto> response = restTemplate.exchange(
                "/bookings/" + booking.getId() + "?approved=false",
                HttpMethod.PATCH,
                request,
                BookingDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto approvedBooking = response.getBody();
        assertThat(approvedBooking).isNotNull();
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void findById_shouldReturnBooking() {
        BookingDto booking = createTestBooking();
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto> response = restTemplate.exchange(
                "/bookings/" + booking.getId(),
                HttpMethod.GET,
                request,
                BookingDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto foundBooking = response.getBody();
        assertThat(foundBooking).isNotNull();
        assertThat(foundBooking.getId()).isEqualTo(booking.getId());
    }

    @Test
    void findAllBookingsOfUser_shouldReturnUserBookings() {
        BookingDto booking = createTestBooking();

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings?state=ALL",
                HttpMethod.GET,
                request,
                BookingDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto[] bookings = response.getBody();
        assertThat(bookings).hasSize(1);
        assertThat(bookings[0].getId()).isEqualTo(booking.getId());
    }

    @Test
    void findAllBookingsByItemsOwner_shouldReturnOwnerBookings() {
        BookingDto booking = createTestBooking();

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings/owner?state=ALL",
                HttpMethod.GET,
                request,
                BookingDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto[] bookings = response.getBody();
        assertThat(bookings).hasSize(1);
        assertThat(bookings[0].getId()).isEqualTo(booking.getId());
    }


    @Test
    void approveBooking_shouldRejectBooking() {
        // Создаём бронирование
        BookingDto booking = createTestBooking();

        // Отклоняем бронирование через PATCH с параметром approved=false
        HttpHeaders headers = createHeaders(owner.getId());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<BookingDto> response = restTemplate.exchange(
                "/bookings/{bookingId}?approved=false",
                HttpMethod.PATCH,
                request,
                BookingDto.class,
                booking.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingDto rejected = response.getBody();
        assertThat(rejected).isNotNull();
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_byNotOwner_shouldReturnForbidden() {
        BookingDto booking = createTestBooking();

        // Попытка подтвердить бронирование не владельцем
        HttpHeaders headers = createHeaders(booker.getId()); // booker — не владелец
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/{bookingId}?approved=true",
                HttpMethod.PATCH,
                request,
                String.class,
                booking.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("не является владельцем");
    }

    @Test
    void findById_byNotOwnerOrBooker_shouldReturnForbidden() {
        BookingDto booking = createTestBooking();

        // Третий пользователь пытается получить бронирование
        UserDto stranger = createUser("Stranger", "stranger@test.com");
        HttpHeaders headers = createHeaders(stranger.getId());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/{bookingId}",
                HttpMethod.GET,
                request,
                String.class,
                booking.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("нет доступа");
    }

    @Test
    void createBooking_withInvalidDates_shouldReturnBadRequest() {
        ItemDto item = createItem(new ItemCreateDto("Drill", "Usable", true, null), owner.getId());
        BookingCreateDto invalidBooking = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1) // end < start
        );
        HttpHeaders headers = createHeaders(booker.getId());
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(invalidBooking, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/bookings", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("начало бронирования не должно быть позже конца");
    }

    @Test
    void createBooking_withNonExistentUser_shouldReturnNotFound() {
        int nonExistentUserId = 999;
        ItemDto item = createItem(new ItemCreateDto("Drill", "Usable", true, null), owner.getId());
        BookingCreateDto booking = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        HttpHeaders headers = createHeaders(nonExistentUserId);
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(booking, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/bookings", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("пользователь с id " + nonExistentUserId + " не найден");
    }

    @Test
    void createBooking_withNonExistentItem_shouldReturnNotFound() {
        int nonExistentItemId = 999;
        BookingCreateDto booking = new BookingCreateDto(
                nonExistentItemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        HttpHeaders headers = createHeaders(booker.getId());
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(booking, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/bookings", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("предмет с id " + nonExistentItemId + " не найден");
    }

    @Test
    void createBooking_withUnavailableItem_shouldReturnBadRequest() {
        // Делаем вещь недоступной
        ItemUpdateDto updateDto = new ItemUpdateDto(item.getName(), item.getDescription(), false);
        restTemplate.exchange(
                "/items/" + item.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto, createHeaders(owner.getId())),
                ItemDto.class
        );

        // Пытаемся забронировать
        BookingCreateDto bookingDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingDto, createHeaders(booker.getId()));
        ResponseEntity<String> response = restTemplate.postForEntity("/bookings", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("уже занят");
    }


    @Test
    void findAllBookingsByItemsOwner_withNoItems_shouldReturnNotFound() {
        UserDto noItemsOwner = createUser("NoItems", "noitems@test.com");

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(noItemsOwner.getId()));
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/owner?state=ALL",
                HttpMethod.GET,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("У пользователя " + noItemsOwner.getId() + " нет предметов");
    }

    @Test
    void findAllByBookerAndState_ALL_shouldReturnAllBookings() {
        // Создаём бронирования в разных состояниях
        BookingDto waiting = createBooking(item.getId(), booker.getId());
        BookingDto approved = createBooking(item.getId(), booker.getId());
        approveBooking(approved.getId(), owner.getId(), true);

        BookingDto rejected = createBooking(item.getId(), booker.getId());
        approveBooking(rejected.getId(), owner.getId(), false);

        // Запрашиваем все
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings?state=ALL",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void findAllByBookerAndState_WAITING_shouldReturnOnlyWaiting() {
        createBooking(item.getId(), booker.getId()); // waiting
        BookingDto approved = createBooking(item.getId(), booker.getId());
        approveBooking(approved.getId(), owner.getId(), true); // approved

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings?state=WAITING",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void findAllByBookerAndState_PAST_shouldReturnOnlyPast() {
        BookingDto past = createBooking(item.getId(), booker.getId());
        approveBooking(past.getId(), owner.getId(), true);

        createBooking(item.getId(), booker.getId()); // future

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings?state=PAST",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void findAllByBookerAndState_FUTURE_shouldReturnOnlyFuture() {
        createBooking(item.getId(), booker.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)); // past
        BookingDto future = createBooking(item.getId(), booker.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings?state=FUTURE",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getId()).isEqualTo(future.getId());
    }

    @Test
    void findAllByItemOwnerAndState_ALL_shouldReturnAllBookingsForOwner() {
        BookingDto b1 = createBooking(item.getId(), booker.getId());
        BookingDto b2 = createBooking(item.getId(), booker.getId());
        approveBooking(b2.getId(), owner.getId(), true);

        BookingDto b3 = createBooking(item.getId(), booker.getId());
        approveBooking(b3.getId(), owner.getId(), false);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings/owner?state=ALL",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(3);
    }

    @Test
    void findAllByItemOwnerAndState_PAST_shouldReturnOnlyPast() {
        BookingDto past = createBooking(item.getId(), booker.getId());
        approveBooking(past.getId(), owner.getId(), true);

        createBooking(item.getId(), booker.getId());

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                "/bookings/owner?state=PAST",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BookingDto> bookings = Arrays.asList(response.getBody());
        assertThat(bookings.size()).isEqualTo(2);
    }

    @Test
    void findAllByItemOwnerAndState_withNoItems_shouldReturnNotFound() {
        UserDto noItemsOwner = createUser("NoItems", "noitems@test.com");

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(noItemsOwner.getId()));
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/owner?state=ALL",
                HttpMethod.GET,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("У пользователя " + noItemsOwner.getId() + " нет предметов");
    }

    private UserDto createUser(String name, String email) {
        UserCreateDto userCreateDto = new UserCreateDto(name, email);
        return restTemplate.postForEntity("/users", userCreateDto, UserDto.class).getBody();
    }

    private ItemDto createItem(ItemCreateDto itemCreateDto, int userId) {
        HttpHeaders headers = createHeaders(userId);
        HttpEntity<ItemCreateDto> request = new HttpEntity<>(itemCreateDto, headers);
        return restTemplate.postForEntity("/items", request, ItemDto.class).getBody();
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

    private BookingDto createTestBooking() {
        ItemDto item = createItem(new ItemCreateDto("Дрель", "Простая дрель", true, null), owner.getId());
        BookingCreateDto bookingCreateDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        HttpHeaders headers = createHeaders(booker.getId());
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, headers);
        return restTemplate.postForEntity("/bookings", request, BookingDto.class).getBody();
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


    private HttpHeaders createHeaders(int userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        return headers;
    }

    private BookingDto createBooking(int itemId, int bookerId, LocalDateTime start, LocalDateTime end) {
        BookingCreateDto bookingCreateDto = new BookingCreateDto(itemId, start, end);
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(bookerId));
        return restTemplate.postForEntity("/bookings", request, BookingDto.class).getBody();
    }

}

