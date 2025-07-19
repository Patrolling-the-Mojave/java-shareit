package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemOwnerDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private LocalDateTime last;
    private LocalDateTime next;
    private Set<CommentDto> comments;

}
