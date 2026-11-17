package com.restaurante.whatsappbot.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.restaurante.whatsappbot.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ReservationRulesSchemaConstraintsTest {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	ReservationRulesSchemaConstraintsTest(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void rejectsLastBookingAfterClosing() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into reservation_time_slot (day_of_week, opens_at, closes_at, last_booking_at) "
						+ "values (2, '13:00', '23:00', '23:30')"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsDayOfWeekOutOfRange() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into reservation_time_slot (day_of_week, opens_at, closes_at, last_booking_at) "
						+ "values (8, '13:00', '23:00', '22:30')"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsZeroMaxPartySize() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update reservation_settings set max_party_size = 0 where id = 1"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsMinGreaterThanMax() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update reservation_settings set min_party_size = 16 where id = 1"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsSecondSettingsRow() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into reservation_settings (id, max_party_size, min_party_size) values (2, 10, null)"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

}
