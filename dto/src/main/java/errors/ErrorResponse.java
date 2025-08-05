package errors;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String name;
    private final String description;
}
