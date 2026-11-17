package com.restaurante.whatsappbot.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.restaurante.whatsappbot.reservation.domain.PartySizeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationConfigurationException;
import com.restaurante.whatsappbot.reservation.domain.ReservationSettings;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeZone;
import com.restaurante.whatsappbot.reservation.repository.ReservationSettingsRepository;
import com.restaurante.whatsappbot.reservation.repository.ReservationTimeSlotRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationRulesServicePartySizeTest {

	@Mock
	private ReservationTimeSlotRepository timeSlotRepository;

	@Mock
	private ReservationSettingsRepository settingsRepository;

	private ReservationRulesService service;

	@BeforeEach
	void setUp() {
		service = new ReservationRulesService(timeSlotRepository, settingsRepository,
				Clock.system(ReservationTimeZone.ZONE_ID));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 15})
	void withinInitialLimitsIsAllowed(int partySize) {
		givenSettings(15, null);

		assertThat(service.checkPartySize(partySize)).isEqualTo(PartySizeCheck.ALLOWED);
	}

	@Test
	void aboveMaximumRequiresHuman() {
		givenSettings(15, null);

		assertThat(service.checkPartySize(16)).isEqualTo(PartySizeCheck.REQUIRES_HUMAN);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -3})
	void zeroOrNegativeIsInvalid(int partySize) {
		givenSettings(15, null);

		assertThat(service.checkPartySize(partySize)).isEqualTo(PartySizeCheck.INVALID);
	}

	@Test
	void configuredMinimumIsApplied() {
		givenSettings(15, 4);

		assertThat(service.checkPartySize(3)).isEqualTo(PartySizeCheck.INVALID);
		assertThat(service.checkPartySize(4)).isEqualTo(PartySizeCheck.ALLOWED);
	}

	@Test
	void missingSettingsRowFailsExplicitly() {
		when(settingsRepository.findById(ReservationSettings.SINGLETON_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.checkPartySize(4))
				.isInstanceOf(ReservationConfigurationException.class);
	}

	private void givenSettings(int max, Integer min) {
		when(settingsRepository.findById(ReservationSettings.SINGLETON_ID))
				.thenReturn(Optional.of(new ReservationSettings(max, min)));
	}

}
