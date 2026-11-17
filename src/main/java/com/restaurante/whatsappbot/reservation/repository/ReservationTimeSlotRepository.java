package com.restaurante.whatsappbot.reservation.repository;

import com.restaurante.whatsappbot.reservation.domain.ReservationTimeSlot;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeSlotRepository extends JpaRepository<ReservationTimeSlot, Long> {

	List<ReservationTimeSlot> findByDayOfWeek(DayOfWeek dayOfWeek);

}
