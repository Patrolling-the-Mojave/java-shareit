package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class RequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private RequestCreateDto newRequest;

    private ItemRequestDto requestDto;

    private static final int REQUESTER_ID = 1;

    @BeforeEach
    void setup() {
        newRequest = RequestCreateDto.builder().description("item request").build();

        requestDto = ItemRequestDto
                .builder()
                .id(1)
                .description(newRequest.getDescription())
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    public void testCreateRequest() throws Exception {
        when(requestService.createRequest(newRequest, REQUESTER_ID))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", REQUESTER_ID)
                        .content(objectMapper.writeValueAsString(newRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
    }

    @Test
    void findAllRequests_shouldReturnAllRequests() throws Exception {
        ItemRequestDto request2 = new ItemRequestDto(2, "second item request", LocalDateTime.now());

        when(requestService.findAll()).thenReturn(List.of(requestDto, request2));

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void findRequestsByRequester_shouldReturnUserRequests() throws Exception {
        RequestWithAnswersDto request1 = new RequestWithAnswersDto(1, "need an item", LocalDateTime.now(), Collections.emptyList());
        RequestWithAnswersDto request2 = new RequestWithAnswersDto(2, "need an item", LocalDateTime.now(), Collections.emptyList());

        when(requestService.findByRequesterId(REQUESTER_ID))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", REQUESTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void findRequestById_shouldReturnRequestWithAnswers() throws Exception {
        RequestWithAnswersDto expectedDto = new RequestWithAnswersDto(
                1,
                "need an item",
                LocalDateTime.now(),
                Collections.emptyList()
        );

        when(requestService.findByRequestId(REQUESTER_ID)).thenReturn(expectedDto);

        mockMvc.perform(get("/requests/{requestId}", REQUESTER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

    }
}
