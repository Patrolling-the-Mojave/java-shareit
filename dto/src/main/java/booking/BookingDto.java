package booking;

import booking.enums.BookingStatus;
import item.ItemShortDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import user.UserShortDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@AllArgsConstructor
public class BookingDto {
    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    private UserShortDto booker;
    private ItemShortDto item;
    private BookingStatus status;
}
