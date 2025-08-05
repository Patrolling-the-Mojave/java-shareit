package item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ItemCreateDto {
    @NotBlank
    @Size(max = 25)
    private String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    @NotNull
    private Boolean available;
    private Integer requestId;

}
