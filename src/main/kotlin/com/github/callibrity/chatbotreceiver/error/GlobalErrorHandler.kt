package com.github.callibrity.chatbotreceiver.error

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Error as ApiError
import com.github.callibrity.chatbotreceiver.response.Meta
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalErrorHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler(StatusException::class)
    fun handleStatusRuntimeException(
        ex: StatusException,
        request: WebRequest
    ): ResponseEntity<Any> = ex
        .message
        .also {
            logger.info("Error message: $this")
            logger.error(ex.printStackTrace())
        }
        .run {
          handleException(ex)
        }

    private fun handleException(ex: StatusException): ResponseEntity<Any> {
      logger.error(
        "${ex.status.description}: ${ex.message}",
        ex.cause
      )

      val body = ApiResponse(
        data = null,
        meta = Meta.DEFAULT_META,
        errors = listOf(
          ApiError(
            code = ex.status.code.toString(),
            cause = ex.message,
            timestamp = System.currentTimeMillis()
          )
        )
      )

      return when (ex.status) {
        Status.INTERNAL ->
          ResponseEntity(
            body,
            HttpStatus.INTERNAL_SERVER_ERROR
          )
        Status.DEADLINE_EXCEEDED ->
          ResponseEntity(
            body,
            HttpStatus.REQUEST_TIMEOUT
          )
        else ->
          ResponseEntity(
            body,
            HttpStatus.I_AM_A_TEAPOT
          )
      }
    }
}
