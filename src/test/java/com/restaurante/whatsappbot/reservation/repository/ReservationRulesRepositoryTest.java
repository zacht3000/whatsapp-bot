package com.restaurante.whatsappbot.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurante.whatsappbot.TestcontainersConfiguration;
import com.restaurante.whatsappbot.reservation.domain.ReservationSettings;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeSlot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ReservationRulesRepositoryTest {

	private final ReservationTimeSlotRepository timeSlotRepository;
	private final ReservationSettingsRepository settingsRepository;

	@Autowired
	ReservationRulesRepositoryTest(ReservationTimeSlotRepository timeSlotRepository,
			ReservationSettingsRepository settingsRepository) {
		this.timeSlotRepository = timeSlotRepository;
		this.settingsRepository = settingsRepository;
	}

	@Test
	void readsSeededSlotForSaturday() {
		List<ReservationTimeSlot> slots = timeSlotRepository.findByDayOfWeek(DayOfWeek.SATURDAY);

		assertThat(slots).singleElement().satisfies(slot -> {
			assertThat(slot.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
			assertThat(slot.getOpensAt()).isEqualTo(LocalTime.of(13, 0));
			assertThat(slot.getClosesAt()).isEqualTo(LocalTime.of(23, 0));
			assertThat(slot.getLastBookingAt()).isEqualTo(LocalTime.of(22, 30));
		});
	}

	@Test
	void readsNoSlotsForMonday() {
		assertThat(timeSlotRepository.findByDayOfWeek(DayOfWeek.MONDAY)).isEmpty();
	}

	@Test
	void readsSeededSettings() {
		ReservationSettings settings = settingsRepository.findById(ReservationSettings.SINGLETON_ID)
				.orElseThrow();

		assertThat(settings.getMaxPartySize()).isEqualTo(15);
		assertThat(settings.getMinPartySize()).isNull();
	}

}
