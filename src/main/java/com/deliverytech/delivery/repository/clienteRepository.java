package com.deliverytech.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.deliverytech.delivery.entity.cliente;

@Repository
public interface clienteRepository extends JpaRepository<cliente, Long> {

    // Buscar cliene por email (método derivado)
    Optional<cliente> findByEmail (String email);

    // Verificar se o email já existe
    boolean existsByEmail (String email);

    // Buscar clientes ativos
    List<cliente> findByAtivoTrue();

    // Buscar clientes por nome (contendo)
    List<cliente> findByNomeContainingIgnoreCase(String nome);

}
