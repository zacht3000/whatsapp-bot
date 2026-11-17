package com.restaurante.whatsappbot.reservation.domain;

/**
 * Resultado de comprobar si una fecha y hora se puede reservar.
 */
public enum ReservationTimeCheck {

	/** Fecha y hora futuras y dentro de alguna franja del día. */
	ALLOWED,

	/** La fecha y hora no es posterior al momento actual. */
	PAST,

	/** El día tiene franjas, pero la hora no cae en ninguna. */
	OUTSIDE_HOURS,

	/** El día no tiene franjas: el restaurante está cerrado. */
	CLOSED_DAY

}
