package de.hska.flug.service

import java.lang.RuntimeException

abstract class FlugServiceException(msg: String) : RuntimeException(msg)

class AccessForbiddenException(roles: Collection<String>) :
    FlugServiceException("Unzureichende Berechtigung (Rollen): $roles")

class InvalidAccountException(username: String) : FlugServiceException("Ungueltiger Account: $username")

class InvalidVersionException(version: String) : FlugServiceException("Falsche Versionsnummer: $version")
