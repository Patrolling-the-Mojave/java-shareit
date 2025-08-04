package request;

import item.AnswerItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RequestWithAnswersDto {
    private Integer id;
    private String description;
    private LocalDateTime created;
    private List<AnswerItemDto> items;
}
