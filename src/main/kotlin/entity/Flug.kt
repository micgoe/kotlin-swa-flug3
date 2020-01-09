package de.hska.flug.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Future
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version

/**
 * Entity-Klasse für FLug. Unveränderinglich und in DDD ein "Aggregate Root"
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @param id Einen für den Flug eindeutig identifizierbare ID
 * @param version Das Version-Tag
 * @param flugnummer Die Flugnummer des Fluges
 * @param abflugFlughafen Der Flughafen auf dem das Flugzeug startet
 * @param ankunftFlughafen Der Flughafen auf dem das Flugzeug landet
 * @param abflugzeit Der Abflugzeitpunkt
 * @param ankunftzeit Der Ankunftszeitpunkt
 * @param gate Das Gate des Abflug-Flughafen
 * @param flugzeugId Der Flugzeugtyp des Fluges
 * @param airline Die Airline des angebotenen Fluges
 * @param status Der aktuelles Status des Fluges
 */
@JsonPropertyOrder(
    "flugnummer", "abflugFlughafen", "ankunftFlughafen", "abflugZeit",
    "ankunftZeit", "gate", "flugzeug", "airline", "status"
)

data class Flug(
    @JsonIgnore
    val id: UUID?,

    @Version
    @JsonIgnore
    val version: Int? = null, // Default Wert ?

    @get:NotEmpty(message = "{flug.flugnummer.notEmpty}")
    val flugnummer: String,

    @get:Valid
    val abflugFlughafen: Flughafen,

    @get:Valid
    val ankunftFlughafen: Flughafen,

    @get:FutureOrPresent(message = "{flug.abflugzeit.futureOrPresent}")
    @get:NotNull(message = "{flug.abflugzeit.notNull}")
    val abflugzeit: LocalDateTime,

    @get:Future(message = "{flug.ankunftzeit.future}")
    @get:NotNull(message = "{flug.ankunftzeit.notNull}")
//    @get:Min(value = abflugzeit, message =  "{flug.abflugzeit.min}")
    val ankunftzeit: LocalDateTime,

    @get:Positive(message = "{flug.gate.positive}")
    val gate: Int,

    val flugzeugId: UUID,

    @get:NotEmpty(message = "{flug.airline.notEmpty}")
    val airline: String,

    val status: Status?,

    @CreatedDate
    @JsonIgnore
    private val erzeugt: LocalDateTime? = null,

    @LastModifiedDate
    @JsonIgnore
    private val aktualisiert: LocalDateTime? = null
) {
    @Transient
    var flugzeugTyp: String? = null

    /**
     *  Ein Flug kann als gleich behandelt werden, ist die Flugnummer und die Abflugzeit die gleiche. equals überprüft diese Faktoren
     *  @param Das zu vergleichende Objekt
     *  @return true wenn es gleich ist, false wenn es ungleich ist
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Flug
        return flugnummer == other.flugnummer && abflugzeit == other.abflugzeit
    }

    /**
     * Der Hashcodes des Flugobjektes. Berechnet aus der Flugnummer und der Abflugzeit
     */
    override fun hashCode() = flugnummer.hashCode() + abflugzeit.hashCode()

    /**
     * Eine Ausgabe des Fluges als String
     */
    override fun toString() = "Flug(id=$id, flugnummer=$flugnummer, abflugFlughafen=$abflugFlughafen, " +
            "ankunftFlughafen=$ankunftFlughafen, abflugzeit=$abflugzeit, ankunftzeit=$ankunftzeit, gate=$gate, " +
            "flugzeugtyp=$flugzeugId, airline=$airline, status=$status, erzeugt=$erzeugt, aktualisiert=$aktualisiert"

    companion object {
        private const val HEX_PATTERN = "[\\dA-Fa-f]"
        /**
         * Das Pattern der UUID
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"
    }
}
