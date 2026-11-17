package com.restaurante.whatsappbot.reservation.domain;

/**
 * Resultado de comprobar el número de personas de una reserva.
 */
public enum PartySizeCheck {

	/** Entre el mínimo efectivo y el máximo, ambos incluidos. */
	ALLOWED,

	/** Supera el máximo: no se reserva y se deriva a una persona. */
	REQUIRES_HUMAN,

	/** Menor que el mínimo efectivo (incluye 0 y negativos). */
	INVALID

}
