package com.deliverytech.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.restaurante;

@Repository
public interface restauranteRepository extends JpaRepository<restaurante, Long> {

    //Buscar por nome
    Optional<restaurante> findByNome(String nome);

    // Buscar restaurantes ativos
    List<restaurante> findByAtivoTrue();

    // Buscar por categoria
    List<restaurante> findByCategoria(String categoria);



}
