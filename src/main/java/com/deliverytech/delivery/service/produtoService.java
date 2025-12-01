package com.deliverytech.delivery.service;

import java.math.BigDecimal;
import java.util.List;

import com.deliverytech.delivery.dto.request.ProdutoRequestDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;

public interface produtoService {

    ProdutoResponseDTO cadastrar(ProdutoRequestDTO dto);

    ProdutoResponseDTO buscarPorId(Long id);

    ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto);

    ProdutoResponseDTO ativarDesativarProduto(Long id);

    ProdutoResponseDTO buscarPorNome(String nome);

    List<ProdutoResponseDTO>  buscarPorRestaurante(Long restauranteId);

    List<ProdutoResponseDTO> buscarPorCategoria(String categoria);

    List<ProdutoResponseDTO> buscarPorPreco(BigDecimal precoMinimo, BigDecimal precoMaximo);

    List<ProdutoResponseDTO> buscarTodosProdutos();

    List<ProdutoResponseDTO> buscarProdutosDisponiveis(boolean disponivel);

    List<ProdutoResponseDTO> buscarPorPrecoMenorOuIgual(BigDecimal valor);
}
