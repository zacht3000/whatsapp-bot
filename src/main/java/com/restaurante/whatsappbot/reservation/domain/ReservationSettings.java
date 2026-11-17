package com.restaurante.whatsappbot.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ajustes generales de reserva. Tabla de fila única ({@code id = 1}).
 */
@Entity
@Table(name = "reservation_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSettings {

	public static final short SINGLETON_ID = 1;

	@Id
	@Column(name = "id", columnDefinition = "smallint")
	private Short id;

	@Column(name = "max_party_size", nullable = false)
	private Integer maxPartySize;

	/** Mínimo opcional; {@code null} significa sin mínimo (mínimo efectivo 1). */
	@Column(name = "min_party_size")
	private Integer minPartySize;

	public ReservationSettings(Integer maxPartySize, Integer minPartySize) {
		this.id = SINGLETON_ID;
		this.maxPartySize = maxPartySize;
		this.minPartySize = minPartySize;
	}

}
