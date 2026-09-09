package com.example.task_management_api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        write(response, 401, "Unauthorized", "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(response, 403, "Forbidden", "You do not have permission to access this resource");
    }

    private void write(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"timestamp\":\"" + Instant.now()
                + "\",\"status\":" + status + ",\"error\":\"" + error
                + "\",\"message\":\"" + message + "\"}");
    }
}
