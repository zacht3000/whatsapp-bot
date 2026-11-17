package com.restaurante.whatsappbot.reservation.repository;

import com.restaurante.whatsappbot.reservation.domain.ReservationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSettingsRepository extends JpaRepository<ReservationSettings, Short> {

}
