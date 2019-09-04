package com.github.callibrity.chatbotreceiver.error

import io.grpc.StatusRuntimeException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalErrorHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(StatusRuntimeException::class)
    fun handleStatusRuntimeException(
        ex: StatusRuntimeException,
        request: WebRequest
    ): ResponseEntity<Any> = ex
        .message
        .run {
            handleExceptionInternal(
                ex,
                this,
                HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                request
            )
        }
}
