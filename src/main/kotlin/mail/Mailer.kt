package de.hska.flug.mail

import de.hska.flug.config.MailAddressProps
import de.hska.flug.config.logger
import de.hska.flug.entity.Flug
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.messaging.support.MessageBuilder.withPayload
import org.springframework.stereotype.Component

/**
 * Kafka-Client, der Nachrichten sendet, die als Email versendet werden sollen.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 */
@Component
class Mailer(
    private val mailOutput: MailOutput,
    private val probs: MailAddressProps,
    circuitBreakerFactory: CircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder>
){

    private val circuitBreaker = circuitBreakerFactory.create("mail");

    fun send(flug: Flug?) {
        val mail = Mail(
            to = probs.supervisor,
            from = probs.from,
            subject = "Flug wurde gelöscht UUID \"${flug?.id}\"",
            body = "<b>Gelöschter Flug: </b> <br> <p>${flug?.toString()} </p>"
        )
        logger.trace("$flug");

        val sendFn = { mailOutput.getChannel().send(withPayload(mail).build(), timeout)}
        val fallbackFn = {_: Throwable ->
            logger.error("Fehler bei Senden der EMail: {}", mail)
            false
        }
        circuitBreaker.run(sendFn, fallbackFn);
    }
//    fun send(flug: Flug?) {
//        val preparator = MimeMessagePreparator {
//            with(it) {
//                setFrom(InternetAddress(probs.from))
//                setRecipient(TO, InternetAddress(probs.supervisor))
//                subject = "Flug wurde gelöscht UUID: \"${flug?.id}\""
//                val body = """
//                    <b>Gelöschter Flug: </b> <br>
//                    <p>${flug?.toString()} </p>
//                    """.trimIndent()
//                logger.trace("Mail Body: {}", body)
//                setText(body)
//            }
//        }
//
//        try {
//            mailSender.send(preparator)
//        } catch (e: MailException) {
//            logger.error("Fehler beim senden der Email bzgl der Löschung eines Fluges: $flug", e)
//        }
//    }

    private companion object {
        const val timeout = 500L
        val logger = logger()
    }
}
