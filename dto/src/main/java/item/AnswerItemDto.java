package item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerItemDto {
    private Integer id;
    private String name;
    private Integer ownerId;
}
