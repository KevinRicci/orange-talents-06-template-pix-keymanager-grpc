package br.com.zupacademy.exception.handler

import br.com.zupacademy.exception.*
import io.grpc.Status
import javax.validation.ConstraintViolationException

/**
 * By design, this class must NOT be managed by Micronaut
 */
class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is PixKeyExistingException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is NotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            is ForbiddenException -> Status.PERMISSION_DENIED.withDescription(e.message)
            is InternalServerErrorException -> Status.INTERNAL.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            else -> Status.UNKNOWN
        }
        return ExceptionHandler.StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}