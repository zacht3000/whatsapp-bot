package com.restaurante.whatsappbot.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Franja de reserva de un día de la semana. Un día sin franjas está cerrado.
 */
@Entity
@Table(name = "reservation_time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationTimeSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = IsoDayOfWeekConverter.class)
	@Column(name = "day_of_week", nullable = false, columnDefinition = "smallint")
	private DayOfWeek dayOfWeek;

	@Column(name = "opens_at", nullable = false)
	private LocalTime opensAt;

	@Column(name = "closes_at", nullable = false)
	private LocalTime closesAt;

	@Column(name = "last_booking_at", nullable = false)
	private LocalTime lastBookingAt;

	public ReservationTimeSlot(DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt,
			LocalTime lastBookingAt) {
		this.dayOfWeek = dayOfWeek;
		this.opensAt = opensAt;
		this.closesAt = closesAt;
		this.lastBookingAt = lastBookingAt;
	}

}
