package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.RestauranteRequestDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.pedidoRepository;
import com.deliverytech.delivery.repository.produtoRepository;
import com.deliverytech.delivery.repository.restauranteRepository;

@Service
public class restauranteService {

    @Autowired
    private restauranteRepository restauranteRepository;

    @Autowired
    private produtoRepository produtoRepository;

    @Autowired
    private pedidoRepository pedidoRepository;

    /**
     * Cadastrar novo restaurante
     */
    public Restaurante cadastrar(Restaurante restaurante) {
        // Validar nome único
        if (restauranteRepository.findByNome(restaurante.getNome()).isPresent()) {
            throw new IllegalArgumentException("Restaurante " + restaurante.getNome() + " já cadastrado.");
        }

        validarDadosRestaurante(restaurante);
        restaurante.setAtivo(true);

        return restauranteRepository.save(restaurante);
    }

    /**
     * Buscar por ID
     */
    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorId(Long id) {
        return restauranteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RestauranteRequestDTO> findById(Long id) {
        Optional<Restaurante> byId = restauranteRepository.findById(id);
        if (byId.isEmpty()) {
            return Optional.empty();
        }
        return byId.map(restaurante -> new RestauranteRequestDTO(
                restaurante.getId(),
                restaurante.getNome(),
                restaurante.getCategoria(),
                restaurante.getEndereco(),
                restaurante.getTelefone(),
                restaurante.getTaxaEntrega(),
                restaurante.getAvaliacao(),
                restaurante.getAtivo()));
    }

    /**
     * Listar restaurantes ativos
     */
    @Transactional(readOnly = true)
    public List<RestauranteRequestDTO> listarAtivos() {
        List<Restaurante> byAtivoTrue = restauranteRepository.findByAtivoTrue();
        if (byAtivoTrue.isEmpty()) {
            throw new IllegalArgumentException("Nenhum restaurante ativo encontrado.");
        }
        return byAtivoTrue.stream()
            .map(restaurante -> new RestauranteRequestDTO(
                restaurante.getId(),
                restaurante.getNome(),
                restaurante.getCategoria(),
                restaurante.getEndereco(),
                restaurante.getTelefone(),
                restaurante.getTaxaEntrega(),
                restaurante.getAvaliacao(),
                restaurante.getAtivo()))
            .toList();
    }

    /**
     * Buscar por categoria
     */
    @Transactional(readOnly = true)
    public List<RestauranteRequestDTO> buscarPorCategoria(String categoria) {
        List<Restaurante> byCategoria = restauranteRepository.findByCategoria(categoria);
        if (byCategoria.isEmpty()) {
            throw new IllegalArgumentException("Nenhum restaurante encontrado para a categoria: " + categoria);
        }

        return byCategoria.stream()
            .map(restaurante -> new RestauranteRequestDTO(
                restaurante.getId(),
                restaurante.getNome(),
                restaurante.getCategoria(),
                restaurante.getEndereco(),
                restaurante.getTelefone(),
                restaurante.getTaxaEntrega(),
                restaurante.getAvaliacao(),
                restaurante.getAtivo()))
            .toList();
    }

    /**
     * Atualizar restaurante
     */
    public Restaurante atualizar(Long id, Restaurante restauranteAtualizado) {
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante " + id + " não encontrado."));

        // Verificar nome único (se mudou)
        if (!restaurante.getNome().equals(restauranteAtualizado.getNome()) &&
            restauranteRepository.findByNome(restauranteAtualizado.getNome()).isPresent()) {
            throw new IllegalArgumentException("Nome " + restauranteAtualizado.getNome() + " já cadastrado.");
        }

        restaurante.setNome(restauranteAtualizado.getNome());
        restaurante.setCategoria(restauranteAtualizado.getCategoria());
        restaurante.setEndereco(restauranteAtualizado.getEndereco());
        restaurante.setTelefone(restauranteAtualizado.getTelefone());
        restaurante.setTaxaEntrega(restauranteAtualizado.getTaxaEntrega());

        return restauranteRepository.save(restaurante);
    }

     /**
     * Inativar restaurante
     */
    public void inativar(Long id) {
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante " + id + " não encontrado."));

        restaurante.setAtivo(false);
        restauranteRepository.save(restaurante);
    }

    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório!");
        }

        if (restaurante.getTaxaEntrega() != null &&
            restaurante.getTaxaEntrega().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa de entrega não pode ser negativa!");
        }
    }

    public void deletar(Long id) {
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante " + id + " não encontrado."));
        restauranteRepository.delete(restaurante);
    }

}
