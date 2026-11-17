package com.restaurante.whatsappbot.reservation.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

/**
 * Guarda {@link DayOfWeek} con la numeración ISO-8601 (1 = lunes ... 7 = domingo).
 */
@Converter
public class IsoDayOfWeekConverter implements AttributeConverter<DayOfWeek, Short> {

	@Override
	public Short convertToDatabaseColumn(DayOfWeek dayOfWeek) {
		return dayOfWeek == null ? null : (short) dayOfWeek.getValue();
	}

	@Override
	public DayOfWeek convertToEntityAttribute(Short value) {
		return value == null ? null : DayOfWeek.of(value);
	}

}
