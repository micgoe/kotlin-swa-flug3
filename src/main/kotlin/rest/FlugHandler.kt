package de.hska.flug.rest

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import de.hska.flug.Router.Companion.idPathVar
import de.hska.flug.config.logger
import de.hska.flug.entity.Flug
import de.hska.flug.rest.constraints.FlugConstraintViolation
import de.hska.flug.rest.hateoas.FlugModelAssembler
import de.hska.flug.rest.hateoas.ListFlugModelAssembler
import de.hska.flug.service.FlugService
import de.hska.flug.service.FlugServiceException
import de.hska.flug.service.InvalidVersionException
import java.lang.IllegalArgumentException
import java.net.URI
import java.util.UUID
import javax.validation.ConstraintViolationException
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.core.codec.DecodingException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.HttpStatus.PRECONDITION_FAILED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Component
@Suppress("TooManyFunctions")
class FlugHandler(
    private val service: FlugService,
    private val modelAssembler: FlugModelAssembler,
    private val listModelAssembler: ListFlugModelAssembler
) {

    suspend fun findById(request: ServerRequest): ServerResponse {
        val idStr = request.pathVariable(idPathVar)
        val id = UUID.fromString(idStr)

        // Include User Login Stuff
        val flug = service.findById(id)
        // Check for Exeption of Login

        logger.debug("findById: {}", flug)

        if (flug == null) {
            return notFound().buildAndAwait() // Was hat es mit dem Await auf sich? Zwecks Reactive?
        }
        return toResponse(flug, request)
    }

    private suspend fun toResponse(flug: Flug, request: ServerRequest): ServerResponse {
        // Include Version Header
        val versionHeader = getIfNoneMatch(request)
        val versionStr = "\"${flug.version}\""
        if (versionStr == versionHeader)
            return status(NOT_MODIFIED).buildAndAwait()

        val flugModel = modelAssembler.toModel(flug, request)
        return ok().eTag("\"$flug.version}\"").bodyValueAndAwait(flugModel)
    }

    private fun getIfNoneMatch(request: ServerRequest): String? {
        val versionHeaderList = try {
            request.headers().asHttpHeaders().ifNoneMatch
        } catch (e: IllegalArgumentException) {
            emptyList<String>()
        }

        val versionHeader = versionHeaderList.firstOrNull()
        logger.debug("versionHeader: {}", versionHeader)
        return versionHeader
    }

    suspend fun find(request: ServerRequest): ServerResponse {
        val queryParams = request.queryParams()
        val fluege = mutableListOf<Flug>()
        service.find(queryParams)
            .onEach { flug -> logger.debug("find: {}", flug) }
            .toList(fluege) // Check in again about these functions

        if (fluege.isEmpty()) {
            logger.debug("find(): Keine Fluege gefunden")
            return notFound().buildAndAwait()
        }

        val fluegeModel = fluege.map { flug -> listModelAssembler.toModel(flug, request) }
        // What is the difference between .map and forEach
        logger.debug("find(): {}", fluegeModel)
        return ok().bodyValueAndAwait(fluegeModel)
    }

    @Suppress("ReturnCount")
    suspend fun create(request: ServerRequest): ServerResponse {
        val flug = try {
            request.awaitBody<Flug>()
        } catch (e: DecodingException) {
            return handleDecodingException(e) // Why is it not Thrown
        }

        // val username = getUsername(request)
        val neuerFlug = try {
            service.create(flug)
        } catch (e: ConstraintViolationException) {
            return handleConstraintViolation(e)
        } catch (e: FlugServiceException) {
            val msg = e.message ?: ""
            return badRequest().bodyValueAndAwait(msg)
        } // Some more Exceptions if necessary

        logger.trace("Kunde abspeichern: {}", neuerFlug) // Was ist Trace?
        val location = URI("${request.uri()}${neuerFlug.id}")
        return created(location).buildAndAwait()
    }

    @Suppress("MagicNumber", "ReturnCount")
    suspend fun update(request: ServerRequest): ServerResponse {
        var version = getIfMatch(request)
            ?: return status(PRECONDITION_FAILED).bodyValueAndAwait("Versionsummer: felt oder falsche Syntax")
        if (version.length < 3) {
            return status(PRECONDITION_FAILED).bodyValueAndAwait("Falsche Versionsummer: $version")
        }
        logger.trace("Versionnummer $version")
        version = version.substring(1, version.length - 1)

        val idStr = request.pathVariable(idPathVar)
        val id = UUID.fromString(idStr)

        val flug = try {
            request.awaitBody<Flug>()
        } catch (e: DecodingException) {
            return handleDecodingException(e)
        }
        // val username = getUsername(request)
        return update(flug, id, version)
    }

    private fun getIfMatch(request: ServerRequest): String? {
        val versionList = try {
            request.headers().asHttpHeaders().ifMatch
            // Check ob kompletter Header matched ? Und wenn zu was ? Oder nur ob exisitert?
        } catch (e: IllegalArgumentException) {
            null
        }
        return versionList?.firstOrNull()
    }

    @Suppress("MaxLineLength", "ReturnCount")
    private suspend fun update(flug: Flug, id: UUID, version: String): ServerResponse {
        val updatedFlug = try {
            service.update(flug, id, version) ?: return notFound().buildAndAwait()
        } catch (e: ConstraintViolationException) {
            return handleConstraintViolation(e)
        } catch (e: InvalidVersionException) {
            val msg = e.message ?: ""
            logger.trace("InvalidVersionException: {}", msg)
            return status(PRECONDITION_FAILED).bodyValueAndAwait(msg)
        } catch (e: OptimisticLockingFailureException) {
            val msg = e.message ?: ""
            logger.trace("OptimisticLockingFailureException: {}", msg)
            return status(PRECONDITION_FAILED).bodyValueAndAwait("Falsche Versionsnummer $version")
        }

        logger.trace("Flug aktualisiert {}", updatedFlug)
        return noContent().eTag("\"$updatedFlug.version}\"").buildAndAwait() // Muss mit "" umklammert sein
    }

    suspend fun deleteById(request: ServerRequest): ServerResponse {
        val idStr = request.pathVariable(idPathVar)
        val id = UUID.fromString(idStr)
        // val username = getUsername(request)
        val deleteResult = service.deleteById(id)
        logger.debug("deleteById: {}", deleteResult)
        return noContent().buildAndAwait()
    }

    private suspend fun getUsername(request: ServerRequest): String {
        val principal = request.principal().awaitFirst()
        val username = principal.name
        logger.debug("username: {}", username)
        return username
    }

    private suspend fun handleConstraintViolation(exception: ConstraintViolationException):
        ServerResponse {
        val violations = exception.constraintViolations
        if (violations.isEmpty()) {
            return badRequest().buildAndAwait()
        }

        val flugViolations = violations.map { violation ->
            FlugConstraintViolation(
                property = violation.propertyPath.toString(),
                message = violation.message
            )
        }
        logger.debug("violation(): {}", flugViolations)
        return badRequest().bodyValueAndAwait(flugViolations)
    }

    private suspend fun handleDecodingException(e: DecodingException) = when (val exception = e.cause) {
        is JsonParseException -> {
            logger.debug("handleDecodingException(): JsonParseException={}", exception.message)
            val msg = exception.message ?: ""
            badRequest().bodyValueAndAwait(msg)
        }
        is InvalidFormatException -> {
            logger.debug("handleDecodingException(): InvalidFormatException={}", exception.message)
            val msg = exception.message ?: ""
            badRequest().bodyValueAndAwait(msg)
        }
        else -> status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
    }

    companion object {
        val logger by lazy { logger() }
    }
}
