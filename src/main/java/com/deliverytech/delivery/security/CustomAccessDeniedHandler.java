package com.deliverytech.delivery.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    // Objeto para converter nosso erro em JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        // Define o código 403
        response.setStatus(HttpStatus.FORBIDDEN.value()); 
        response.setContentType("application/json;charset=UTF-8");
        
        // Cria um corpo de erro padronizado para o 403
        Map<String, Object> errorResponse = Map.of(
            "timestamp", new Date(),
            "status", HttpStatus.FORBIDDEN.value(),
            "error", "Acesso Proibido",
            "message", "Você não tem permissão para acessar este recurso.",
            "path", request.getRequestURI()
        );

        // Escreve o JSON na resposta
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
