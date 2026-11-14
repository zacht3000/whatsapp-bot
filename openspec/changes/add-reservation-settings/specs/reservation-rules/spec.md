# Spec Delta

## Purpose

Determina si una reserva solicitada es admisible según el horario de reservas y los límites de personas del restaurante, configurados en base de datos para poder cambiarlos sin desplegar.

## ADDED Requirements

### Requirement: Horario de reservas configurable en base de datos
El sistema SHALL obtener el horario de reservas de la base de datos en cada consulta, sin valores fijos en código ni en la configuración de la aplicación. El horario se define por día de la semana como cero o más franjas, cada una con hora de apertura, hora de cierre y última hora de reserva. Un día sin franjas se considera cerrado.

#### Scenario: Cambio de horario sin desplegar
- **GIVEN** el martes tiene una franja con última hora de reserva 22:30
- **WHEN** la última hora de reserva del martes se cambia a 22:00 directamente en base de datos
- **THEN** una reserva para un martes a las 22:15 deja de admitirse sin reiniciar ni desplegar la aplicación

#### Scenario: Día sin franjas
- **GIVEN** un día de la semana que no tiene ninguna franja configurada
- **WHEN** se consulta si se puede reservar en ese día a cualquier hora
- **THEN** el sistema responde que el restaurante está cerrado ese día

### Requirement: Horario inicial cargado por migración
El sistema SHALL cargar mediante migración de base de datos un horario inicial de martes a domingo con apertura a las 13:00, cierre a las 23:00 y última hora de reserva a las 22:30, y sin franjas los lunes.

#### Scenario: Base de datos recién creada
- **WHEN** se aplican las migraciones sobre una base de datos vacía
- **THEN** existen franjas de martes a domingo con apertura 13:00, cierre 23:00 y última reserva 22:30
- **AND** no existe ninguna franja para el lunes

### Requirement: Rechazo de fechas pasadas
El sistema SHALL rechazar como pasada cualquier fecha y hora de reserva que no sea posterior al momento actual en hora local de España (Europe/Madrid). Esta comprobación se aplica antes que las del horario.

#### Scenario: Fecha de un día anterior
- **GIVEN** el momento actual es el miércoles 18 de noviembre de 2026 a las 10:00 en Madrid
- **WHEN** se consulta una reserva para el martes 17 de noviembre de 2026 a las 14:00
- **THEN** el sistema responde que la fecha y hora ya ha pasado

#### Scenario: Hora ya pasada del mismo día
- **GIVEN** el momento actual es el miércoles 18 de noviembre de 2026 a las 15:00 en Madrid
- **WHEN** se consulta una reserva para ese mismo día a las 14:00
- **THEN** el sistema responde que la fecha y hora ya ha pasado

#### Scenario: Hora futura del mismo día
- **GIVEN** el momento actual es el miércoles 18 de noviembre de 2026 a las 15:00 en Madrid
- **WHEN** se consulta una reserva para ese mismo día a las 21:00
- **THEN** el sistema responde que la fecha y hora es admisible

#### Scenario: Fecha pasada en día cerrado
- **GIVEN** el momento actual es el miércoles 18 de noviembre de 2026 a las 10:00 en Madrid
- **WHEN** se consulta una reserva para el lunes 16 de noviembre de 2026 a las 14:00
- **THEN** el sistema responde que la fecha y hora ya ha pasado, no que el restaurante está cerrado

### Requirement: Admisión de fecha y hora de reserva
El sistema SHALL admitir una fecha y hora de reserva futura, expresada en hora local de España (Europe/Madrid), solo si ese día de la semana tiene alguna franja cuya hora de apertura sea anterior o igual a la hora pedida y cuya última hora de reserva sea posterior o igual a ella.

#### Scenario: Hora dentro de la franja
- **WHEN** se consulta una reserva para un sábado a las 21:00
- **THEN** el sistema responde que la fecha y hora es admisible

#### Scenario: Límites de la franja incluidos
- **WHEN** se consulta una reserva para un miércoles a las 13:00 y otra para un miércoles a las 22:30
- **THEN** el sistema responde que ambas son admisibles

#### Scenario: Hora antes de la apertura
- **WHEN** se consulta una reserva para un jueves a las 12:59
- **THEN** el sistema responde que la hora está fuera del horario de reservas

#### Scenario: Hora posterior a la última reserva
- **WHEN** se consulta una reserva para un viernes a las 22:31
- **THEN** el sistema responde que la hora está fuera del horario de reservas

#### Scenario: Lunes cerrado
- **WHEN** se consulta una reserva para un lunes a las 14:00
- **THEN** el sistema responde que el restaurante está cerrado ese día

### Requirement: Límites de personas configurables en base de datos
El sistema SHALL obtener de la base de datos, en cada consulta, el máximo de personas por reserva y un mínimo opcional. Si no hay mínimo configurado, el mínimo efectivo es 1. La migración inicial SHALL fijar el máximo en 15 personas y no fijar mínimo.

#### Scenario: Valores iniciales
- **WHEN** se aplican las migraciones sobre una base de datos vacía
- **THEN** el máximo de personas por reserva es 15 y no hay mínimo configurado

#### Scenario: Cambio de máximo sin desplegar
- **GIVEN** el máximo de personas es 15
- **WHEN** el máximo se cambia a 12 directamente en base de datos
- **THEN** una reserva para 13 personas pasa a derivarse a un humano sin reiniciar ni desplegar la aplicación

### Requirement: Admisión del número de personas
El sistema SHALL clasificar el número de personas de una reserva como admisible si está entre el mínimo efectivo y el máximo, ambos incluidos; como derivable a un humano si supera el máximo; y como inválido si es menor que el mínimo efectivo.

#### Scenario: Número dentro de los límites
- **WHEN** se consulta una reserva para 1 persona y otra para 15 personas con la configuración inicial
- **THEN** el sistema responde que ambas son admisibles

#### Scenario: Grupo por encima del máximo
- **WHEN** se consulta una reserva para 16 personas con la configuración inicial
- **THEN** el sistema responde que la reserva debe derivarse a un humano

#### Scenario: Número no válido
- **WHEN** se consulta una reserva para 0 personas o un número negativo
- **THEN** el sistema responde que el número de personas no es válido
