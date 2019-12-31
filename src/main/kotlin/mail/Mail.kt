/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.flug.mail

/**
 * Datensatz für eine Email bei einem gelöschten Flug, der in einer Kafka-Nachricht gesendet wird.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @property to Emailadresse für den Empfänger.
 * @property from Emailadresse für den Absender.
 * @property subject Betreff ("Subject") der zu sendenden Email.
 * @property body Rumpf der zu sendenden Email.
 */
data class Mail(
    val to: String,
    val from: String,
    val subject: String,
    val body: String
)
