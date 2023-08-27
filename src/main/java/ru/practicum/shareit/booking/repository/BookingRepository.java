package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                          LocalDateTime now1,
                                                                          LocalDateTime now2,
                                                                          Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStatusEquals(Long bookerId, Status status, Pageable pageable);

    List<Booking> findByItem_OwnerOrderByStartDesc(User owner, Pageable pageable);

    List<Booking> findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner,
                                                                            LocalDateTime now1,
                                                                            LocalDateTime now2,
                                                                            Pageable pageable);

    List<Booking> findByItem_OwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime now, Pageable pageable);

    List<Booking> findByItem_OwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime now, Pageable pageable);

    List<Booking> findByItem_OwnerAndStatusEquals(User owner, Status status, Pageable pageable);

    Booking findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(Long itemId,
                                                                             LocalDateTime moment,
                                                                             Status status);

    Booking findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(Long itemId,
                                                                               LocalDateTime moment,
                                                                               Status status);

    Optional<Booking> findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(Long authorId, Long itemId,
                                                                            LocalDateTime now, Status status);

}
