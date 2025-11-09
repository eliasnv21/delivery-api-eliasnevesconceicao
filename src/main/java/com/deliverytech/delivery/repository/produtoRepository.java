package com.deliverytech.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.Produto;

@Repository
public interface produtoRepository extends JpaRepository <Produto, Long> {

    // Buscar produto por restaurante ID
    List<Produto> findByRestauranteId(Long restauranteId);

}
