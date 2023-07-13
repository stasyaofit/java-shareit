package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    Item saveItem(Item item, User user);

    Item updateItem(Item item);

    boolean deleteItem(Long itemId);

    Item getItemById(Long itemId);

    List<Item> getUserItems(Long userId);

    List<Item> findAvailableItemsByText(String text);
}
