-- Franjas de reserva por día de la semana (ISO-8601: 1 = lunes ... 7 = domingo).
-- Un día sin filas se considera cerrado. Puede haber varias franjas por día.
create table reservation_time_slot (
    id              bigint generated always as identity primary key,
    day_of_week     smallint not null,
    opens_at        time     not null,
    closes_at       time     not null,
    last_booking_at time     not null,
    constraint ck_reservation_time_slot_day_of_week
        check (day_of_week between 1 and 7),
    constraint ck_reservation_time_slot_times
        check (opens_at <= last_booking_at and last_booking_at <= closes_at)
);

create index ix_reservation_time_slot_day_of_week on reservation_time_slot (day_of_week);

-- Ajustes generales de reserva: tabla de fila única (id = 1).
create table reservation_settings (
    id             smallint primary key,
    max_party_size integer  not null,
    min_party_size integer  null,
    constraint ck_reservation_settings_single_row
        check (id = 1),
    constraint ck_reservation_settings_max
        check (max_party_size >= 1),
    constraint ck_reservation_settings_min
        check (min_party_size >= 1),
    constraint ck_reservation_settings_min_le_max
        check (min_party_size is null or min_party_size <= max_party_size)
);
