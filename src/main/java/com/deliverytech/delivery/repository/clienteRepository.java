package com.deliverytech.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deliverytech.delivery.entity.Cliente;

@Repository
public interface clienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar cliene por email (método derivado)
    Optional<Cliente> findByEmail (String email);

    // Verificar se o email já existe
    boolean existsByEmail (String email);

    // Buscar clientes ativos
    List<Cliente> findByAtivoTrue();

    // Buscar clientes por nome (contendo)
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

}
