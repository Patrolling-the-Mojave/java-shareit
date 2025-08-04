package request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateDto {
    @NotBlank
    @Size(max = 500)
    private String description;
}
