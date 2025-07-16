package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :userId
            AND (
            :state = 'ALL' OR
            (:state = 'CURRENT' AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP) OR
            (:state = 'PAST' AND b.end <= CURRENT_TIMESTAMP) OR
            (:state = 'FUTURE' AND b.start >= CURRENT_TIMESTAMP) OR
            (:state = 'WAITING' AND b.status = 'WAITING') OR
            (:state = 'REJECTED' AND b.status = 'REJECTED')
            )
            ORDER BY b.start DESC
            """)
    Collection<Booking> findByBookerAndState(@Param("userId") int userId, @Param("state") String state);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId
            AND (
            :state = 'ALL' OR
            (:state = 'CURRENT' AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP) OR
            (:state = 'PAST' AND b.end <= CURRENT_TIMESTAMP) OR
            (:state = 'FUTURE' AND b.start >= CURRENT_TIMESTAMP) OR
            (:state = 'WAITING' AND b.status = 'WAITING') OR
            (:state = 'REJECTED' AND b.status = 'REJECTED')
            )
            ORDER BY b.start DESC
            """)
    Collection<Booking> findByOwnerIdAndState(@Param("ownerId") int ownerId, @Param("state") String state);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.id = (
                SELECT b2.id FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.end < :now
                AND b2.status = 'APPROVED'
                ORDER BY b2.end DESC
                LIMIT 1
            )
            AND b.item.id IN :itemIds
            """)
    Collection<Booking> findLasBookings(@Param("itemIds") List<Integer> itemIds, @Param("now") LocalDateTime now);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.id = (
                SELECT b2.id FROM Booking b2
                WHERE b2.item.id = b.item.id
                AND b2.start > :now
                AND b2.status = 'APPROVED'
                ORDER BY b2.start ASC
                LIMIT 1
            )
            AND b.item.id IN :itemIds
            """)
    Collection<Booking> findNextBookings(@Param("itemIds") List<Integer> itemIds, @Param("now") LocalDateTime now);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.booker.id = :bookerId AND
            b.item.id = :itemId AND
            b.status = 'APPROVED' AND
            b.end <= CURRENT_TIMESTAMP
            """)
    boolean existByBookerIdAndItemId(@Param("bookerId") int bookerId, @Param("itemId") int itemId);

}
