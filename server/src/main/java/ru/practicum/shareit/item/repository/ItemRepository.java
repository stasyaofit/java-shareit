package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemByOwner_IdIs(Long ownerId, Pageable pageable);

    List<Item> findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(String text1,
                                                                               String text2,
                                                                               Pageable pageable);

    List<Item> findAllByRequest_IdIn(List<Long> requestIds);
}
