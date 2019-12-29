package de.hska.flug

import de.hska.flug.entity.Flug
import de.hska.flug.rest.FlugHandler
import de.hska.flug.rest.FlugStreamHandler
import org.springframework.context.annotation.Bean
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.coRouter

interface Router {

    /**
     * Bean Funktion um die Routen von SpringWebFlux zu konfigurieren
     *
     *@param handler Objekt der Handler Klasse [FlugHandler] um Request zu bearbeiten
     *
     *@param streamHandler Objekt der Stream Handler Klasse [FlugStreamHandler] um Streams zu verarbeiten
     *
     */

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection", "LongMethod")
    fun router(handler: FlugHandler, streamHandler: FlugStreamHandler) = coRouter {
            accept(HAL_JSON).nest {
                GET("/", handler::find)
                GET("/$idPathPattern", handler::findById)
            }

            POST("/", handler::create)
            PUT("/$idPathPattern", handler::update)

            DELETE("/$idPathPattern", handler::deleteById)

            accept(TEXT_EVENT_STREAM).nest {
                GET("/", streamHandler::findAll)
            }
    }

    companion object {
        /**
         * Name der Pfadvariable für Ids
         */
        const val idPathVar = "id"

        /**
         * Das Pattern für die Pfadvariable ID
         */
        const val idPathPattern = "{$idPathVar:${Flug.ID_PATTERN}}"

        /**
         * Pfad für Multimedia Dateien
         */
        const val multimediaPath = "/multimedia"
        /**
         * Pfad für Authentifizierung
         */
        const val authPath = "/auth"
        /**
         * Pfad um Version abzufragen
         */
        const val versionPath = "/version"

        // private val logger = logger()
    }
}
