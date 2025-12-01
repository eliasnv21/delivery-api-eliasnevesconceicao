package com.deliverytech.delivery.service.impl;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;

import com.deliverytech.delivery.dto.request.ProdutoRequestDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.repository.produtoRepository;
import com.deliverytech.delivery.repository.restauranteRepository;
import com.deliverytech.delivery.security.SecurityUtils;
import com.deliverytech.delivery.service.produtoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class produtoServiceImpl implements produtoService {

    @Autowired
    private produtoRepository produtoRepository;

    @Autowired
    private restauranteRepository restauranteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @CacheEvict(value = { "produtos", "produtos-disponiveis" }, allEntries = true)
    public ProdutoResponseDTO cadastrar(ProdutoRequestDTO dto) {
        // Validação de segurança
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = SecurityUtils.getCurrentRestauranteId();
            // Impede que o restaurante 1 cadastre algo para o restaurante 2
            if (!dto.getRestauranteId().equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Você não pode cadastrar produtos para outro restaurante.");
            }
        }
        // Converter DTO para entidade
        Produto produto = modelMapper.map(dto, Produto.class);
        produto.setRestaurante(restauranteRepository.findById(dto.getRestauranteId()).get());
        // Salvar cliente
        Produto produtoSalvo = produtoRepository.save(produto);
        // Retornar DTO de resposta
        return modelMapper.map(produtoSalvo, ProdutoResponseDTO.class);
    }

    @Override
    public ProdutoResponseDTO buscarPorId(Long id) {
        // Buscar produto por ID
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        // Converter entidade para DTO
        return modelMapper.map(produto, ProdutoResponseDTO.class);
    }

    @Override
    @CacheEvict(value = { "produtos", "produtos-disponiveis" }, allEntries = true)
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        // Buscar produto existente
        Produto produtoExistente = produtoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        // Validação de segurança
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = SecurityUtils.getCurrentRestauranteId();
            // Verifica se o produto que está sendo editado pertence ao restaurante logado
            if (!produtoExistente.getRestaurante().getId().equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Este produto não pertence ao seu restaurante.");
            }
        }
        Optional<Restaurante> restaurante = restauranteRepository.findById(dto.getRestauranteId());
        // Validar dados do produto
        if (dto.getNome() == null || dto.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        if (dto.getDescricao() == null || dto.getDescricao().isEmpty()) {
            throw new IllegalArgumentException("Descrição do produto é obrigatória");
        }
        if (dto.getPreco() == null || dto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
        if (dto.getCategoria() == null || dto.getCategoria().isEmpty()) {
            throw new IllegalArgumentException("Categoria do produto é obrigatória");
        }
        // Atualizar dados do produto
        produtoExistente.setNome(dto.getNome());
        produtoExistente.setDescricao(dto.getDescricao());
        produtoExistente.setPreco(dto.getPreco());
        produtoExistente.setCategoria(dto.getCategoria());
        produtoExistente.setDisponivel(dto.getDisponivel());
        produtoExistente.setRestaurante(restaurante.get());
        // Salvar produto atualizado
        Produto produtoAtualizado = produtoRepository.save(produtoExistente);
        // Retornar DTO de resposta
        return modelMapper.map(produtoAtualizado, ProdutoResponseDTO.class);
    }

    @Override
    @CacheEvict(value = { "produtos", "produtos-disponiveis" }, allEntries = true)
    public ProdutoResponseDTO ativarDesativarProduto(Long id) {
        // Buscar produto existente
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        // Validação de segurança
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = SecurityUtils.getCurrentRestauranteId();
            // Verifica se o produto que está sendo editado pertence ao restaurante logado
            if (!produto.getRestaurante().getId().equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Este produto não pertence ao seu restaurante e não pode ser ativado/desativado.");
            }
        }
        // Inverter disponibilidade do produto
        produto.setDisponivel(!produto.getDisponivel());
        // Salvar produto atualizado
        Produto produtoAtualizado = produtoRepository.save(produto);
        // Retornar DTO de resposta
        return modelMapper.map(produtoAtualizado, ProdutoResponseDTO.class);
    }

    @Override
    public ProdutoResponseDTO buscarPorNome(String nome) {
        // Buscar produto por nome
        Produto produto = produtoRepository.findByNome(nome);
        if(!produto.getDisponivel()){
            throw new BusinessException("Produto indisponível: " + nome);
        }
        // Converter entidade para DTO
        return modelMapper.map(produto, ProdutoResponseDTO.class);
    }

    @Override
    public List<ProdutoResponseDTO> buscarPorRestaurante(Long restauranteId) {
        // Buscar produtos por restaurante ID
        List<Produto> produtos = produtoRepository.findByRestauranteId(restauranteId);
        if (produtos.isEmpty() || produtos.stream().noneMatch(Produto::getDisponivel)) {
            throw new BusinessException("Nenhum produto encontrado para o restaurante ID: " + restauranteId);
        }
        // Converter lista de entidades para lista de DTOs
        return produtos.stream().filter(Produto::getDisponivel) .map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class)).toList();
    }

    @Override
    public List<ProdutoResponseDTO> buscarPorCategoria(String categoria) {
        // Buscar produtos por categoria
        List<Produto> produtos = produtoRepository.findByCategoria(categoria);
        if (produtos.isEmpty()) {
            throw new BusinessException("Nenhum produto encontrado para a categoria: " + categoria);
        }
        // Converter lista de entidades para lista de DTOs
        return produtos.stream().map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class)).toList();
    }

    @Override
    public List<ProdutoResponseDTO> buscarPorPreco(BigDecimal precoMinimo, BigDecimal precoMaximo) {
        // Buscar produtos por faixa de preço
        List<Produto> produtos = produtoRepository.findByPrecoLessThanEqual(precoMaximo);
        if (produtos.isEmpty()) {
            throw new BusinessException("Nenhum produto encontrado na faixa de preço: " + precoMinimo + " a " + precoMaximo);
        }
        // Converter lista de entidades para lista de DTOs
        return produtos.stream().filter(produto -> produto.getPreco().compareTo(precoMinimo) >= 0).map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class)).toList();
    }

    @Override
    @Cacheable(value = "produtos")
    public List<ProdutoResponseDTO> buscarTodosProdutos() {
        // Buscar todos os produtos
        List<Produto> produtos = produtoRepository.findAll();
        if (produtos.isEmpty()) {
            throw new BusinessException("Nenhum produto encontrado");
        }
        // Converter lista de entidades para lista de DTOs
        return produtos.stream().map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class)).toList();
    }

    @Override
    @Cacheable(value = "produtos-disponiveis")
    public List<ProdutoResponseDTO> buscarProdutosDisponiveis(boolean disponivel) {
        
        List<Produto> produtos;
        
        // Lógica de busca
        if (disponivel) {
            produtos = produtoRepository.findByDisponivelTrue();
        } else {
            produtos = produtoRepository.findAll().stream()
                    .filter(p -> !p.getDisponivel())
                    .toList();
        }

        if (produtos.isEmpty()) {
             throw new BusinessException("Nenhum produto encontrado com a disponibilidade: " + disponivel);
        }

        return produtos.stream()
                .map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class))
                .toList();
    }
    
    //buscar produtos com preço menor ou igual a um valor específico
    @Override
    public List<ProdutoResponseDTO> buscarPorPrecoMenorOuIgual(BigDecimal valor) {
        // Buscar produtos com preço menor ou igual ao valor especificado
        List<Produto> produtos = produtoRepository.findByPrecoLessThanEqual(valor);
        if (produtos.isEmpty()) {
            throw new BusinessException("Nenhum produto encontrado com preço menor ou igual a: " + valor);
        }
        // Converter lista de entidades para lista de DTOs
        return produtos.stream().map(produto -> modelMapper.map(produto, ProdutoResponseDTO.class)).toList();
    }
}
