package de.hska.flug.mail

import de.hska.flug.config.MailAddressProps
import de.hska.flug.config.logger
import de.hska.flug.entity.Flug
import javax.mail.Message.RecipientType.TO
import javax.mail.internet.InternetAddress
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component

@Component
class Mailer(private val mailSender: JavaMailSender, private val probs: MailAddressProps) {

    fun send(flug: Flug?) {
        val preparator = MimeMessagePreparator {
            with(it) {
                setFrom(InternetAddress(probs.from))
                setRecipient(TO, InternetAddress(probs.supervisor))
                subject = "Flug wurde gelöscht UUID: \"${flug?.id}\""
                val body = """
                    <b>Gelöschter Flug: </b> <br> 
                    <p>${flug?.toString()} </p>
                    """.trimIndent()
                logger.trace("Mail Body: {}", body)
                setText(body)
            }
        }

        try {
            mailSender.send(preparator)
        } catch (e: MailException) {
            logger.error("Fehler beim senden der Email bzgl der Löschung eines Fluges: $flug", e)
        }
    }

    private companion object {
        val logger = logger()
    }
}
