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
@file:Suppress("PackageDirectoryMismatch")

package de.hska.flug.service

import com.mongodb.client.result.DeleteResult
import de.hska.flug.config.security.CustomUserDetailsService
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flughafen
import de.hska.flug.entity.Status
import de.hska.flug.mail.Mailer
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import java.util.UUID.randomUUID
import javax.validation.Validation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_12
import org.junit.jupiter.api.condition.JRE.JAVA_13
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.get
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.mongodb.core.ReactiveFindOperation.ReactiveFind
import org.springframework.data.mongodb.core.ReactiveFindOperation.TerminatingFind
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations
import org.springframework.data.mongodb.core.ReactiveInsertOperation.ReactiveInsert
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.ReactiveRemoveOperation.ReactiveRemove
import org.springframework.data.mongodb.core.ReactiveRemoveOperation.TerminatingRemove
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.mongodb.core.query.where
import org.springframework.data.mongodb.core.remove
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Suppress("ReactorUnusedPublisher")
@Tag("service")
@DisplayName("Anwendungskern fuer Flug testen")
@ExtendWith(MockKExtension::class, SoftAssertionsExtension::class)
@EnabledOnJre(JAVA_12, JAVA_13)
@ExperimentalCoroutinesApi
class FlugServiceTest {
    private var mongo: ReactiveFluentMongoOperations = mockk()
    private var mongoTemplate: ReactiveMongoTemplate = mockk()
    // ggf. com.ninja-squad:springmockk
    private var userDetailsService: CustomUserDetailsService = mockk()
    private val validatorFactory = Validation.buildDefaultValidatorFactory()
    private val mailer: Mailer = mockk()
    private val service = FlugService(mongo, userDetailsService, validatorFactory, mailer)

    private var reactiveFind: ReactiveFind<Flug> = mockk()
    private var terminatingFind: TerminatingFind<Flug> = mockk()
    private var reactiveInsert: ReactiveInsert<Flug> = mockk()
    private var reactiveRemove: ReactiveRemove<Flug> = mockk()
    private var terminatingRemove: TerminatingRemove<Flug> = mockk()

    @BeforeEach
    fun beforeEach() {
        clearMocks(
            mongo,
            mongoTemplate,
            userDetailsService,
            mailer,
            reactiveFind,
            terminatingFind,
            reactiveInsert,
            reactiveRemove
        )
    }

    @Test
    @Order(100)
    fun `Immer erfolgreich`() {
        @Suppress("UsePropertyAccessSyntax")
        assertThat(true).isTrue()
    }

    @Test
    @Order(200)
    @Disabled
    fun `Noch nicht fertig`() {
        @Suppress("UsePropertyAccessSyntax")
        assertThat(false).isFalse()
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Lesen {
        @Suppress("ClassName")
        @Nested
        inner class `Suche anhand der ID` {
            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, $FLUGNUMMER")
            @Order(1000)
            fun `Suche mit vorhandener ID`(idStr: String, flugnummer: String) = runBlockingTest {
                // arrange
                every { mongo.query<Flug>() } returns reactiveFind

                val id = UUID.fromString(idStr)
                val query = Query(where(Flug::id).isEqualTo(id))
                every { reactiveFind.matching(query) } returns terminatingFind

                val flugMock = createFlugMock(id, flugnummer)
                every { terminatingFind.one() } returns flugMock.toMono()

                val flug = service.findById(id)

                // assert
                assertThat(flug?.id).isEqualTo(id)
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_NICHT_VORHANDEN])
            @Order(1100)
            fun `Suche mit nicht vorhandener ID`(idStr: String) = runBlockingTest {
                // arrange
                every { mongo.query<Flug>() } returns reactiveFind
                val id = UUID.fromString(idStr)
                val query = Query(where(Flug::id).isEqualTo(id))
                every { reactiveFind.matching(query) } returns terminatingFind
                every { terminatingFind.one() } returns Mono.empty()

                // act
                val result = service.findById(id)

                // assert
                assertThat(result).isNull()
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [FLUGNUMMER])
        @Order(2000)
        fun `Suche alle Flügen`(flugnummer: String) = runBlockingTest {
            // arrange
            every { mongo.query<Flug>() } returns reactiveFind
            // .flow()
            val flugMock = createFlugMock(flugnummer)
            every { reactiveFind.all() } returns listOf(flugMock).toFlux()
            val emptyQueryParams = LinkedMultiValueMap<String, String>()

            // act
            val fluege = service.find(emptyQueryParams)

            // assert
            val fluegeList = mutableListOf<Flug>()
            fluege.toList(fluegeList)
            assertThat(fluegeList).isNotEmpty
        }

        @ParameterizedTest
        @ValueSource(strings = [FLUGNUMMER])
        @Order(2100)
        fun `Suche mit vorhandener Flugnummer`(flugnummer: String) = runBlockingTest {
            // arrange
            every { mongo.query<Flug>() } returns reactiveFind
            val query = Query(Flug::flugnummer.regex(flugnummer, "i"))
            every { reactiveFind.matching(query) } returns terminatingFind
            // .flow()
            val flugMock = createFlugMock(flugnummer)
            every { terminatingFind.all() } returns listOf(flugMock).toFlux()
            val queryParams = LinkedMultiValueMap(mapOf("flugnummer" to listOf(flugnummer)))

            // act
            val fluege = service.find(queryParams)

            // assert
            val fluegeList = mutableListOf<Flug>()
            fluege.toList(fluegeList)
            assertThat(fluegeList)
                .isNotEmpty
                .allMatch { flug -> flug.flugnummer == flugnummer }
        }

        @ParameterizedTest
        @CsvSource("$ID_VORHANDEN, $FLUGNUMMER, $AIRLINE")
        @Order(2400)
        fun `Suche mit vorhandener Flugnummer und Airline`(idStr: String, flugnummer: String, airline: String) =
            runBlockingTest {
                // arrange
                every { mongo.query<Flug>() } returns reactiveFind
                val query = Query(Flug::flugnummer.regex(flugnummer, "i"))
                query.addCriteria(Flug::airline.regex(airline, "i"))
                every { reactiveFind.matching(query) } returns terminatingFind
                val id = UUID.fromString(idStr)
                val flugMock = createFlugMock(id, flugnummer, airline)
                every { terminatingFind.all() } returns listOf(flugMock).toFlux()
                val queryParams =
                    LinkedMultiValueMap(mapOf("flugnummer" to listOf(flugnummer), "airline" to listOf(airline)))

                // act
                val fluege = service.find(queryParams)

                // assert
                val fluegeList = mutableListOf<Flug>()
                fluege.toList(fluegeList)
                assertThat(fluegeList)
                    .isNotEmpty
                    .allMatch { flug ->
                        flug.flugnummer == flugnummer &&
                                flug.airline.toLowerCase() == airline.toLowerCase()
                    }
            }
    }

        // -------------------------------------------------------------------------
        // S C H R E I B E N
        // -------------------------------------------------------------------------
    @Nested
    inner class Schreiben {
            @Nested
            inner class Erzeugen {
                @ParameterizedTest
                @CsvSource("$FLUGNUMMER, $GATE, $FLUGZEUGTYP, $AIRLINE")
                @Order(5000)
                fun `Neuen Flug abspeichern`(args: ArgumentsAccessor, softly: SoftAssertions) = runBlockingTest {
                    // arrange
                    val flugnummer = args.get<String>(0)
                    val gate = args.get<Int>(1)
                    val flugzeugtyp = args.get<String>(2)
                    val airline = args.get<String>(3)

                    every { mongo.query<Flug>() } returns reactiveFind
                    every { terminatingFind.exists() } returns false.toMono()

                    every { mongo.insert<Flug>() } returns reactiveInsert
                    val flugMock = createFlugMock(null, flugnummer, gate, flugzeugtyp, airline)
                    val flugResultMock = flugMock.copy(id = randomUUID())
                    every { reactiveInsert.one(flugMock) } returns flugResultMock.toMono()

                    every { mailer.send(flugMock) } just Runs

                    // act
                    val flug = service.create(flugMock)

                    // assert
                    with(softly) {
                        assertThat(flug.id).isNotNull()
                        assertThat(flug.flugnummer).isEqualTo(flugnummer)
                        assertThat(flug.gate).isEqualTo(gate)
                        assertThat(flug.flugzeugId).isEqualTo(flugzeugtyp)
                        assertThat(flug.airline).isEqualTo(airline)
                    }
                }
            }

            @Nested
            inner class Aendern {
                @ParameterizedTest
                @CsvSource("$ID_UPDATE, $FLUGNUMMER, $AIRLINE")
                @Order(6000)
                @Disabled("Mocking des Cache in Spring Data MongoDB...")
                fun `Vorhandenen Flug aktualisieren`(args: ArgumentsAccessor) = runBlockingTest {
                    val idStr = args.get<String>(0)
                    val flugnummer = args.get<String>(1)
                    val airline = args.get<String>(2)
                    // arrange
                    every { mongo.query<Flug>() } returns reactiveFind
                    val id = UUID.fromString(idStr)
                    every { reactiveFind.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingFind
                    val flugMock = createFlugMock(id, flugnummer, airline)
                    every { terminatingFind.one() } returns flugMock.toMono()
                    every { mongoTemplate.save(flugMock) } returns flugMock.toMono()

                    // act
                    val flug = service.update(flugMock, id, flugMock.version.toString())
                    // assert
                    assertThat(flug?.id).isEqualTo(id)
                }

                @ParameterizedTest
                @CsvSource("$ID_NICHT_VORHANDEN, $FLUGNUMMER, $AIRLINE, $VERSION")
                @Order(6100)
                fun `Nicht-existierenden Flug aktualisieren`(args: ArgumentsAccessor) = runBlockingTest {
                    // arrange
                    val idStr = args.get<String>(0)
                    val id = UUID.fromString(idStr)
                    val flugnummer = args.get<String>(1)
                    val airline = args.get<String>(2)
                    val version = args.get<String>(3)

                    every { mongo.query<Flug>() } returns reactiveFind
                    every { reactiveFind.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingFind
                    every { terminatingFind.one() } returns Mono.empty()

                    val flugMock = createFlugMock(id, flugnummer, airline)

                    // act
                    val flug = service.update(flugMock, id, version)

                    // assert
                    assertThat(flug).isNull()
                }

                @ParameterizedTest
                @CsvSource("$ID_UPDATE, $FLUGNUMMER, $AIRLINE, $VERSION_INVALID")
                @Order(6200)
                fun `Flug aktualisieren mit falscher Versionsnummer`(args: ArgumentsAccessor) = runBlockingTest {
                    // arrange
                    val idStr = args.get<String>(0)
                    val id = UUID.fromString(idStr)
                    val flugnummer = args.get<String>(1)
                    val airline = args.get<String>(2)
                    val version = args.get<String>(3)

                    every { mongo.query<Flug>() } returns reactiveFind
                    every { reactiveFind.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingFind
                    val flugMock = createFlugMock(id, flugnummer, airline)
                    every { terminatingFind.one() } returns flugMock.toMono()

                    // act
                    val thrown = catchThrowableOfType(
                        { runBlockingTest { service.update(flugMock, id, version) } },
                        InvalidVersionException::class.java
                    )

                    // assert
                    assertThat(thrown.message).isEqualTo("Falsche Versionsnummer: $version")
                }

                @ParameterizedTest
                @CsvSource("$ID_UPDATE, $FLUGNUMMER, $AIRLINE, $VERSION_ALT")
                @Order(6300)
                @Disabled("Mocking des Cache in Spring Data MongoDB...")
                fun `Flug aktualisieren mit alter Versionsnummer`(args: ArgumentsAccessor) = runBlockingTest {
                    // arrange
                    val idStr = args.get<String>(0)
                    val id = UUID.fromString(idStr)
                    val flugnummer = args.get<String>(1)
                    val airline = args.get<String>(2)
                    val version = args.get<String>(4)

                    every { mongo.query<Flug>() } returns reactiveFind
                    every { reactiveFind.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingFind
                    val flugMock = createFlugMock(id, flugnummer, airline)

                    // act
                    val thrown = catchThrowableOfType(
                        { runBlockingTest { service.update(flugMock, id, version) } },
                        InvalidVersionException::class.java
                    )

                    // assert
                    assertThat(thrown.cause).isNull()
                }
            }

            @Nested
            inner class Loeschen {
                @ParameterizedTest
                @CsvSource("$ID_LOESCHEN_NICHT_VORHANDEN, $FLUGNUMMER")
                @Order(7000)
                fun `Vorhandenen Flug loeschen`(idStr: String, flugnummer: String) = runBlockingTest {
                    // arrange
                    every { mongo.query<Flug>() } returns reactiveFind

                    val id = UUID.fromString(idStr)
                    val query = Query(where(Flug::id).isEqualTo(id))
                    every { reactiveFind.matching(query) } returns terminatingFind
                    val flugMock = createFlugMock(id, flugnummer)
                    every { terminatingFind.one() } returns flugMock.toMono()

                    every { mongo.remove<Flug>() } returns reactiveRemove
                    every { reactiveRemove.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingRemove
                    // DeleteResult ist eine abstrakte Klasse
                    val deleteResultMock = object : DeleteResult() {
                        override fun wasAcknowledged() = true
                        override fun getDeletedCount() = 1L
                    }
                    every { terminatingRemove.all() } returns deleteResultMock.toMono()

                    every { mailer.send(flugMock) } just Runs

                    // act
                    val deleteResult = service.deleteById(id)

                    // assert
                    assertThat(deleteResult.deletedCount).isOne()
                }

                @ParameterizedTest
                @CsvSource("$ID_LOESCHEN_NICHT_VORHANDEN, $FLUGNUMMER")
                @Order(7100)
                fun `Nicht-vorhandenen Flug loeschen`(idStr: String, flugnummer: String) = runBlockingTest {
                    // arrange
                    every { mongo.query<Flug>() } returns reactiveFind

                    val id = UUID.fromString(idStr)
                    val query = Query(where(Flug::id).isEqualTo(id))
                    every { reactiveFind.matching(query) } returns terminatingFind
                    val flugMock = createFlugMock(id, flugnummer)
                    every { terminatingFind.one() } returns flugMock.toMono()

                    every { mongo.remove<Flug>() } returns reactiveRemove
                    every { reactiveRemove.matching(Query(where(Flug::id).isEqualTo(id))) } returns terminatingRemove
                    // DeleteResult ist eine abstrakte Klasse
                    val deleteResultMock = object : DeleteResult() {
                        override fun wasAcknowledged() = true
                        override fun getDeletedCount() = 0L
                    }
                    every { terminatingRemove.all() } returns deleteResultMock.toMono()

                    every { mailer.send(flugMock) } just Runs

                    // act
                    val deleteResult = service.deleteById(id)

                    // assert
                    assertThat(deleteResult.deletedCount).isZero()
                }
            }
        }

        // -------------------------------------------------------------------------
        // Hilfsmethoden fuer Mocking
        // -------------------------------------------------------------------------
        private fun createFlugMock(flugnummer: String): Flug = createFlugMock(randomUUID(), flugnummer)

        private fun createFlugMock(id: UUID, flugnummer: String): Flug = createFlugMock(id, flugnummer, AIRLINE)

        private fun createFlugMock(id: UUID, flugnummer: String, airline: String) =
            createFlugMock(id, flugnummer, GATE, null, airline)

        @Suppress("LongParameterList", "SameParameterValue")
        private fun createFlugMock(
            id: UUID?,
            flugnummer: String,
            gate: Int,
            flugzeugtyp: String?,
            airline: String
        ): Flug {
            val abflugFlughafen =
                Flughafen("ATL", "HartsfieldJackson Atlanta International Airport", "United States of America")
            val ankunftFlughafen = Flughafen("FRA", "Frankfurt Airport", "Deutschland")
            val flug = Flug(
                id = id,
                version = 0,
                flugnummer = flugnummer,
                abflugFlughafen = abflugFlughafen,
                ankunftFlughafen = ankunftFlughafen,
                abflugzeit = ABFLUGZEIT,
                ankunftzeit = ANKUNFTZEIT,
                gate = gate,
                flugzeugId = flugzeugtyp ?: "",
                airline = airline,
                status = Status.CANCELED
            )

            return flug
        }
        private companion object {
            const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
            const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
            const val ID_UPDATE = "00000000-0000-0000-0000-000000000002"
            const val ID_LOESCHEN = "00000000-0000-0000-0000-000000000003"
            const val ID_LOESCHEN_NICHT_VORHANDEN = "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"
            const val FLUGNUMMER = "AB327"
            const val AIRLINE = "Lufthansa"
            val ABFLUGZEIT = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(14, 48, 0))
            val ANKUNFTZEIT = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(23, 15, 0))
            const val GATE = 4
            const val FLUGZEUGTYP = "A380"
//            const val HOMEPAGE = "https://test.de"
//            const val USERNAME = "test"
//            const val PASSWORD = "p"
            const val VERSION = "0"
            const val VERSION_INVALID = "!?"
            const val VERSION_ALT = "-1"
        }
    }
