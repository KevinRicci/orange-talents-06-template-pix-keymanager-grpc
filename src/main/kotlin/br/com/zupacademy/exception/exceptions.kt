package br.com.zupacademy.exception

class PixKeyExistingException(
    override val message: String
) : RuntimeException(message)

class NotFoundException(
    override val message: String
) : RuntimeException(message)

class InternalServerErrorException(
    override val message: String
) : RuntimeException(message)

class ForbiddenException(
    override val message: String
) : RuntimeException(message)