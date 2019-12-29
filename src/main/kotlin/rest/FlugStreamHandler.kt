package de.hska.flug.rest

import de.hska.flug.rest.hateoas.ListFlugModelAssembler
import de.hska.flug.service.FlugService
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyAndAwait

/**
 * Eine Handler-Function wird von der Router-Function [de.hska.flug.Router.router]
 * aufgerufen, nimmt einen Request entgegen und erstellt den Response.
 *
 * @author [Michael Goehrig](mailto:goja1014@HS-Karlsruhe.de)
 *
 * @constructor Einen FlugStreamHandler mit einem injizierten
 *      [de.hska.kunde.service.FlugStreamService] erzeugen.
 */
@Component
class FlugStreamHandler(private val service: FlugService, private val modelAssembler: ListFlugModelAssembler) {

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val flug = service.findAll()
            .map { modelAssembler.toModel(it, request) }

        return ok().contentType(TEXT_EVENT_STREAM).bodyAndAwait(flug)
    }
}
