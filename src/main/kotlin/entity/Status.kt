package de.hska.flug.entity

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

enum class Status(val value: String) {
    CANCELED("CC"),
    GATEOPEN("GO"),
    GATECLOSED("GC"),
    BOARDING("BG"),
    BOARDED("BD"),
    DEPARTED("DP"),
    LANDED("LD");

    @JsonValue
    override fun toString() = value

    @ReadingConverter
    class ReadConverter : Converter<String, Status> {
        override fun convert(value: String) = build(value)
    }

    @WritingConverter
    class WriteConverter : Converter<Status, String> {
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

        fun build(value: String): Status = nameCache[value]
            ?: throw IllegalArgumentException("$value ist kein gültiger Status")
    }
}
