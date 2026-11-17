-- Horario inicial: de martes (2) a domingo (7), de 13:00 a 23:00, última reserva a las 22:30.
-- El lunes (1) no tiene franjas: cerrado.
insert into reservation_time_slot (day_of_week, opens_at, closes_at, last_booking_at) values
    (2, '13:00', '23:00', '22:30'),
    (3, '13:00', '23:00', '22:30'),
    (4, '13:00', '23:00', '22:30'),
    (5, '13:00', '23:00', '22:30'),
    (6, '13:00', '23:00', '22:30'),
    (7, '13:00', '23:00', '22:30');

-- Máximo 15 personas por reserva, sin mínimo.
insert into reservation_settings (id, max_party_size, min_party_size) values (1, 15, null);
