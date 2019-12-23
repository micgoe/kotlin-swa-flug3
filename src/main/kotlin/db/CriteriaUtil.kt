package de.hska.flug.db

import de.hska.flug.config.logger
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flughafen
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.div
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.regex
import org.springframework.util.MultiValueMap

/**
 * Singleton-Klasse, die dazu dient Criteria Queries für MongoDb zu bauen
 *
 */

object CriteriaUtil {
    private const val flugnummer = "flugnummer"
    private const val abflugFlughafenIata = "abflugFlughafenIata"
    private const val ankunftFlughafenIata = "ankunftFlughafenIata"
//    private const val abflugzeit = "abflugzeit"
//    private const val ankunftzeit = "ankunftzeit"
    private const val gate = "gate"
//    private const val flugzeugtyp = "flugzeugtyp"
    private const val airline = "airline"
//    private const val status = "status"

    private val logger = logger()

    @Suppress("ComplexMethod", "LongMethod")
    fun getCriteria(queryParams: MultiValueMap<String, String>): List<CriteriaDefinition?> {
        val criteria = queryParams.map { (key, value) ->
            if (value?.size != 1) {
                null
            } else {
                val critVal = value[0]
                when (key) {
                    flugnummer -> getCriteriaFlugnummer(critVal)
                    abflugFlughafenIata -> getCriteriaAbflugFlughafenIata(critVal)
                    ankunftFlughafenIata -> getCriteriaAnkunftFlughafenIata(critVal)
                    airline -> getCriteriaAirline(critVal)
                    gate -> getCriteriaGate(critVal)
                    else -> null
                }
            }
        }
        logger.debug("Criteria: {}", criteria.size)
        criteria.forEach { logger.debug("Criteria: {}", it?.criteriaObject) }
        return criteria
    }

    // Vernachlässige Groß und Kleinschreibung
    // https://docs.mongodb.com/manual/reference/operator/query/#query-selectors
    private fun getCriteriaFlugnummer(flugnummer: String) = Flug::flugnummer.regex(flugnummer, "i")

    private fun getCriteriaAbflugFlughafenIata(abflugFlughafenIata: String) =
        (Flug::abflugFlughafen / Flughafen::iata).regex(abflugFlughafenIata, "i")

    private fun getCriteriaAnkunftFlughafenIata(ankunftFlughafenIata: String) =
        (Flug::ankunftFlughafen / Flughafen::iata).regex(ankunftFlughafenIata, "i")

    private fun getCriteriaAirline(airline: String) = Flug::airline.regex(airline, "i")

    private fun getCriteriaGate(gateStr: String): Criteria? {
        val gateDec = gateStr.toIntOrNull() ?: return null
        return Flug::gate isEqualTo gateDec
    }
}
