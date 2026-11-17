package com.restaurante.whatsappbot.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurante.whatsappbot.TestcontainersConfiguration;
import com.restaurante.whatsappbot.reservation.domain.PartySizeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeCheck;
import com.restaurante.whatsappbot.reservation.domain.ReservationTimeZone;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Escenarios "sin desplegar" de la spec: un cambio en base de datos se refleja en la siguiente consulta.
 * Sin transacción de test, para que cada consulta del servicio vaya en su propia transacción como en producción.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ReservationRulesService.class,
		ReservationRulesServiceIntegrationTest.FixedClockConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReservationRulesServiceIntegrationTest {

	/** Martes 24/11/2026 a las 22:15 (hora de Madrid), futuro respecto al reloj fijo. */
	private static final LocalDateTime TUESDAY_22_15 = LocalDateTime.of(2026, 11, 24, 22, 15);

	private final ReservationRulesService service;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	ReservationRulesServiceIntegrationTest(ReservationRulesService service, JdbcTemplate jdbcTemplate) {
		this.service = service;
		this.jdbcTemplate = jdbcTemplate;
	}

	@AfterEach
	void restoreSeedValues() {
		jdbcTemplate.update("update reservation_time_slot set last_booking_at = '22:30' where day_of_week = 2");
		jdbcTemplate.update("update reservation_settings set max_party_size = 15 where id = 1");
	}

	@Test
	void lastBookingChangeAppliesWithoutRedeploy() {
		assertThat(service.checkDateTime(TUESDAY_22_15)).isEqualTo(ReservationTimeCheck.ALLOWED);

		jdbcTemplate.update("update reservation_time_slot set last_booking_at = '22:00' where day_of_week = 2");

		assertThat(service.checkDateTime(TUESDAY_22_15)).isEqualTo(ReservationTimeCheck.OUTSIDE_HOURS);
	}

	@Test
	void maxPartySizeChangeAppliesWithoutRedeploy() {
		assertThat(service.checkPartySize(13)).isEqualTo(PartySizeCheck.ALLOWED);

		jdbcTemplate.update("update reservation_settings set max_party_size = 12 where id = 1");

		assertThat(service.checkPartySize(13)).isEqualTo(PartySizeCheck.REQUIRES_HUMAN);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfig {

		@Bean
		Clock clock() {
			return Clock.fixed(LocalDateTime.of(2026, 11, 18, 10, 0).atZone(ReservationTimeZone.ZONE_ID).toInstant(),
					ReservationTimeZone.ZONE_ID);
		}

	}

}
