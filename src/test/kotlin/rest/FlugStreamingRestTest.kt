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

import de.hska.flug.config.Settings.DEV
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_12
import org.junit.jupiter.api.condition.JRE.JAVA_13
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@Tag("streamingRest")
@DisplayName("Streaming fuer Flug testen")
@ExtendWith(SpringExtension::class, SoftAssertionsExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@TestPropertySource(locations = ["/rest-test.properties"])
@EnabledOnJre(JAVA_12, JAVA_13)
class FlugStreamingRestTest(@LocalServerPort private val port: Int, ctx: ReactiveWebApplicationContext) {
    private var baseUrl = "$SCHEMA://$HOST:$port"
    private var client = WebClient.builder()
        .filter(basicAuthentication(USERNAME, PASSWORD))
        .baseUrl(baseUrl)
        .build()

    init {
        assertThat(ctx.getBean<FlugStreamHandler>()).isNotNull
    }

    @Test
    @Order(1000)
    fun `Streaming mit allen Fluegen`(softly: SoftAssertions) = runBlocking<Unit> {
        // act
        val response = client.get()
            .header(ACCEPT, TEXT_EVENT_STREAM.toString())
            .awaitExchange()

        // assert
        with(softly) {
            assertThat(response.statusCode()).isEqualTo(OK)
            val body = response.awaitBody<String>()
            assertThat(body).startsWith("data:")

            // TODO List<Flug> durch ObjectMapper von Jackson
        }
    }

    private companion object {
        const val SCHEMA = "http"
        const val HOST = "localhost"
        const val USERNAME = "admin"
        const val PASSWORD = "p"
    }
}
