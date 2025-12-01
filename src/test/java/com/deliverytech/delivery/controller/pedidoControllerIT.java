package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.ItemPedidoRequestDTO;
import com.deliverytech.delivery.dto.request.PedidoRequestDTO;
import com.deliverytech.delivery.enums.StatusPedido;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PedidoControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private PedidoRequestDTO criarPedidoValido() {
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO();
        item.setProdutoId(1L); // ID deve existir no data.sql
        item.setQuantidade(1);

        PedidoRequestDTO pedido = new PedidoRequestDTO();
        pedido.setClienteId(1L); 
        pedido.setRestauranteId(1L); 
        pedido.setItens(List.of(item));
        pedido.setNumeroPedido("TESTE_IT_001");
        pedido.setEnderecoEntrega("Rua Teste IT");
        return pedido;
    }

    // Requisito: Testar POST /api/pedidos (criação de pedido completo)
    @Test
    @DisplayName("1. POST /pedidos Deve criar pedido com sucesso (201 Created)")
    @WithMockUser(authorities = "CLIENTE")
    void criarPedido_Sucesso() throws Exception {
        mockMvc.perform(post("/pedidos")
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarPedidoValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    // Requisito: Testar validação de produtos inexistentes (404)
    @Test
    @DisplayName("2. POST /pedidos deve retornar 404 para produto inexistente")
    @WithMockUser(authorities = "CLIENTE")
    void criarPedido_ProdutoInexistente() throws Exception {
        PedidoRequestDTO pedido = criarPedidoValido();
        pedido.getItens().get(0).setProdutoId(9999L); // Produto que não existe

        mockMvc.perform(post("/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound()); 
    }
    
    // Requisito: Testar PUT /api/pedidos/{id}/status (atualização de status)
    @Test
    @DisplayName("3. PUT /pedidos/{id}/status deve atualizar para CONFIRMADO com sucesso")
    @WithMockUser(authorities = "RESTAURANTE")
    void atualizarStatus_Sucesso() throws Exception {
        // Pré-condição: Pedido 1 deve existir e estar PENDENTE no data.sql
        Long pedidoId = 1L; 

        mockMvc.perform(put("/pedidos/" + pedidoId + "/" + StatusPedido.CONFIRMADO)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }
    
    // Requisito: Testar GET /api/pedidos/cliente/{id} (histórico do cliente)
    @Test
    @DisplayName("4. GET /pedidos/cliente/{id} deve retornar histórico do cliente")
    @WithMockUser(username = "joao@email.com", authorities = "CLIENTE") // Usuário João logado
    void listarHistorico_ComCliente() throws Exception {
        // data.sql garante que o ID 1 (João) tem pedidos
        Long clienteId = 1L; 

        mockMvc.perform(get("/pedidos/cliente/" + clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clienteId").value(clienteId));
    }
    
    // Teste de Segurança: Tentativa de atualização de status por Cliente
    @Test
    @DisplayName("5. PUT /pedidos/{id}/status deve retornar 403 para CLIENTE")
    @WithMockUser(authorities = "CLIENTE")
    void atualizarStatus_AcessoNegado_Cliente() throws Exception {
        Long pedidoId = 1L; 

        mockMvc.perform(put("/pedidos/" + pedidoId + "/" + StatusPedido.CONFIRMADO)
                .with(csrf()))
                .andExpect(status().isForbidden()); // 403
    }
}