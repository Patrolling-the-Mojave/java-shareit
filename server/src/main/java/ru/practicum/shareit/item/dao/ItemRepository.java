package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    Collection<Item> findByOwnerId(@Param("ownerId") int ownerId);

    @Query("""
            SELECT i FROM Item i
            JOIN FETCH i.owner
            WHERE (
            LOWER(i.name) LIKE :query OR
            LOWER(i.description) LIKE :query
            )
            AND i.available=true
            """)
    Collection<Item> searchAvailableItems(@Param("query") String query);

    @Query("""
            SELECT COUNT(i.id) > 0 FROM Item i
            WHERE i.owner.id = :ownerId
            """)
    boolean existByOwnerId(@Param("ownerId") int ownerId);

    @Query("""
            SELECT i FROM Item i
            JOIN FETCH i.owner
            JOIN FETCH i.request r
            WHERE r.id IN :requestIds
            """)
    Collection<Item> findAllWhereRequestIdIn(@Param("requestIds") List<Integer> requestIds);

    @Query("""
            SELECT i FROM Item i
            JOIN FETCH i.owner
            JOIN FETCH i.request r
            WHERE r.id = :requestId
            """)
    Collection<Item> findAllByRequestId(@Param("requestId") int requestId);

}
