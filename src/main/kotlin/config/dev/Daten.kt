/*
 * Copyright (C) 2019 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.flug.config.dev

import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flughafen
import de.hska.flug.entity.Status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.flow.flowOf

/**
 * Demo Flug-Daten für die Entwicklung
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
@Suppress("MagicNumber", "UnderscoresInNumericLiterals")
val fluege = flowOf(
    Flug(
        id = UUID.fromString("00000000-0000-0000-0000-000000000000"),
        flugnummer = "AB0D2",
        abflugFlughafen = Flughafen(
            iata = "ATL",
            name = "HartsfieldJackson Atlanta International Airport",
            land = "United States of America"
        ),
        ankunftFlughafen = Flughafen(
            iata = "FRA",
            name = "Frankfurt Airport",
            land = "Deutschland"
        ),
        gate = 4,
        flugzeugId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        airline = "Easy Jet",
        status = Status.build("GO"),
        abflugzeit = LocalDateTime.of(LocalDate.of(2020, 2, 27), LocalTime.of(14, 48, 0)),
        ankunftzeit = LocalDateTime.of(LocalDate.of(2020, 2, 27), LocalTime.of(15, 48, 0))

    ),
    Flug(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        flugnummer = "CG201",
        abflugFlughafen = Flughafen(
            iata = "PEK",
            name = "Beijing Capital International Airport",
            land = "China"
        ),
        ankunftFlughafen = Flughafen(
            iata = "FRA",
            name = "Frankfurt Airport",
            land = "Deutschland"
        ),
        gate = 4,
        flugzeugId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        airline = "Turkish Airlines",
        status = Status.build("GO"),
        abflugzeit = LocalDateTime.of(LocalDate.of(2020, 6, 27), LocalTime.of(22, 48, 0)),
        ankunftzeit = LocalDateTime.of(LocalDate.of(2020, 7, 27), LocalTime.of(5, 20, 0))
    ),
    Flug(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        flugnummer = "NL0D2",
        abflugFlughafen = Flughafen(
            iata = "ATL",
            name = "HartsfieldJackson Atlanta International Airport",
            land = "United States of America"
        ),
        ankunftFlughafen = Flughafen(
            iata = "LAX",
            name = "Los Angeles International Airport",
            land = "United States of America"
        ),
        gate = 4,
        flugzeugId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        airline = "Lufthansa",
        status = Status.build("GO"),
        abflugzeit = LocalDateTime.of(LocalDate.of(2020, 2, 27), LocalTime.of(14, 48, 0)),
        ankunftzeit = LocalDateTime.of(LocalDate.of(2020, 2, 27), LocalTime.of(15, 48, 0))
    ),
    Flug(
        id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
        flugnummer = "NL0D2",
        abflugFlughafen = Flughafen(
            iata = "DXB",
            name = "Dubai International Airport",
            land = "United Arab Emirates"
        ),
        ankunftFlughafen = Flughafen(
            iata = "LAX",
            name = "Los Angeles International Airport",
            land = "United States of America"
        ),
        gate = 3,
        flugzeugId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        airline = "Lufthansa",
        status = Status.build("GO"),
        abflugzeit = LocalDateTime.of(LocalDate.of(2020, 5, 1), LocalTime.of(9, 48, 0)),
        ankunftzeit = LocalDateTime.of(LocalDate.of(2020, 5, 11), LocalTime.of(15, 48, 0))
    ),
    Flug(
        id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
        flugnummer = "AB0D2",
        abflugFlughafen = Flughafen(
            iata = "ATL",
            name = "HartsfieldJackson Atlanta International Airport",
            land = "United States of America"
        ),
        ankunftFlughafen = Flughafen(
            iata = "FRA",
            name = "Frankfurt Airport",
            land = "Deutschland"
        ),
        gate = 4,
        flugzeugId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        airline = "Easy Jet",
        status = Status.build("DP"),
        abflugzeit = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(14, 48, 0)),
        ankunftzeit = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(23, 15, 0))

    )
)
