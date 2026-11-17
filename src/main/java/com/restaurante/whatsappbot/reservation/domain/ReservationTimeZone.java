package com.restaurante.whatsappbot.reservation.domain;

import java.time.ZoneId;

/**
 * Zona horaria del restaurante. Las fechas y horas de reserva se expresan en hora local de España.
 */
public final class ReservationTimeZone {

	public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");

	private ReservationTimeZone() {
	}

}
