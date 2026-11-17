package com.restaurante.whatsappbot.reservation.service;

import com.restaurante.whatsappbot.reservation.domain.PartySizeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationConfigurationException;
import com.restaurante.whatsappbot.reservation.domain.ReservationSettings;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeSlot;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeZone;
import com.restaurante.whatsappbot.reservation.repository.ReservationSettingsRepository;
import com.restaurante.whatsappbot.reservation.repository.ReservationTimeSlotRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationRulesService {

	private final ReservationTimeSlotRepository timeSlotRepository;
	private final ReservationSettingsRepository settingsRepository;
	private final Clock clock;

	public ReservationRulesService(ReservationTimeSlotRepository timeSlotRepository,
			ReservationSettingsRepository settingsRepository, Clock clock) {
		this.timeSlotRepository = timeSlotRepository;
		this.settingsRepository = settingsRepository;
		this.clock = clock;
	}

	/**
	 * Comprueba si se puede reservar en la fecha y hora indicadas.
	 *
	 * @param requested fecha y hora en hora local de {@link ReservationTimeZone#ZONE_ID}
	 */
	public ReservationTimeCheck checkDateTime(LocalDateTime requested) {
		if (!requested.isAfter(LocalDateTime.now(clock.withZone(ReservationTimeZone.ZONE_ID)))) {
			return ReservationTimeCheck.PAST;
		}
		List<ReservationTimeSlot> slots = timeSlotRepository.findByDayOfWeek(requested.getDayOfWeek());
		if (slots.isEmpty()) {
			return ReservationTimeCheck.CLOSED_DAY;
		}
		LocalTime time = requested.toLocalTime();
		boolean insideAnySlot = slots.stream()
				.anyMatch(slot -> !time.isBefore(slot.getOpensAt()) && !time.isAfter(slot.getLastBookingAt()));
		return insideAnySlot ? ReservationTimeCheck.ALLOWED : ReservationTimeCheck.OUTSIDE_HOURS;
	}

	/**
	 * Clasifica el número de personas según los límites configurados en base de datos.
	 *
	 * @throws ReservationConfigurationException si falta la fila de ajustes
	 */
	public PartySizeCheck checkPartySize(int partySize) {
		ReservationSettings settings = settingsRepository.findById(ReservationSettings.SINGLETON_ID)
				.orElseThrow(() -> new ReservationConfigurationException(
						"Falta la fila de ajustes de reserva (reservation_settings.id = 1)"));
		int effectiveMinimum = settings.getMinPartySize() == null ? 1 : settings.getMinPartySize();
		if (partySize < effectiveMinimum) {
			return PartySizeCheck.INVALID;
		}
		if (partySize > settings.getMaxPartySize()) {
			return PartySizeCheck.REQUIRES_HUMAN;
		}
		return PartySizeCheck.ALLOWED;
	}

}
