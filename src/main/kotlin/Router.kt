package de.hska.flug

import de.hska.flug.entity.Flug
import de.hska.flug.rest.FlugHandler
import de.hska.flug.rest.FlugStreamHandler
import org.springframework.context.annotation.Bean
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.coRouter

interface Router {

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
        const val idPathVar = "id"
        const val idPathPattern = "{$idPathVar:${Flug.ID_PATTERN}}"

        const val multimediaPath = "/multimedia"
        const val authPath = "/auth"
        const val versionPath = "/version"
        const val prefixPathVar = "prefix"

        // private val logger = logger()
    }
}
