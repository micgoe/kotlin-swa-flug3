package de.hska.flug.entity

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

/**
 * Entitiy-Klasse - Representiert den Status des Fluges.
 *
 * @author [Michael Goehrig](mailto:goja1014@HS-Karlsruhe.de)
 * @property value Der interne Wert
 */
enum class Status(val value: String) {
    /**
     * CANCELED mit dem internen Wert `CC` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    CANCELED("CC"),
    /**
     * GATEOPEN mit dem internen Wert `GO` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    GATEOPEN("GO"),
    /**
     * GATECLOSED mit dem internen Wert `GC` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    GATECLOSED("GC"),
    /**
     * BOARDING mit dem internen Wert `BG` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    BOARDING("BG"),
    /**
     * BOARDED mit dem internen Wert `BD` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    BOARDED("BD"),
    /**
     * DEPARTED mit dem internen Wert `DP` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    DEPARTED("DP"),
    /**
     * LANDED mit dem internen Wert `LD` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    LANDED("LD");

    @JsonValue

    /**
     *  Ausgabe des Enums als String. Verwendet wird der Interne Wert (Bsp. GO)
     *  Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
     */
    override fun toString() = value

    /**
     *  Convertiert beim lesen aus der DB in eine Enum Objekt
     */
    @ReadingConverter
    class ReadConverter : Converter<String, Status> {
        /**
         * Baut das Enum Objekt
         * @param value Wert um ein Enum-Objekt zu bauen
         * @return Enum-Objekt
         */
        override fun convert(value: String) = build(value)
    }

    /**
     *  Convertiert für das Schreiben in die DB das Enum-Objekt in einen String. Verwendet wird der interne Wert
     */
    @WritingConverter
    class WriteConverter : Converter<Status, String> {
        /**
         *  Convertiert Enum in String
         *  @param status Der Status als Enum-Objekt
         *  @return String mit dem internen Wert des Enums
         */
        override fun convert(status: Status) = status.value
    }

    companion object {

        private val nameCache = HashMap<String, Status>().apply {
            enumValues<Status>().forEach {
                put(it.value, it)
                put(it.value.toLowerCase(), it)
                put(it.name, it)
                put(it.name.toLowerCase(), it)
            }
        }

        /**
         * Konvertieren eines String in einen Enum-Wert
         * @param value zu konvertierender Wert als String
         * @return Passendes Enum
         * @throws IllegalArgumentException Wenn übergebener String keinen Enum-Wert zuordbar ist
         */
        fun build(value: String): Status = nameCache[value]
            ?: throw IllegalArgumentException("$value ist kein gültiger Status")
    }
}
