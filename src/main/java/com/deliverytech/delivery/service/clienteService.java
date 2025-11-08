package com.deliverytech.delivery.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.entity.cliente;
import com.deliverytech.delivery.repository.clienteRepository;

@Service
@Transactional
public class clienteService {
    @Autowired
    private clienteRepository clienteRepository;

     /**
     * Cadastrar novo cliente
     */
    public cliente cadastrar(cliente cliente) {
        // Validar email único
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + cliente.getEmail());
        }

        // Validações de negócio
        validarDadosCliente(cliente);

        // Definir como ativo por padrão
        cliente.setAtivo(true);

        return clienteRepository.save(cliente);

    }

     /**
     * Buscar cliente por Id
     */
    @Transactional (readOnly = true)
    public Optional<cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

     /**
     * Buscar cliente por email
     */
    @Transactional (readOnly = true)
    public Optional<cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    /**
     * Listar todos os clientes ativos
     */
    @Transactional (readOnly = true)
    public List<cliente> listarAtivos() {
        return clienteRepository.findByAtivoTrue();
    }

    /**
     * Atualizar dados do cliente
     */
    public cliente atualizar(Long id, cliente clienteAtualizado) {
       cliente cliente = buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

       // Verificar se o email não está sendo usado por outro cliente
       if (!cliente.getEmail().equals(clienteAtualizado.getEmail()) && clienteRepository.existsByEmail(clienteAtualizado.getEmail())) {
        throw new IllegalArgumentException("Email já cadastrado: " + clienteAtualizado.getEmail());
       }

       // Atualizar campos
       cliente.setNome(clienteAtualizado.getNome());
       cliente.setEmail(clienteAtualizado.getEmail());
       cliente.setTelefone(clienteAtualizado.getTelefone());
       cliente.setEndereco(clienteAtualizado.getEndereco());

       return clienteRepository.save(cliente);
    }

    /**
     * Inativar cliente (soft delete)
     */
    public void inativar(Long id) {
        cliente cliente = buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

        cliente.inativar();
        clienteRepository.save(cliente);
    }

    /**
     * Buscar clientes por nome
     */
    @Transactional (readOnly = true)
    public List<cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    private void validarDadosCliente(cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é orbigatório");
        }

        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (cliente.getNome().length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
    }
    
}
