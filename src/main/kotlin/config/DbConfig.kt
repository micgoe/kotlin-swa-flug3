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

import com.mongodb.WriteConcern.ACKNOWLEDGED
import de.hska.flug.config.security.CustomUser
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Status
import java.util.UUID
import java.util.UUID.randomUUID
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.WriteConcernResolver
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback
import reactor.kotlin.core.publisher.toMono

/**
 * Spring-Konfiguration für den Zugriff auf _MongoDB_.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
interface DbConfig {
    /**
     * Liste mit Konvertern für Lesen und Schreiben in _MongoDB_ ermitteln.
     * @return Liste mit Konvertern für Lesen und Schreiben in _MongoDB_.
     */
    @Bean
    fun customConversions() = MongoCustomConversions(
        listOf(
            // Enums
            UUIDReadConverter(),
            UUIDWriteConverter(),
            Status.ReadConverter(),
            Status.WriteConverter(),
            // Rollen fuer Security
            CustomUser.RoleReadConverter(),
            CustomUser.RoleWriteConverter()
        )
    )

    /**
     * Konvertierungsklasse für MongoDB, um einen String einzulesen und eine UUID zu erzeugen.
     * Wegen @ReadingConverter ist kein Lambda-Ausdruck möglich.
     */
    @ReadingConverter
    class UUIDReadConverter : Converter<String, UUID> {
        /**
         * Konvertierung eines Strings in eine UUID.
         * @param uuid String mit einer UUID.
         * @return Zugehörige UUID
         */
        override fun convert(uuid: String): UUID = UUID.fromString(uuid)
    }

    /**
     * Konvertierungsklasse für MongoDB, um eine UUID in einen String zu konvertieren.
     * Wegen @WritingConverter ist kein Lambda-Ausdruck möglich.
     */
    @WritingConverter
    class UUIDWriteConverter : Converter<UUID, String> {
        /**
         * Konvertierung einer UUID in einen String, z.B. beim Abspeichern.
         * @param uuid Objekt von UUID
         * @return String z.B. zum Abspeichern.
         */
        override fun convert(uuid: UUID): String? = uuid.toString()
    }

    /**
     * Bean zur Generierung der Kunde-ID beim Anlegen eines neuen Kunden
     * @return Kunde-Objekt mit einer Kunde-ID
     */
    @Bean
    fun generateKundeId() = ReactiveBeforeConvertCallback<Flug> { flug, _ ->
        if (flug.id == null) {
            val flugMitId = flug.copy(id = randomUUID()) // Any other Change Operations to keep the DB well structured
            LoggerFactory.getLogger(DbConfig::class.java).debug("generateKundeId: {}", flugMitId)
            flugMitId
        } else {
            flug
        }.toMono()
    }

    /**
     * Bean für Optimistische Synchronisation
     * @return ACKNOWLEDGED als "WriteConcern" für MongoDB
     */
    @Bean
    fun writeConcernResolver() = WriteConcernResolver { ACKNOWLEDGED }

    /**
     * Autokonfiguration für MongoDB-Transaktionen bei "Reactive Programming".
     * @return Instanziiertes Objekt der Klasse ReactiveMongoTransactionManager.
     */
    @Bean
    fun reactiveTransactionManager(factory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(factory)
}
