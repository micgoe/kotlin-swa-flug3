package de.hska.flug.rest.hateoas

import de.hska.flug.entity.Flug
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * Mit der Klasse [ListFlugModelAssembler] können Entity-Objekte der Klasse [de.hska.flug.entity.Flug].
 * in eine HATEOAS-Repräsentation innerhalb einer Liste bzw. eines JSON-Arrays transformiert werden.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
@Component
class ListFlugModelAssembler : SimpleRepresentationModelAssembler<Flug> {
    /**
     * Konvertierung eines (gefundenen) Flug-Objektes in ein Model gemäß Spring HATEOAS .
     * @param flug Gefundenes Flug-Objekt oder null
     * @param request Der eingegangene Request mit insbesondere der aufgerufenen URI
     * @return Model für den Flug mit Atom-Links für HATEOAS
     */
    fun toModel(flug: Flug, request: ServerRequest): EntityModel<Flug> {
        val uri = request.uri().toString()
        val baseUri = uri.substringBefore('?').removeSuffix("/")
        val idUri = "$baseUri/${flug.id}"

        val selfLink = Link(idUri)
        return toModel(flug).add(selfLink) // Why do we only include the self link and not update, remove ...
    }

    /**
     * Konvertierung eines (gefundenen) Flug-Objektes in ein Model gemäß Spring HATEOAS.
     * @param model Gefundenes Flug-Objekt als EntityModel gemäß Spring HATEOAS
     */
    override fun addLinks(model: EntityModel<Flug>) = Unit

    /**
     * Konvertierung eines (gefundenen) Flug-Objektes in ein Model gemäß Spring HATEOAS.
     * @param model Gefundenes Flug-Objekt als CollectionModel gemäß Spring HATEOAS
     */
    override fun addLinks(model: CollectionModel<EntityModel<Flug>>) = Unit
}
