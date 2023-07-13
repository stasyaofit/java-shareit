package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemDto saveItem(ItemDto itemDto, Long userId) {
        return ItemMapper.toItemDto(itemStorage.saveItem(ItemMapper.toItem(itemDto), userStorage.getUser(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с id = " + userId + " не найден."))));
    }

    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        checkIdAndItemExists(itemId);
        Item updateItem = itemStorage.getItemById(itemId);
        if (!Objects.equals(updateItem.getOwner().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Пользователь с id = " + " не является собственником вещи.");
        }

        updateItem.setName(itemDto.getName() != null ? itemDto.getName() : updateItem.getName());
        updateItem.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : updateItem.getDescription());
        updateItem.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : updateItem.getAvailable());
        validateItem(updateItem);
        log.info("Вещь {} обновлена.", updateItem);
        return ItemMapper.toItemDto(itemStorage.updateItem(updateItem));
    }

    @Override
    public void deleteItem(Long itemId) {
        if (!itemStorage.deleteItem(itemId)) {
            throw new ItemNotFoundException("Вещь с id = " + itemId + " не найдена.");
        }
        log.info("Вещь с id = {} удалена.", itemId);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        checkIdAndItemExists(itemId);
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        checkIdAndUserExists(userId);
        return ItemMapper.toItemDtoList(itemStorage.getUserItems(userId));
    }

    @Override
    public List<ItemDto> findAvailableItemsByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemStorage.findAvailableItemsByText(text));
    }

    private void checkIdAndItemExists(Long itemId) {
        if (itemId < 0 || itemStorage.getItemById(itemId) == null) {
            throw new ItemNotFoundException("Вещь с id = " + itemId + " не найдена.");
        }
    }

    private void checkIdAndUserExists(Long userId) {
        if (userId < 0 || userStorage.getUser(userId).isEmpty()) {
            throw new UserNotFoundException("Пользователь c id = " + userId + " не найден.");
        }
    }

    private void validateItem(Item item) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
