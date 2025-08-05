package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemCreateDto item;

    private static final int USER_ID = 1;

    @BeforeEach
    void setUp() {
        item = new ItemCreateDto("item", "description", true, 1);
    }

    @Test
    public void testCreate() throws Exception {
        when(itemService.create(item, USER_ID))
                .thenReturn(new ItemDto(1, item.getName(), item.getDescription(), item.getAvailable()));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())));
    }

    @Test
    public void itemUpdateTest() throws Exception {
        ItemUpdateDto updatedItem = new ItemUpdateDto("updatedName", "updatedDescription", true);
        when(itemService.update(updatedItem, 1, USER_ID))
                .thenReturn(new ItemDto(1, updatedItem.getName(), updatedItem.getDescription(), updatedItem.getAvailable()));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(updatedItem.getName())))
                .andExpect(jsonPath("$.description", is(updatedItem.getDescription())));
    }

    @Test
    public void findByIdTest() throws Exception {
        when(itemService.findById(1))
                .thenReturn(ItemWithCommentsDto
                        .builder()
                        .id(1)
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .lastBooking(LocalDateTime.now().minusHours(10))
                        .nextBooking(LocalDateTime.now().plusHours(10))
                        .comments(Collections.emptySet())
                        .build());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())));
    }

    @Test
    void getItemsByOwnerTest() throws Exception {
        ItemOwnerDto item1 = new ItemOwnerDto(1, "item1", "desc1", true, null, null, Collections.emptySet());
        ItemOwnerDto item2 = new ItemOwnerDto(2, "item2", "desc2", true, null, null, Collections.emptySet());

        when(itemService.getItemsWithBookingsAndComments(USER_ID))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("item1"))
                .andExpect(jsonPath("$[1].name").value("item2"));
    }

    @Test
    public void testSearch_shouldReturnItemsContainingText() throws Exception {
        ItemDto itemDto1 = new ItemDto(1, "item1", "desc1", true);
        ItemDto itemDto2 = new ItemDto(2, "item2", "desc2", true);

        final String query = "item";

        when(itemService.search(query))
                .thenReturn(List.of(itemDto1, itemDto2));

        mockMvc.perform(get("/items/search").param("text", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(itemDto1.getName())));

    }

    @Test
    public void addComment_shouldReturnCreatedComment() throws Exception {
        CommentCreateDto createDto = new CommentCreateDto("like!");
        CommentDto expectedDto = new CommentDto(1, "like!", "user", LocalDateTime.now());

        when(itemService.postComment(createDto, USER_ID, 1))
                .thenReturn(expectedDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("like!"))
                .andExpect(jsonPath("$.authorName").value("user"));
    }
}
