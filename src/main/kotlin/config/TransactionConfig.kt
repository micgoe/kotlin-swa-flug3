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
package de.hska.flug.config

import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.MongoTransactionManager

/**
 * Spring-Konfiguration für den transaktionalen Zugriff auf _MongoDB_ .
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
interface TransactionConfig {
    /**
     * Transaktionsmanager für den transaktionalen Zugriff auf _MongoDB_ bereitstellen.
     * @return Transaktionsmanager für _MongoDB_.
     */
    @Bean
    fun transactionManager(dbFactory: MongoDbFactory) = MongoTransactionManager(dbFactory)
}
