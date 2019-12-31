package de.hska.flug.rest.constraints
/**
 * Datensatz für eine Verletzung gemäß Hibernate Validator.
 *
 * @author [Michael Goehrig](mailto: goja1014@HS-Karlsruhe.de)
 *
 * @property property Name der Flug-Property bei der es eine Verletzung gibt.
 * @property message Die zugehörige Fehlermeldung.
 */
data class FlugConstraintViolation(val property: String, val message: String?)
