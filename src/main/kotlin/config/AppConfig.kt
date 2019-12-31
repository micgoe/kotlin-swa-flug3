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

import de.hska.flug.Router
import de.hska.flug.config.security.PasswordEncoder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL
import org.springframework.hateoas.support.WebStack.WEBFLUX
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
@Configuration(proxyBeanMethods = false)
@EnableHypermediaSupport(type = [HAL], stacks = [WEBFLUX])
@EnableConfigurationProperties(MailProps::class, MailAddressProps::class)
@EnableMongoAuditing
@EnableWebFluxSecurity
class AppConfig :
    Router,
    DbConfig,
    PasswordEncoder,
    SecurityConfig,
    TransactionConfig,
    ValidatorFactoryConfig
