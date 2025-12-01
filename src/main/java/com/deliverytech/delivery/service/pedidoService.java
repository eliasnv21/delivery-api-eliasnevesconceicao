package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.deliverytech.delivery.dto.request.ItemPedidoRequestDTO;
import com.deliverytech.delivery.dto.request.PedidoRequestDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.enums.StatusPedido;
public interface pedidoService {

    PedidoResponseDTO criarPedido(PedidoRequestDTO dto);

    PedidoResponseDTO buscarPorId(Long id);

    List<PedidoResponseDTO> listarPedidosPorCliente(Long clienteId);

    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);

    BigDecimal calcularValorTotalPedido(List<ItemPedidoRequestDTO> itens );

    PedidoResponseDTO cancelarPedido(Long id);

    List<PedidoResponseDTO> findByRestauranteIdOrderByDataPedidoDesc(Long restauranteId);

    List<PedidoResponseDTO> findByStatus(StatusPedido status);

    List<PedidoResponseDTO> findTop10ByOrderByDataPedidoDesc();

    List<PedidoResponseDTO> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);

} 