package com.restaurante.whatsappbot.reservation.domain;

/**
 * La configuración de reservas en base de datos falta o es incoherente.
 * Es un error de despliegue/datos, no un resultado de negocio.
 */
public class ReservationConfigurationException extends IllegalStateException {

	public ReservationConfigurationException(String message) {
		super(message);
	}

}
