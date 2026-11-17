package com.restaurante.whatsappbot.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurante.whatsappbot.TestcontainersConfiguration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ReservationRulesSeedDataTest {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	ReservationRulesSeedDataTest(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void seedsTuesdayToSundaySlots() {
		List<Map<String, Object>> slots = jdbcTemplate.queryForList(
				"select day_of_week, opens_at, closes_at, last_booking_at "
						+ "from reservation_time_slot order by day_of_week");

		assertThat(slots).hasSize(6);
		assertThat(slots).extracting(row -> ((Number) row.get("day_of_week")).intValue())
				.containsExactly(2, 3, 4, 5, 6, 7);
		assertThat(slots).allSatisfy(row -> {
			assertThat(toLocalTime(row.get("opens_at"))).isEqualTo(LocalTime.of(13, 0));
			assertThat(toLocalTime(row.get("closes_at"))).isEqualTo(LocalTime.of(23, 0));
			assertThat(toLocalTime(row.get("last_booking_at"))).isEqualTo(LocalTime.of(22, 30));
		});
	}

	@Test
	void seedsNoSlotsOnMonday() {
		Integer mondaySlots = jdbcTemplate.queryForObject(
				"select count(*) from reservation_time_slot where day_of_week = 1", Integer.class);

		assertThat(mondaySlots).isZero();
	}

	@Test
	void seedsMaxPartySizeFifteenAndNoMinimum() {
		Map<String, Object> settings = jdbcTemplate.queryForMap(
				"select id, max_party_size, min_party_size from reservation_settings");

		assertThat(((Number) settings.get("id")).intValue()).isEqualTo(1);
		assertThat(((Number) settings.get("max_party_size")).intValue()).isEqualTo(15);
		assertThat(settings.get("min_party_size")).isNull();
	}

	private static LocalTime toLocalTime(Object value) {
		return ((java.sql.Time) value).toLocalTime();
	}

}
