package item;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemUpdateDto {
    @Size(max = 25)
    private String name;
    @Size(max = 200)
    private String description;
    private Boolean available;
}
