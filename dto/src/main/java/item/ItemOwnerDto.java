package item;

import item.comment.CommentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
