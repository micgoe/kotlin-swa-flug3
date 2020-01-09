package de.hska.flug.service

import com.mongodb.client.result.DeleteResult
import de.hska.flug.config.logger
import de.hska.flug.config.security.CustomUserDetailsService
import de.hska.flug.db.CriteriaUtil.getCriteria
import de.hska.flug.entity.Flug
import de.hska.flug.entity.Flugzeug
import de.hska.flug.mail.Mailer
import java.util.UUID
import javax.validation.ConstraintViolationException
import javax.validation.ValidatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
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
import org.springframework.data.mongodb.core.query.where
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.update
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.kotlin.core.publisher.toMono

/**
 * Anwendungslogik für Flug.
 *
 * [Klassendiagramm](../../../../docs/images/FlugService.png)
 *
 * @author [Michael Goehrig](mailto:goja1014@HS-Karlsruhe.de)
 */
@Service
class FlugService(
    private val mongo: ReactiveFluentMongoOperations,
    @Lazy val validatorFactory: ValidatorFactory,
    @Lazy private val mailer: Mailer,
    private val clientBuilder: WebClient.Builder,
    private val circuitBreakerFactory: ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder>
) {

    private val validator by lazy { validatorFactory.validator }
    private val circuitBreaker = circuitBreakerFactory.create("kunde")

    /**
     * Ein Flug mit seiner ID suchen
     *
     * @param id Id zur Ermittlung des gesuchten Fluges
     * @return Das gesuchte Flugobjekt oder ein Mono
     */
    suspend fun findById(id: UUID): Flug? {
        val flug = mongo.query<Flug>()
            .matching(query(Flug::id isEqualTo id))
            .awaitOneOrNull()
        logger.debug("findById: {}", flug)
        if(flug == null) {
            return flug
        }

        val (typ) = findFlugzeugById(flug.flugzeugId)
        return flug.apply { flugzeugTyp = typ }
    }

    /**
     *  FLugzeug anhand einer flugzeugId von einem anderen Rest-Server ermitteln
     *  @param flugzeugId Die Id des gesuchten Flugzeugs
     *  @return Das gesuchte Flugzeug
     */
    suspend fun findFlugzeugById(flugzeugId: UUID): Flugzeug {
        logger.debug("findFlugzeugById: {}", flugzeugId)

        val client = clientBuilder
            .baseUrl("https://$flugzeugService")
            .filter(basicAuthentication(username, password))
            .build()

        val getFlugzeugFn = client
            .get()
            .uri("/$flugzeugId")
            .retrieve()
            .bodyToMono<Flugzeug>() //
            .doOnNext { logger.debug("findFlugzeugById(): {}", it) }

         val fallbackFn = { throwable: Throwable ->
             logger.warn("findKundeById(): ${throwable.message}", throwable)
             Flugzeug(typ = "FALLBACK").toMono()
         }

        return circuitBreaker.run(getFlugzeugFn, fallbackFn).awaitFirst()
    }

    /**
     * Alle Flüge passend zu den Query Paramertern finden
     * @param queryParams Eine Map mit den Name des Queryparamters als erster Wert und dem Wert des Querys als zweiter Wert
     * @return Gefundene Flüge
     */
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

    /**
     * Flüge zur FluzeugID suchen
     * @param flugzeugId Die Id des gegebenen Flugzeugs
     * @return Die gefundenenen Flüge oder ein leeres Flux-Objekt
     */
    suspend fun findByFlugzeugId(flugzeugId: UUID): Flow<Flug> {
        val (flugzeugTyp) = findFlugzeugById(flugzeugId)

        val criteria = where(Flug::flugzeugId).regex("\\.*$flugzeugId\\.*", "i")
        return mongo.query<Flug>().matching(Query(criteria))
            .flow()
            .onEach { flug ->
                logger.debug("findByFlugzeugId() {}", flug)
                flug.flugzeugTyp = flugzeugTyp
            }
    }

    /**
     * Nach allen Flügen suchen
     * @return Alle Flüge
     */
    suspend fun findAll(): Flow<Flug> = mongo.query<Flug>()
        .flow()
        .onEach { flug ->
            logger.debug("findAll: {}", flug)
            val flugzeug = findFlugzeugById(flug.flugzeugId)
            flug.flugzeugTyp = flugzeug.typ
        }

    /**
     * Ein neuen Flug anlegen
     * @param flug Das anzulegende Flugobjekt
     * @return Das eingefügte Flugobjekt
     */
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

    /**
     * Einen vorhandenen Flug abändern
     * @param flug Das neue Flugobjekt
     * @param id Die id de zu änderten Flugobjekts
     * @param versionStr Versionsnummer
     * @throws InvalidVersionException falls die Versionsnummer nicht korrekt ist
     *
     */
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

    /**
     * Einen vorhandenen Flug löschen
     * @param id Id des zu löschenden Fluges
     * @return DeleteResult, falls das zu löschende Objekt existierte, sonst null
     */
    // @PreAuthorize("hasRole($roleMitarbeiterStr)")
    suspend fun deleteById(id: UUID): DeleteResult {
        val flug = findById(id)
        val res = mongo.remove<Flug>()
            .matching(Query(Flug::id isEqualTo id)) // Wofür der Doppelpunkt ?
            .allAndAwait()
        mailer.send(flug)
        return res
    }

    /**
     * Den User authentifizieren
     * @param username Username des Benutzers
     * @param role Rolle des Benutzers
     */
//    private suspend fun authorizeUser(username: String, role: String) {
//        val userDetails = userService.findByUsernameAndAwait(username) ?: throw InvalidAccountException(username)
//        val rollen = userDetails.authorities.map { it.authority }
//        if (!rollen.contains(role))
//            throw AccessForbiddenException(rollen)
//    }

    /**
     * Validierung eines Flugobjekts
     * @param flug Das zu validierende Objekt
     */
    private fun validate(flug: Flug) {
        val violations = validator.validate(flug)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

    companion object {

        /**
         * Name des FlugzeugService beim Server für _Service Discovery_.
         */
        const val flugzeugService = "flugzeug"

        private const val username = "admin"
        private const val password = "p"

        val logger by lazy { logger() }
    }
}
