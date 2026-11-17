package com.restaurante.whatsappbot.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.restaurante.whatsappbot.reservation.domain.ReservationTimeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeSlot;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeZone;
import com.restaurante.whatsappbot.reservation.repository.ReservationSettingsRepository;
import com.restaurante.whatsappbot.reservation.repository.ReservationTimeSlotRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationRulesServiceDateTimeTest {

	@Mock
	private ReservationTimeSlotRepository timeSlotRepository;

	@Mock
	private ReservationSettingsRepository settingsRepository;

	// --- Rechazo de fechas pasadas ---

	@Test
	void previousDayIsPast() {
		ReservationRulesService service = serviceAt(LocalDateTime.of(2026, 11, 18, 10, 0));

		ReservationTimeCheck result = service.checkDateTime(LocalDateTime.of(2026, 11, 17, 14, 0));

		assertThat(result).isEqualTo(ReservationTimeCheck.PAST);
	}

	@Test
	void earlierHourSameDayIsPast() {
		ReservationRulesService service = serviceAt(LocalDateTime.of(2026, 11, 18, 15, 0));

		ReservationTimeCheck result = service.checkDateTime(LocalDateTime.of(2026, 11, 18, 14, 0));

		assertThat(result).isEqualTo(ReservationTimeCheck.PAST);
	}

	@Test
	void currentMomentIsPast() {
		ReservationRulesService service = serviceAt(LocalDateTime.of(2026, 11, 18, 15, 0));

		ReservationTimeCheck result = service.checkDateTime(LocalDateTime.of(2026, 11, 18, 15, 0));

		assertThat(result).isEqualTo(ReservationTimeCheck.PAST);
	}

	@Test
	void laterHourSameDayIsAllowed() {
		ReservationRulesService service = serviceAt(LocalDateTime.of(2026, 11, 18, 15, 0));
		givenInitialSlot(DayOfWeek.WEDNESDAY);

		ReservationTimeCheck result = service.checkDateTime(LocalDateTime.of(2026, 11, 18, 21, 0));

		assertThat(result).isEqualTo(ReservationTimeCheck.ALLOWED);
	}

	@Test
	void pastMondayIsPastNotClosed() {
		ReservationRulesService service = serviceAt(LocalDateTime.of(2026, 11, 18, 10, 0));

		ReservationTimeCheck result = service.checkDateTime(LocalDateTime.of(2026, 11, 16, 14, 0));

		assertThat(result).isEqualTo(ReservationTimeCheck.PAST);
		verifyNoInteractions(timeSlotRepository);
	}

	// --- Admisión de fecha y hora de reserva (ahora: miércoles 18/11/2026 10:00) ---

	@Test
	void saturdayAtNinePmIsAllowed() {
		ReservationRulesService service = serviceAtDefaultNow();
		givenInitialSlot(DayOfWeek.SATURDAY);

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 21, 21, 0)))
				.isEqualTo(ReservationTimeCheck.ALLOWED);
	}

	@Test
	void slotBoundariesAreIncluded() {
		ReservationRulesService service = serviceAtDefaultNow();
		givenInitialSlot(DayOfWeek.WEDNESDAY);

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 25, 13, 0)))
				.isEqualTo(ReservationTimeCheck.ALLOWED);
		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 25, 22, 30)))
				.isEqualTo(ReservationTimeCheck.ALLOWED);
	}

	@Test
	void beforeOpeningIsOutsideHours() {
		ReservationRulesService service = serviceAtDefaultNow();
		givenInitialSlot(DayOfWeek.THURSDAY);

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 19, 12, 59)))
				.isEqualTo(ReservationTimeCheck.OUTSIDE_HOURS);
	}

	@Test
	void afterLastBookingIsOutsideHours() {
		ReservationRulesService service = serviceAtDefaultNow();
		givenInitialSlot(DayOfWeek.FRIDAY);

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 20, 22, 31)))
				.isEqualTo(ReservationTimeCheck.OUTSIDE_HOURS);
	}

	@Test
	void mondayIsClosed() {
		ReservationRulesService service = serviceAtDefaultNow();
		when(timeSlotRepository.findByDayOfWeek(DayOfWeek.MONDAY)).thenReturn(List.of());

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 23, 14, 0)))
				.isEqualTo(ReservationTimeCheck.CLOSED_DAY);
	}

	// --- Día sin franjas (cualquier día configurado sin franjas) ---

	@Test
	void dayWithoutSlotsIsClosed() {
		ReservationRulesService service = serviceAtDefaultNow();
		when(timeSlotRepository.findByDayOfWeek(DayOfWeek.TUESDAY)).thenReturn(List.of());

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 24, 14, 0)))
				.isEqualTo(ReservationTimeCheck.CLOSED_DAY);
	}

	@Test
	void anySlotOfTheDayAdmitsTheTime() {
		ReservationRulesService service = serviceAtDefaultNow();
		when(timeSlotRepository.findByDayOfWeek(DayOfWeek.SATURDAY)).thenReturn(List.of(
				new ReservationTimeSlot(DayOfWeek.SATURDAY, LocalTime.of(13, 0), LocalTime.of(16, 0),
						LocalTime.of(15, 0)),
				new ReservationTimeSlot(DayOfWeek.SATURDAY, LocalTime.of(20, 0), LocalTime.of(23, 0),
						LocalTime.of(22, 30))));

		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 21, 21, 0)))
				.isEqualTo(ReservationTimeCheck.ALLOWED);
		assertThat(service.checkDateTime(LocalDateTime.of(2026, 11, 21, 17, 0)))
				.isEqualTo(ReservationTimeCheck.OUTSIDE_HOURS);
	}

	private ReservationRulesService serviceAtDefaultNow() {
		return serviceAt(LocalDateTime.of(2026, 11, 18, 10, 0));
	}

	private ReservationRulesService serviceAt(LocalDateTime nowInMadrid) {
		Clock clock = Clock.fixed(nowInMadrid.atZone(ReservationTimeZone.ZONE_ID).toInstant(),
				ReservationTimeZone.ZONE_ID);
		return new ReservationRulesService(timeSlotRepository, settingsRepository, clock);
	}

	private void givenInitialSlot(DayOfWeek day) {
		when(timeSlotRepository.findByDayOfWeek(day)).thenReturn(List.of(
				new ReservationTimeSlot(day, LocalTime.of(13, 0), LocalTime.of(23, 0), LocalTime.of(22, 30))));
	}

}
