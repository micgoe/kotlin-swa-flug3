/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import java.security.Security
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

/**
 * Interface, um im Profil _dev_ die im JDK vorhandenen _Signature_-Algorithmen aufzulisten.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
interface LogSignatureAlgorithms {
    /**
     * Bean-Definition, um einen CommandLineRunner im Profil "dev" zusammen mit `debugSignatureAlgorithms`
     * bereitzustellen, damit die im JDK vorhandenen _Signature_-Algorithmen aufgelistet werden.
     * @return CommandLineRunner
     */
    @Bean
    @Profile("debugSignatureAlgorithms")
    fun logSignatureAlgorithms() = CommandLineRunner {
        val logger = getLogger(LogSignatureAlgorithms::class.java)
        Security.getProviders().forEach { provider ->
            provider.services.forEach { service ->
                if (service.type == "Signature") {
                    logger.warn(service.algorithm)
                }
            }
        }
    }
}
