package de.hska.flug.rest.hateoas

import de.hska.flug.entity.Flug
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * Mit der Klasse [FLugModelAssembler] können Entity-Objekte der Klasse [de.hska.flug.entity.Flug].
 * in eine HATEOAS-Repräsentation transformiert werden.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @constructor Ein FlugModelAssembler erzeugen.
 */

@Component
class FlugModelAssembler : SimpleRepresentationModelAssembler<Flug> {
    // Was ist der Unterschied zu dem alten SimpleReactiveRepresentationModelAssembler?
    lateinit var request: ServerRequest // Was ist lateinit ?

    /**
     * EntityModel eines Flug-Objektes (gemäß Spring HATEOAS) um Atom-Links zu ergänzen.
     * @param flugModel Gefundenes Flug-Objekt als EntityModel gemäß Spring HATEOAS
     * @return Model für den Flug mit Atom-Links für HATEOAS
     */
    override fun addLinks(flugModel: EntityModel<Flug>) {
        val uri = request.uri().toString()
        val id = flugModel.content?.id

        val baseUri = uri.substringBefore('?')
            .removeSuffix("/")
            .removeSuffix("$id")
        val idUri = "$baseUri/$id"

        val selfLink = Link(idUri)
        val listLink = Link(baseUri, "list")
        val addLink = Link(baseUri, "add")
        val updateLink = Link(idUri, "update")
        val removeLink = Link(idUri, "remove")
        flugModel.add(selfLink, listLink, addLink, updateLink, removeLink)
    }

    fun toModel(flug: Flug, request: ServerRequest): EntityModel<Flug> {
        this.request = request
        return toModel(flug)
    }

    override fun addLinks(resources: CollectionModel<EntityModel<Flug>>) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
