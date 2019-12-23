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
import org.springframework.data.annotation.Version

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

    val flugzeugtyp: String?,

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Flug
        return flugnummer == other.flugnummer && abflugzeit == other.abflugzeit
    }

    override fun hashCode() = flugnummer.hashCode() + abflugzeit.hashCode()

    override fun toString() = "Flug(id=$id, flugnummer=$flugnummer, abflugFlughafen=$abflugFlughafen, " +
            "ankunftFlughafen=$ankunftFlughafen, abflugzeit=$abflugzeit, ankunftzeit=$ankunftzeit, gate=$gate, " +
            "flugzeugtyp=$flugzeugtyp, airline=$airline, status=$status, erzeugt=$erzeugt, aktualisiert=$aktualisiert"

    companion object {
        private const val HEX_PATTERN = "[\\dA-Fa-f]"
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"
    }
}
