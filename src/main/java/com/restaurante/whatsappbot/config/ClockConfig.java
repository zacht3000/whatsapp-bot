package com.restaurante.whatsappbot.config;

import com.restaurante.whatsappbot.reservation.domain.ReservationTimeZone;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfig {

	@Bean
	Clock clock() {
		return Clock.system(ReservationTimeZone.ZONE_ID);
	}

}
