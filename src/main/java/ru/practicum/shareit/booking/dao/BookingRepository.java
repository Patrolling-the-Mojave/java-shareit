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
            WHERE b.booker.id = :bookerId
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllByBookerId(@Param("bookerId") int bookerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :bookerId AND
            b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllCurrentByBooker(@Param("bookerId") int bookerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :bookerId AND
            b.end <= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllPastByBooker(@Param("bookerId") int bookerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :bookerId AND
            b.start >= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllFutureByBooker(@Param("bookerId") int bookerId);


    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :bookerId AND
            b.status = 'WAITING'
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllWaitingByBooker(@Param("bookerId") int bookerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item
            WHERE b.booker.id = :bookerId AND
            b.status = 'REJECTED'
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllRejectedByBooker(@Param("bookerId") int bookerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllByItemOwner(@Param("ownerId") int ownerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId AND
            b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllCurrentByItemOwner(@Param("ownerId") int ownerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId AND
            b.end <= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllPastByItemOwner(@Param("ownerId") int itemOwner);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId AND
            b.start >= CURRENT_TIMESTAMP
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllFutureByItemOwner(@Param("ownerId") int itemOwner);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId AND
            b.status = 'WAITING'
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllWaitingByItemOwner(@Param("ownerId") int itemOwner);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.booker
            JOIN FETCH b.item i
            WHERE i.owner.id = :ownerId AND
            b.status = 'REJECTED'
            ORDER BY b.start DESC
            """)
    Collection<Booking> findAllRejectedByItemOwner(@Param("ownerId") int itemOwner);

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
