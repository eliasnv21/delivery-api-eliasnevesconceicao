package com.deliverytech.delivery.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.ItemPedidoRequestDTO;
import com.deliverytech.delivery.dto.request.PedidoRequestDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.service.pedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
public class pedidoController {

    @Autowired
    private pedidoService pedidoService;

    /**
     * Criar novo pedido
     */
    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Pedido já existe")
    })
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /**
     * Listar pedidos por cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar pedidos por cliente", description = "Lista todos os pedidos de um cliente específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos encontrados"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<List<PedidoResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Atualizar status do pedido
     */
    @PutMapping("/{pedidoId}/{status}")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido específico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Status inválido")
    })
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(@PathVariable Long pedidoId, @PathVariable StatusPedido status) {
        PedidoResponseDTO dto = pedidoService.atualizarStatusPedido(pedidoId, status);
        return ResponseEntity.ok(dto);
    }

    /**
     * Buscar pedido por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Recupera os detalhes de um pedido específico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Calcular valor total do pedido
     */
    @PostMapping("/calcular")
    @Operation(summary = "Calcular valor total do pedido", description = "Calcula o valor total de um pedido com base nos itens fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor total calculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<BigDecimal> calcularValorTotalPedido(@RequestBody List<ItemPedidoRequestDTO> itens) {
        BigDecimal valorTotal = pedidoService.calcularValorTotalPedido(itens);
        return ResponseEntity.ok(valorTotal);
    }

    /**
     * Cancelar pedido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido específico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedido);

    }

    /**
     * Listar pedidos por restaurante
     */
    @GetMapping("/restaurante/{restauranteId}")
    @Operation(summary = "Listar pedidos por restaurante", description = "Lista todos os pedidos de um restaurante específico")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorRestaurante(@PathVariable Long restauranteId) {
        // Você precisará criar este método no service: listarPedidosPorRestaurante
        List<PedidoResponseDTO> pedidos = pedidoService.findByRestauranteIdOrderByDataPedidoDesc(restauranteId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Listar pedidos por status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Listar pedidos por status", description = "Lista pedidos filtrados pelo status (ex: PENDENTE, ENTREGUE)")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorStatus(@PathVariable StatusPedido status) {
        // Criar no service: listarPedidosPorStatus
        List<PedidoResponseDTO> pedidos = pedidoService.findByStatus(status);;
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Listar pedidos recentes (últimos criados)
     */
    @GetMapping("/recentes")
    @Operation(summary = "Listar os 10 pedidos mais recentes", description = "Retorna os últimos pedidos cadastrados")
    public ResponseEntity<List<PedidoResponseDTO>> listarRecentes() {
        // Criar no service: listarPedidosRecentes (pode usar findTop10ByOrderByDataPedidoDesc)
        List<PedidoResponseDTO> pedidos = pedidoService.findTop10ByOrderByDataPedidoDesc();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Listar pedidos por período
     */
    @GetMapping("/periodo")
    @Operation(summary = "Listar pedidos por período", description = "Filtra pedidos entre duas datas")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorPeriodo(
            @RequestParam LocalDateTime inicio, // Formato ISO (ex: 2025-11-01T00:00:00)
            @RequestParam LocalDateTime fim) {
        
        // Criar no service: listarPedidosPorPeriodo(inicio, fim)
        List<PedidoResponseDTO> pedidos = pedidoService.findByDataPedidoBetween(inicio, fim);;
        return ResponseEntity.ok(pedidos);
    }

}
