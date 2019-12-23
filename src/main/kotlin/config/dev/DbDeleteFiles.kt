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

import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations

/**
 * Interface, um im Profil _dev_ die Dateien in _GridFS_ zu löschen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface DbDeleteFiles {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev"
     * bereitzustellen, in dem die Dateien in _GridFS_ gelöscht werden.
     * @param gridFsOps Template für Files
     * @return CommandLineRunner
     */
    @Bean
    @Description("Dateien in GridFS loeschen")
    fun dbDeleteFiles(gridFsOps: GridFsOperations) = CommandLineRunner {
        getLogger(DbDeleteFiles::class.java).warn("Alle multimedialen Dateien werden geloescht")
        gridFsOps.delete(Query())
    }
}
