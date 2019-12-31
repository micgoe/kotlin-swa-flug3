@file:Suppress("StringLiteralDuplication")

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
package de.hska.flug.config.dev
import com.mongodb.reactivestreams.client.MongoCollection
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flughafen.Companion.IATA_PATTERN
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.oneAndAwait
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.`object`
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.date
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.int32
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string
import org.springframework.data.mongodb.core.schema.MongoJsonSchema

/**
 * Interface, um im Profil _dev_ die (Test-) DB neu zu laden.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
interface DbPopulate {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen,
     * damit die (Test-) DB neu geladen wird.
     * @param mongo Template für MongoDB
     * @return CommandLineRunner
     */
    @Bean
    @Description("Test-DB neu laden")
    fun dbPopulate(mongo: ReactiveMongoOperations) = CommandLineRunner {
        val logger = getLogger(DbPopulate::class.java)
        logger.warn("Neuladen der Collection 'Flug'")

        runBlocking {
            mongo.dropCollection<Flug>().awaitFirstOrNull()
            createSchema(mongo, logger)
//            createIndexNachname(mongo, logger)
//            createIndexEmail(mongo, logger)
//            createIndexUmsatz(mongo, logger)

            fluege.collect { flug ->
                val flugDb = mongo.insert<Flug>().oneAndAwait(flug)
                logger.debug("{}", flugDb)
            }
        }
    }

    @Suppress("MagicNumber", "LongMethod")
    private suspend fun createSchema(mongo: ReactiveMongoOperations, logger: Logger): MongoCollection<Document> {
        val schema = MongoJsonSchema.builder()
            .required("flugnummer", "airline")
            .properties(
                // int32("version"),
                string("flugnummer"),
                `object`("abflugFlughafen")
                    .properties(
                        string("iata").matching(IATA_PATTERN),
                        string("name"),
                        string("land")
                    ),
                `object`("ankunftFlughafen")
                    .properties(
                        string("iata").matching(IATA_PATTERN),
                        string("name"),
                        string("land")
                    ),
                date("abflugzeit"),
                date("ankunftzeit"),
                int32("gate"),
                string("flugzeugtyp"),
                string("airline"),
                string("Status"),
                // string("username"),
                date("erzeugt"),
                date("aktualisiert")
            )
            .build()
        logger.info("JSON Schema fuer Flug: {}", schema.toDocument().toJson())
        return mongo.createCollection<Flug>(CollectionOptions.empty().schema(schema)).awaitFirst()
    }

//    private suspend fun createIndexNachname(mongo: ReactiveMongoOperations, logger: Logger): String {
//        logger.warn("Index fuer 'nachname'")
//        val idx = Index("nachname", ASC).named("nachname")
//        return mongo.indexOps<Kunde>().ensureIndex(idx).awaitFirst()
//    }
//
//    private suspend fun createIndexEmail(mongo: ReactiveMongoOperations, logger: Logger): String {
//        logger.warn("Index fuer 'email'")
//        // Emailadressen sollen paarweise verschieden sein
//        val idx = Index("email", ASC).unique().named("email")
//        return mongo.indexOps<Kunde>().ensureIndex(idx).awaitFirst()
//    }
//
//    private suspend fun createIndexUmsatz(mongo: ReactiveMongoOperations, logger: Logger): String {
//        logger.warn("Index fuer 'umsatz'")
//        // "sparse" statt NULL bei relationalen DBen
//        // Keine Indizierung der Kunden, bei denen es kein solches Feld gibt
//        val umsatzIdx = Index("umsatz", ASC).sparse().named("umsatz")
//        return mongo.indexOps<Kunde>().ensureIndex(umsatzIdx).awaitFirst()
//    }
}
