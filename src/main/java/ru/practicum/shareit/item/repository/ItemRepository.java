package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemByOwner_IdIs(Long ownerId);

    List<Item> findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(String text1, String text2);
}
