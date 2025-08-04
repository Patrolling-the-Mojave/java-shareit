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

    private UserDto createUser(String email, String name) {
        UserCreateDto userCreateDto = new UserCreateDto(name, email);
        ResponseEntity<UserDto> response = restTemplate.postForEntity("/users", userCreateDto, UserDto.class);
        return response.getBody();
    }

    private ItemDto createItem(ItemCreateDto itemCreateDto, int ownerId) {
        HttpEntity<ItemCreateDto> request = new HttpEntity<>(itemCreateDto, createHeaders(ownerId));
        ResponseEntity<ItemDto> response = restTemplate.postForEntity("/items", request, ItemDto.class);
        return response.getBody();
    }

    private HttpHeaders createHeaders(int userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
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
    void createBooking_withUnavailableItem_shouldReturnBadRequest() {
        ItemUpdateDto updateDto = new ItemUpdateDto(item.getName(), item.getDescription(), false);
        restTemplate.exchange(
                "/items/" + item.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(updateDto, createHeaders(owner.getId())),
                ItemDto.class);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(item.getId(), start, end);

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(booker.getId()));
        ResponseEntity<String> response = restTemplate.postForEntity("/bookings", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("уже занят");
    }

    private BookingDto createTestBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(item.getId(), start, end);

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(booker.getId()));
        ResponseEntity<BookingDto> response = restTemplate.postForEntity("/bookings", request, BookingDto.class);
        return response.getBody();
    }
}

