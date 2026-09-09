package com.webgis.ancientdata.web.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Thrown by Tomcat when a client (e.g. a browser cancelling a superseded map-tile
     * request while panning/zooming) disconnects mid-response. The socket is already
     * gone, so there's no body to write back - falling through to
     * {@link #handleUnhandledExceptions} would try to write a JSON error body onto a
     * response whose Content-Type is already committed as image/png (or whatever the
     * original handler was streaming), which Spring can't do and throws a second,
     * noisier exception on top of this harmless one.
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException exception) {
        logger.debug("Client aborted connection mid-response: {}", exception.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException exception, WebRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", exception.getStatusCode().value());
        response.put("error", exception.getStatusCode());
        response.put("message", exception.getReason());
        response.put("details", exception.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, exception.getStatusCode());
    }

    /**
     * Missing static asset (e.g. a stale/renamed chunk requested by a cached HTML
     * page). This does NOT extend ResponseStatusException - it implements the
     * separate ErrorResponse interface instead - so without this handler it fell
     * through to {@link #handleUnhandledExceptions} below, which hardcodes 500 and
     * turned every ordinary 404 for a missing file into a misleading "unexpected
     * error" response.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(
            NoResourceFoundException exception, WebRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", exception.getStatusCode().value());
        response.put("error", exception.getStatusCode());
        response.put("message", "Resource not found: " + exception.getResourcePath());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, exception.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnhandledExceptions(
            Exception exception, WebRequest request, HttpServletResponse response) {

        if (response.isCommitted()) {
            // Headers (and possibly part of the body) were already flushed by the original
            // handler - e.g. RasterProxyController mid-write of an image/png tile when the
            // client disconnected. Writing a JSON error body on top of a committed response
            // isn't possible and previously threw a second, noisier
            // HttpMessageNotWritableException on top of this one, for no benefit (the client
            // that would have received it is already gone).
            logger.debug("Response already committed, dropping error body for: {}", exception.getMessage());
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", "An unexpected error occurred.");
        body.put("details", exception.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Validation failed.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", errorMessage));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDeniedException(
            AuthorizationDeniedException exception, WebRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        response.put("message", "Access Denied");
        response.put("details", exception.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}