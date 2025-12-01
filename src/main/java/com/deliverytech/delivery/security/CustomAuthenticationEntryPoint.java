package com.deliverytech.delivery.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // Define o código 401
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); 
        response.setContentType("application/json;charset=UTF-8");

        // Cria um corpo de erro padronizado para o 401
        Map<String, Object> errorResponse = Map.of(
            "timestamp", new Date(),
            "status", HttpStatus.UNAUTHORIZED.value(),
            "error", "Não Autorizado",
            "message", "Autenticação necessária ou Token inválido/ausente.",
            "path", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
