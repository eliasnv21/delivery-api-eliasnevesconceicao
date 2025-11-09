package com.deliverytech.delivery.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.entity.PedidoDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.repository.pedidoRepository;
import com.deliverytech.delivery.repository.produtoRepository;
import com.deliverytech.delivery.repository.restauranteRepository;

@Service
public class pedidoService {

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private clienteRepository clienteRepository;

    @Autowired
    private restauranteRepository restauranteRepository;

    @Autowired
    private produtoRepository produtoRepository;

    /**
     * Criar novo pedido
     */
    public Pedido criarPedido(PedidoDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + dto.getClienteId()));

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId()).orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + dto.getRestauranteId()));

        if (!cliente.getAtivo()) {
            throw new IllegalArgumentException("Cliente inativo não pode fazer pedidos");
        }

        if (!restaurante.getAtivo()) {
            throw new IllegalArgumentException("Restaurante não está disponível");
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(cliente.getId());
        pedido.setRestaurante(restaurante);
        pedido.setStatus(StatusPedido.PENDENTE.name());
        pedido.setDataPedido(dto.getDataPedido());
        pedido.setNumeroPedido(dto.getNumeroPedido());
        pedido.setValorTotal(dto.getValorTotal());
        pedido.setObservacoes(dto.getObservacoes());
        pedido.setItens(dto.getItens());

        return pedidoRepository.save(pedido);
    }

    /**
     * Listar pedidos por cliente
     */
    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente (Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
    }

    /**
     * Atualizar status do pedido
     */
    public Pedido atualizarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + pedidoId));

        if (pedido.getStatus().equals(StatusPedido.ENTREGUE.name())) {
            throw new IllegalArgumentException("Pedido já finalizado: " + pedidoId);
        }

        pedido.setStatus(status.name());
        return pedidoRepository.save(pedido);
    }

}
