package de.hska.flug.entity

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

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
        const val IATA_PATTERN = "[A-Z]{3}"
    }
}
