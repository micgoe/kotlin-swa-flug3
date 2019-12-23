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

package de.hska.flug.rest

import com.jayway.jsonpath.JsonPath
import de.hska.flug.config.Settings.DEV
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flug.Companion.ID_PATTERN
import de.hska.flug.entity.Flughafen
import de.hska.flug.entity.Status
import de.hska.flug.rest.constraints.FlugConstraintViolation
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension
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
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer
import org.springframework.http.HttpHeaders.IF_MATCH
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.PRECONDITION_FAILED
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlow
import reactor.kotlin.core.publisher.toMono

@Tag("rest")
@DisplayName("REST-Schnittstelle fuer Fluege testen")
@ExtendWith(SpringExtension::class, SoftAssertionsExtension::class)
// Alternative zu @ContextConfiguration von Spring
// Default: webEnvironment = MOCK, d.h.
//          Mock Servlet Umgebung anstatt eines Embedded Servlet Containers
@SpringBootTest(webEnvironment = RANDOM_PORT)
// @SpringBootTest(webEnvironment = DEFINED_PORT, ...)
// ggf.: @DirtiesContext, falls z.B. ein Spring Bean modifiziert wurde
@ActiveProfiles(DEV)
@TestPropertySource(locations = ["/rest-test.properties"])
@EnabledOnJre(JAVA_12, JAVA_13)
class FlugRestTest(@LocalServerPort private val port: Int, ctx: ReactiveWebApplicationContext) {
    private var baseUrl = "$SCHEMA://$HOST:$port"

    // WebClient auf der Basis von "Reactor Netty"
    // Alternative: Http Client von Java http://openjdk.java.net/groups/net/httpclient/intro.html
    private var client = WebClient.builder()
        .filter(basicAuthentication(USERNAME, PASSWORD))
        .baseUrl(baseUrl)
        .build()

    init {
        assertThat(ctx.getBean<FlugHandler>()).isNotNull
    }

    @Test
    @Order(100)
    fun `Immer erfolgreich`() {
        @Suppress("UsePropertyAccessSyntax")
        assertThat(true).isTrue()
    }

    @Test
    @Disabled("Noch nicht fertig")
    @Order(200)
    fun `Noch nicht fertig`() {
        @Suppress("UsePropertyAccessSyntax")
        assertThat(true).isFalse()
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Lesen {
        @Nested
        inner class `Suche anhand der ID` {
            @ParameterizedTest
            @ValueSource(strings = [ID_VORHANDEN, ID_UPDATE_PUT, ID_UPDATE_PATCH])
            @Order(1000)
            fun `Suche mit vorhandener ID`(id: String, softly: SoftAssertions) = runBlocking<Unit> {
                // act
                val response = client.get()
                    .uri(ID_PATH, id)
                    .accept(HAL_JSON)
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(OK)
                val content = response.awaitBody<String>()

                with(softly) {
                    val flugnummer: String = JsonPath.read(content, "$.flugnummer")
                    assertThat(flugnummer).isNotBlank
//                    val abflugzeit: LocalDateTime = JsonPath.read(content, "$.abflugzeit")
//                    assertThat(eabflugzeitmail).isNullOrEmpty // Aber negiert
                    val linkDiscoverer = HalLinkDiscoverer()
                    val selfLink = linkDiscoverer.findLinkWithRel("self", content).get().href
                    assertThat(selfLink).endsWith("/$id")
                }
            }

            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, 0")
            @Order(1100)
            fun `Suche mit vorhandener ID und vorhandener Version`(id: String, version: String) = runBlocking<Unit> {
                // act
                val response = client.get()
                    .uri(ID_PATH, id)
                    .accept(HAL_JSON)
                    .ifNoneMatch("\"$version\"")
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(NOT_MODIFIED)
            }

            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, xxx")
            @Order(1200)
            fun `Suche mit vorhandener ID und falscher Version`(
                id: String,
                version: String,
                softly: SoftAssertions
            ) = runBlocking<Unit> {
                // act
                val response = client.get()
                    .uri(ID_PATH, id)
                    .accept(HAL_JSON)
                    .ifNoneMatch("\"$version\"")
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(OK)
                val content = response.awaitBody<String>()

                with(softly) {
                    val flugnummer: String = JsonPath.read(content, "$.flugnummer")
                    assertThat(flugnummer).isNotBlank
                    val linkDiscoverer = HalLinkDiscoverer()
                    val selfLink = linkDiscoverer.findLinkWithRel("self", content).get().href
                    assertThat(selfLink).endsWith("/$id")
                }
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_INVALID, ID_NICHT_VORHANDEN])
            @Order(1300)
            fun `Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID`(id: String) = runBlocking<Unit> {
                // act
                val response = client.get()
                    .uri(ID_PATH, id)
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(NOT_FOUND)
            }

            @ParameterizedTest
            @CsvSource("$USERNAME, $PASSWORD_FALSCH, $ID_VORHANDEN")
            @Order(1400)
            fun `Suche mit ID, aber falschem Passwort`(
                username: String,
                password: String,
                id: String
            ) = runBlocking<Unit> {
                // arrange
                val clientFalsch = WebClient.builder()
                    .filter(basicAuthentication(username, password))
                    .baseUrl(baseUrl)
                    .build()

                // act
                val response = clientFalsch.get()
                    .uri(ID_PATH, id)
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED)
            }
        }

        @Test
        @Order(2000)
        fun `Suche nach allen Flügen`() = runBlocking<Unit> {
            // act
            val fluege = client.get()
                .retrieve()
                .bodyToFlow<EntityModel<Flug>>()

            // assert
            val fluegeList = mutableListOf<EntityModel<Flug>>()
            fluege.toList(fluegeList)
            assertThat(fluegeList).isNotEmpty
        }

        @ParameterizedTest
        @ValueSource(strings = [FLUGNUMMER])
        @Order(2100)
        fun `Suche mit vorhandener Flugnummer`(flugnummer: String, softly: SoftAssertions) = runBlocking<Unit> {
            // arrange
            val flugnummerLower = flugnummer.toLowerCase()

            // act
            val fluege = client.get()
                .uri {
                    it.path(FLUG_PATH)
                        .queryParam(FLUGNUMMER_PARAM, flugnummerLower)
                        .build()
                }
                .retrieve()
                .bodyToFlow<EntityModel<Flug>>()

            // assert
            val fluegeList = mutableListOf<EntityModel<Flug>>()
            fluege.toList(fluegeList)

            // assert
            with(softly) {
                assertThat(fluegeList).isNotEmpty
                assertThat(fluegeList).allMatch { model -> model.content?.flugnummer?.toLowerCase() == flugnummerLower }
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
            @CsvSource(
                "$NEUE_FLUGNUMMER, $NEUES_GATE, $NEUER_FLUGZEUGTYP, $NEUE_AIRLINE"
            )
            @Order(5000)
            fun `Abspeichern eines neuen Fluges`(args: ArgumentsAccessor, softly: SoftAssertions) = runBlocking<Unit> {
                // arrange

                val abflugFlughafen =
                    Flughafen("ATL", "HartsfieldJackson Atlanta International Airport", "United States of America")
                val ankunftFlughafen = Flughafen("FRA", "Frankfurt Airport", "Deutschland")
                val neuerFlug = Flug(
                    id = null,
                    version = 0,
                    flugnummer = args.get<String>(0),
                    abflugFlughafen = abflugFlughafen,
                    ankunftFlughafen = ankunftFlughafen,
                    abflugzeit = NEUE_ABFLUGZEIT,
                    ankunftzeit = NEUE_ANKUNFTZEIT,
                    gate = args.get<Int>(1),
                    flugzeugtyp = args.get<String>(2),
                    airline = args.get<String>(3),
                    status = Status.CANCELED
                )

                // act
                val response = client.post()
                    .singleBody(neuerFlug)
                    .awaitExchange()

                // assert
                val id: String
                with(response) {
                    with(softly) {
                        assertThat(statusCode()).isEqualTo(CREATED)

                        assertThat(headers()).isNotNull
                        val location = headers().asHttpHeaders().location
                        assertThat(location).isNotNull
                        val locationStr = location.toString()
                        assertThat(locationStr).isNotBlank
                        val indexLastSlash = locationStr.lastIndexOf('/')
                        assertThat(indexLastSlash).isPositive
                        id = locationStr.substring(indexLastSlash + 1)
                        assertThat(id).matches((ID_PATTERN))
                    }
                }

                val flugModel = client.get()
                    .uri(ID_PATH, id)
                    .retrieve()
                    .awaitBody<EntityModel<Flug>>()
                assertThat(flugModel.content?.flugnummer).isEqualTo(neuerFlug.flugnummer)
            }

            @ParameterizedTest
            @CsvSource(
                "$NEUE_FLUGNUMMER_INVALID, $NEUES_GATE, $NEUER_FLUGZEUGTYP, $NEUE_AIRLINE_INVALID"
            )
            @Order(5100)
            @Disabled
            fun `Abspeichern eines neuen Fluges mit ungueltigen Werten`(
                args: ArgumentsAccessor,
                softly: SoftAssertions
            ) = runBlocking {
                // arrange
                val abflugFlughafen =
                    Flughafen("ATL", "HartsfieldJackson Atlanta International Airport", "United States of America")
                val ankunftFlughafen = Flughafen("FRA", "Frankfurt Airport", "Deutschland")
                val neuerFlug = Flug(
                    id = null,
                    version = 0,
                    flugnummer = args.get<String>(0),
                    abflugFlughafen = abflugFlughafen,
                    ankunftFlughafen = ankunftFlughafen,
                    abflugzeit = NEUE_ABFLUGZEIT,
                    ankunftzeit = NEUE_ANKUNFTZEIT,
                    gate = args.get<Int>(1),
                    flugzeugtyp = args.get<String>(2),
                    airline = args.get<String>(3),
                    status = Status.CANCELED
                )

                // act
                val response = client.post()
                    .singleBody(neuerFlug)
                    .awaitExchange()

                // assert
                with(response) {
                    with(softly) {
                        assertThat(statusCode()).isEqualTo(BAD_REQUEST)
                        val violations = bodyToFlow<FlugConstraintViolation>()
                        val violationsList = mutableListOf<FlugConstraintViolation>()
                        violations.toList(violationsList)
                        assertThat(violationsList)
                            .hasSize(2)
                            .doesNotHaveDuplicates()
                        val violationMsgPredicate = { msg: String ->
                            msg.contains("Die Airline muss angeben werden") ||
                            msg.contains("Die Flugnummer muss angegeben werden")
                        }
                        violationsList
                            .map { it.message!! }
                            .forEach { msg ->
                                assertThat(msg).matches(violationMsgPredicate)
                            }
                    }
                }
            }
        }

        @Nested
        inner class Aendern {
            @ParameterizedTest
            @ValueSource(strings = [ID_UPDATE_PUT])
            @Order(6000)
            @Disabled
            fun `Aendern eines vorhandenen Fluges durch Put`(id: String) = runBlocking<Unit> {
                // arrange
                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .awaitExchange()
                val model = responseOrig.awaitBody<EntityModel<Flug>>()
                val flugOrig = model.content
                assertThat(flugOrig).isNotNull
                flugOrig as Flug
                val flug = flugOrig.copy(id = UUID.fromString(id), airline = "${flugOrig.airline}put")

                val etag = responseOrig.headers().asHttpHeaders().eTag
                @Suppress("UsePropertyAccessSyntax")
                assertThat(etag).isNotNull()

                // act
                val response = client.put()
                    .uri(ID_PATH, id)
                    .header(IF_MATCH, etag)
                    .singleBody(flug)
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(NO_CONTENT)
                // ggf. noch GET-Request, um die Aenderung zu pruefen
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_VORHANDEN, ID_UPDATE_PUT, ID_UPDATE_PATCH])
            @Order(6200)
            @Disabled
            fun `Aendern eines Fluges durch Put ohne Version`(id: String, softly: SoftAssertions) = runBlocking<Unit> {
                val responseOrig = client.get()
                    .uri(ID_PATH, id)
                    .awaitExchange()
                val flug = responseOrig.awaitBody<Flug>()

                // act
                val response = client.put()
                    .uri(ID_PATH, id)
                    .singleBody(flug)
                    .awaitExchange()

                // assert
                with(response) {
                    with(softly) {
                        assertThat(statusCode()).isEqualTo(PRECONDITION_FAILED)
                        val body = awaitBody<String>()
                        assertThat(body).contains("Versionsnummer")
                    }
                }
            }

            @ParameterizedTest
            @CsvSource("$ID_UPDATE_PUT, $NEUE_FLUGNUMMER_INVALID, $NEUE_AIRLINE_INVALID")
            @Order(6300)
            @Disabled
            fun `Aendern eines Fluges durch Put mit ungueltigen Daten`(
                id: String,
                flugnummer: String?,
                airline: String?,
                softly: SoftAssertions
            ) = runBlocking {
                // arrange
                val responseOrig = client.get()
                    .uri(ID_PATH, id)
                    .awaitExchange()
                val flugOrig = responseOrig.awaitBody<Flug>()
                val flug = flugOrig.copy(id = UUID.fromString(id),
                    flugnummer = flugnummer ?: "", airline = airline ?: "")

                val etag = responseOrig.headers().asHttpHeaders().eTag
                assertThat(etag).isNotNull()
                etag as String
                val version = etag.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.put()
                    .uri(ID_PATH, id)
                    .header(IF_MATCH, "\"$versionInt\"")
                    .singleBody(flug)
                    .awaitExchange()

                // assert
                with(response) {
                    with(softly) {
                        assertThat(statusCode()).isEqualTo(BAD_REQUEST)
                        val violations = bodyToFlow<FlugConstraintViolation>()
                        val violationsList = mutableListOf<FlugConstraintViolation>()
                        violations.toList(violationsList)
                        assertThat(violationsList).hasSize(2)
                        val violationMsgPredicate = { msg: String ->
                            msg.contains("Flugnummer") || msg.contains("Airline")
                        }
                        violationsList
                            .map { it.message!! }
                            .forEach { msg -> assertThat(msg).matches(violationMsgPredicate) }
                    }
                }
            }
        }

        @Nested
        inner class Loeschen {
            @ParameterizedTest
            @ValueSource(strings = [ID_DELETE])
            @Order(8000)
            fun `Loeschen eines vorhandenen Flug mit der ID`(id: String) = runBlocking<Unit> {
                // act
                val response = client.delete()
                    .uri(ID_PATH, id)
                    .awaitExchange()

                // assert
                assertThat(response.statusCode()).isEqualTo(NO_CONTENT)
            }
        }
    }

    private companion object {
        const val SCHEMA = "http"
        const val HOST = "localhost"
        const val FLUG_PATH = "/"
        const val ID_PATH = "/{id}"
        const val FLUGNUMMER_PARAM = "flugnummer"

        const val USERNAME = "admin"
        const val PASSWORD = "p"
        const val PASSWORD_FALSCH = "Falsches Passwort!"

        const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
        const val ID_INVALID = "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val ID_UPDATE_PUT = "00000000-0000-0000-0000-000000000002"
        const val ID_UPDATE_PATCH = "00000000-0000-0000-0000-000000000003"
        const val ID_DELETE = "00000000-0000-0000-0000-000000000004"

        const val FLUGNUMMER = "AB0D2"

        const val NEUE_FLUGNUMMER = "AB327"
        const val NEUE_FLUGNUMMER_INVALID = ""
        const val NEUE_AIRLINE = "Lufthansa"
        const val NEUE_AIRLINE_INVALID = "Lufthansa"
        val NEUE_ABFLUGZEIT = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(14, 48, 0))
        val NEUE_ANKUNFTZEIT = LocalDateTime.of(LocalDate.of(2020, 3, 3), LocalTime.of(23, 15, 0))
        const val NEUES_GATE = 4
        const val NEUER_FLUGZEUGTYP = "A380"
    }
}

inline fun <reified T : Any> WebClient.RequestBodySpec.singleBody(obj: T): WebClient.RequestHeadersSpec<*> =
    body(obj.toMono())
