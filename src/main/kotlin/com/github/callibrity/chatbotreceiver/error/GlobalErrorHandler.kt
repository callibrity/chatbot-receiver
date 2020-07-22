package com.github.callibrity.chatbotreceiver.error

import com.github.callibrity.chatbotreceiver.response.ApiResponse
import com.github.callibrity.chatbotreceiver.response.Error as ApiError
import com.github.callibrity.chatbotreceiver.response.Meta
import io.grpc.Status
import io.grpc.StatusException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalErrorHandler {

		private val logger = LoggerFactory.getLogger(GlobalErrorHandler::class.java)

    @ExceptionHandler(StatusException::class)
    fun handleStatusRuntimeException(
        ex: StatusException,
        request: ServerHttpRequest
    ): ResponseEntity<Any> = ex
        .message
        .also {
            logger.info("Error message: $it")
            logger.info("Error uri: ${request.uri}")
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
