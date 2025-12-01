package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.ClienteRequestDTO;
import com.deliverytech.delivery.dto.request.RegisterRequestDTO;
import com.deliverytech.delivery.repository.clienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ClienteControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private clienteRepository clienteRepository; 
    
    private final String BASE_URL = "/clientes";

    // Requisito: Testar POST /clientes (criação com dados inválidos)
    @Test
    @DisplayName("1. POST /clientes deve retornar 400 Bad Request com dados inválidos")
    @WithMockUser(authorities = "ADMIN") // Admin tem permissão para cadastrar
    void cadastrarCliente_ErroDadosInvalidos() throws Exception {
        ClienteRequestDTO clienteDTO = new ClienteRequestDTO();
        clienteDTO.setNome("AA"); // Nome muito curto (regra @Size)
        clienteDTO.setEmail("invalido"); // Email inválido

        mockMvc.perform(post(BASE_URL)
                .with(csrf()) // Necessário para POST/PUT/PATCH com segurança ativa
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(jsonPath("$.errors").isNotEmpty()); // Valida a estrutura de erro padronizada
    }

    // Requisito: Testar PUT /api/clientes/{id} (atualização)
    @Test
    @DisplayName("2. PUT /clientes/{id} deve atualizar cliente com sucesso")
    @WithMockUser(username = "joao@email.com", authorities = "CLIENTE") // Usuário logado é o João
    void atualizarCliente_ComSucesso() throws Exception {
        // Pré-condição: Os usuários do data.sql já existem (João ID 1)
        
        ClienteRequestDTO atualizacaoDTO = new ClienteRequestDTO();
        atualizacaoDTO.setNome("João Silva Novo Nome");
        atualizacaoDTO.setTelefone("999999999");
        
        // Simulação do ID 1 do data.sql
        Long joaoId = 1L;

        mockMvc.perform(put(BASE_URL + "/" + joaoId)
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(atualizacaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva Novo Nome"));
    }

    // Requisito: Testar GET /api/clientes (listagem com paginação)
    @Test
    @DisplayName("3. GET /clientes deve retornar listagem paginada (200 OK)")
    @WithMockUser(authorities = "ADMIN")
    void listarClientes_ComPaginacao() throws Exception {
        // data.sql garante que temos 3 clientes
        mockMvc.perform(get(BASE_URL + "?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); 
    }
    
    // Requisito: Testar busca por ID (existente)
    @Test
    @DisplayName("4. GET /clientes/{id} deve retornar cliente existente")
    @WithMockUser(username = "joao@email.com", authorities = "CLIENTE") // Usuário logado é o João
    void buscarPorId_Existente() throws Exception {
        // data.sql garante que o ID 1 existe (João)
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }
}
