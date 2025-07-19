package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

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

}
