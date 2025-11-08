package com.deliverytech.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.pedido;

@Repository
public interface pedidoRepository extends JpaRepository <pedido, Long> {

    //Buscar pedidos por cliente ID
    List<pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);

    // Buscar por número do pedido
    pedido findByNumeropedido(String numeroPedido);

    //Buscar pedidos por restaurante ID
    List<pedido> findByRestauranteIdOrderByDataPedidoDesc (Long restauranteId);


}
