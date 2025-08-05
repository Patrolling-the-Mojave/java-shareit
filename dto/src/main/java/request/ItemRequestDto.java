package request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@AllArgsConstructor
public class ItemRequestDto {
    private Integer id;
    private String description;
    private LocalDateTime created;
}
