/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.flug.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Spring-Konfiguration für Properties zu _Spring Boot_ `spring.mail.*`.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @constructor Ein Objekt zu den Properties `mail.*` für den Präfix `spring`.
 * @property mail Properties `spring.mail.*`.
 */
@ConfigurationProperties(prefix = "spring")
@Suppress("unused")
class MailProps(var mail: Mail = Mail()) {
    /**
     * Properties zu _Spring Boot_ `spring.mail.*`.
     *
     * @author Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
     *
     * @constructor Ein Objekt zu den Properties `host` und `port` für den Präfix `spring.mail`.
     * @property host Property `spring.mail.host`.
     * @property port Property `spring.mail.port`.
     */
    class Mail(
        val host: String = "localhost",
        private val port: Int = PORT
    ) {
        private companion object {
            @Suppress("UnderscoresInNumericLiterals")
            const val PORT = 25000
        }
    }
}

/**
 * Spring-Konfiguration für Properties `mail.*`.
 *
 * @author Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de) *
 * @constructor Ein Objekt zu den Properties `from`, `sales` und `topic` für den Präfix `mail`.
 * @property from Emailadresse für _from_, z.B. `Vorname Nachname <foo@test.de>`.
 * @property supervisor Emailadresse für den Vertrieb.
 * @property topic Name der Topic für _Apache Kafka_.
 */
@ConfigurationProperties(prefix = "mail")
@ConstructorBinding
class MailAddressProps(
    @param:DefaultValue("Theo Test <theo@test.de>")
    val from: String,

    @param:DefaultValue("Maxi Musterfrau <maxi.musterfrau@test.de>")
    val supervisor: String,

    @param:DefaultValue("mail")
    val topic: String
)
