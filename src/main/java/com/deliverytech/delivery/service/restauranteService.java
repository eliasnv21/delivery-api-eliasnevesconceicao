package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.util.List;

import com.deliverytech.delivery.dto.request.RestauranteRequestDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.projection.RelatorioVendas;

public interface restauranteService {

    RestauranteResponseDTO cadastrar(RestauranteRequestDTO dto);

    RestauranteResponseDTO buscarPorId(Long id);

    RestauranteResponseDTO atualizar(Long id, RestauranteRequestDTO dto);

    RestauranteResponseDTO ativarDesativarRestaurante(Long id);

    RestauranteResponseDTO buscarPorNome(String nome);

    List<RestauranteResponseDTO> buscarPorCategoria(String categoria);

    List<RestauranteResponseDTO> buscarPorPreco(BigDecimal precoMinimo, BigDecimal precoMaximo);

    List<RestauranteResponseDTO> listarAtivos();

    List<RestauranteResponseDTO> listarTop5PorNome();

    List<RelatorioVendas> relatorioVendasPorRestaurante();

    List<RestauranteResponseDTO> buscarPorTaxaEntrega(BigDecimal taxaEntrega);

    RestauranteResponseDTO inativarRestaurante(Long id);
}