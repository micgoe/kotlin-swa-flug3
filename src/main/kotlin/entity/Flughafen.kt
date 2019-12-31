package de.hska.flug.entity

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

/**
 * Entity-Klasse für Flufhafen.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @param iata Der Iata Code des Fluges
 * @param name Name des Flughafens
 * @param land Land indem der Flufhafen liegt
 */
data class Flughafen(
    @get:NotEmpty(message = "{flughafen.iata.notEmpty}")
    @get:Pattern(regexp = IATA_PATTERN, message = "{flughafen.iata.pattern}")
    val iata: String,

    @get:NotEmpty(message = "{flughafen.name.notEmpty}")
    val name: String,

    @get:NotEmpty(message = "{flughafen.land.notEmpty}")
    val land: String
) {

    companion object {
        /**
         * Das Pattern für einen gültigen IATA Code
         */
        const val IATA_PATTERN = "[A-Z]{3}"
    }
}
