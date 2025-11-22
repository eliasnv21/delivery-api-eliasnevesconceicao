package com.deliverytech.delivery.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.Produto;

@Repository
public interface produtoRepository extends JpaRepository <Produto, Long> {

    // Buscar produto por restaurante ID
    List<Produto> findByRestauranteId(Long restauranteId);

    // Apenas produtos disponíveis
    List<Produto> findByDisponivelTrue();

    // Produtos por categoria
    List<Produto> findByCategoria(String categoria);

    // Por faixa de preço (menor ou igual)
    List<Produto> findByPrecoLessThanEqual(BigDecimal preco);

    // Buscar produto por nome
    Produto findByNome(String nome);

}
