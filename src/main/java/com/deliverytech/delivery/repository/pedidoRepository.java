package com.deliverytech.delivery.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.enums.StatusPedido;

@Repository
public interface pedidoRepository extends JpaRepository <Pedido, Long> {

    //Buscar pedidos por cliente ID
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);

    // Pedidos por cliente
    List<Pedido> findByClienteId(Long clienteId);

    // Buscar por número do pedido
    Pedido findByNumeroPedido(String numeroPedido);

    //Buscar pedidos por restaurante ID
    List<Pedido> findByRestauranteIdOrderByDataPedidoDesc (Long restauranteId);

     // Pedidos por status
    List<Pedido> findByStatus(String status);

    // 10 pedidos mais recentes
    List<Pedido> findTop10ByOrderByDataPedidoDesc();

    // Pedidos por período
    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);

    


}
