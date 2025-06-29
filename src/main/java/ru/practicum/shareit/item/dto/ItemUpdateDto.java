package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemUpdateDto {
    @Size(max = 25)
    private String name;
    @Size(max = 200)
    private String description;
    private Boolean available;
}
