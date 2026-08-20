package com.webgis.ancientdata.rastertests;

import com.webgis.ancientdata.web.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleClientAbort_DoesNotAttemptToWriteAResponseBody() {
        // Regression test: a client cancelling an in-flight WMS tile request (e.g. a
        // browser dropping a superseded tile while panning/zooming) throws
        // ClientAbortException while RasterProxyController is mid-write of the image
        // bytes. Content-Type is already committed as image/png at that point, so the
        // generic Exception handler trying to write a JSON error Map used to throw a
        // second, noisier HttpMessageNotWritableException on top of this one. This
        // handler must stay void (no response body) to avoid that.
        assertDoesNotThrow(() -> handler.handleClientAbort(new ClientAbortException("broken pipe")));
    }

    @Test
    void handleUnhandledExceptions_ReturnsNullWhenResponseAlreadyCommitted() {
        // Regression test: not every mid-stream disconnect surfaces as a ClientAbortException
        // (Spring's own logging for a failed @ExceptionHandler write doesn't reveal the real
        // triggering exception type), so this is the general-purpose safety net - if headers
        // are already flushed, writing a JSON error body is impossible and must be skipped
        // rather than attempted and thrown again.
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.isCommitted()).thenReturn(true);
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<?> result = handler.handleUnhandledExceptions(new RuntimeException("boom"), request, response);

        assertNull(result);
    }

    @Test
    void handleUnhandledExceptions_WritesErrorBodyWhenResponseNotCommitted() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.isCommitted()).thenReturn(false);
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Map<String, Object>> result =
                handler.handleUnhandledExceptions(new RuntimeException("boom"), request, response);

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        Map<String, Object> body = result.getBody();
        assertNotNull(body);
        assertEquals("boom", body.get("details"));
    }
}
