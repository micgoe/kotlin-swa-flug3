package de.hska.flug.service

import com.mongodb.client.result.DeleteResult
import de.hska.flug.config.logger
import de.hska.flug.config.security.CustomUserDetailsService
import de.hska.flug.db.CriteriaUtil.getCriteria
import de.hska.flug.entity.Flug
import de.hska.flug.mail.Mailer
import java.util.UUID
import javax.validation.ConstraintViolationException
import javax.validation.ValidatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations
import org.springframework.data.mongodb.core.allAndAwait
import org.springframework.data.mongodb.core.asType
import org.springframework.data.mongodb.core.awaitOneOrNull
import org.springframework.data.mongodb.core.findReplaceAndAwait
import org.springframework.data.mongodb.core.flow
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.oneAndAwait
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.update
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap

@Service
class FlugService(
    private val mongo: ReactiveFluentMongoOperations,
    @Lazy private val userService: CustomUserDetailsService,
    @Lazy val validatorFactory: ValidatorFactory,
    @Lazy private val mailer: Mailer
) {

    private val validator by lazy { validatorFactory.validator }

    suspend fun findById(id: UUID): Flug? {
        val flug = mongo.query<Flug>()
            .matching(query(Flug::id isEqualTo id))
            .awaitOneOrNull()
        logger.debug("findById: {}", flug)
        return flug
    }

    @Suppress("ReturnCount")
    suspend fun find(queryParams: MultiValueMap<String, String>): Flow<Flug> {
        if (queryParams.isEmpty()) {
            return findAll()
        }

        val criteria = getCriteria(queryParams)
        if (criteria.contains(null)) {
            return emptyFlow()
        }

        val query = Query()
        criteria.filterNotNull()
            .forEach { query.addCriteria(it) }
        logger.debug("{}", query)

        return mongo.query<Flug>()
            .matching(query)
            .flow()
            .onEach { flug -> logger.debug("find: {}", flug) }
        // Warum onEach und nicht forEach? Was ist der Unteschied
    }

    suspend fun findAll() = mongo.query<Flug>().flow()

    @Suppress("MaxLineLength")
    suspend fun create(flug: Flug): Flug {
        /* Eventuelle Überprüfung ob Flug schon existiert -> Nicht notwendig bzw. möglich,
           jeder Flug ist nur durch seine UUID eindeutig gegenzeichnet,
         * welche das Übergeben Flug-Objekt aber noch nicht besitzt, sondern erst bei Eintragung in die DB
        */
        // Eventuell Mailer integrieren -> Von UseCase hier unpassend

        // authorizeUser(username, roleMitarbeiterStr)
        validate(flug)

        logger.debug("create: {}", flug)
        return mongo.insert<Flug>().oneAndAwait(flug)
    }

    suspend fun update(flug: Flug, id: UUID, versionStr: String): Flug? {
        // authorizeUser(username, roleMitarbeiterStr)
        validate(flug)
        val flugDB = findById(id) ?: return null
        logger.trace("update: version={}, flugDB={}", versionStr, flugDB) // was ist trace ?
        val version = versionStr.toIntOrNull() ?: throw InvalidVersionException(versionStr)
        val neuerFlug = flug.copy(id = flugDB.id, version = version)
        return mongo.update<Flug>()
            .replaceWith(neuerFlug)
            // Woher weis es welchen Flug er ersetzen muss? Wird das automatisch durch den Primärschlüssel erledigt?
            .asType<Flug>()
            .findReplaceAndAwait()
    }

    // @PreAuthorize("hasRole($roleMitarbeiterStr)")
    suspend fun deleteById(id: UUID): DeleteResult {
        val flug = findById(id)
        val res = mongo.remove<Flug>()
            .matching(Query(Flug::id isEqualTo id)) // Wofür der Doppelpunkt ?
            .allAndAwait()
        mailer.send(flug)
        return res
    }

    private suspend fun authorizeUser(username: String, role: String) {
        val userDetails = userService.findByUsernameAndAwait(username) ?: throw InvalidAccountException(username)
        val rollen = userDetails.authorities.map { it.authority }
        if (!rollen.contains(role))
            throw AccessForbiddenException(rollen)
    }

    private fun validate(flug: Flug) {
        val violations = validator.validate(flug)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

    companion object {
        val logger by lazy { logger() }
    }
}
