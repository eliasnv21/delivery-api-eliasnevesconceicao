package com.deliverytech.delivery.service.impl;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.deliverytech.delivery.dto.request.RestauranteRequestDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.exceptions.EntityNotFoundException;
import com.deliverytech.delivery.projection.RelatorioVendas;
import com.deliverytech.delivery.repository.restauranteRepository;
import com.deliverytech.delivery.security.SecurityUtils;
import com.deliverytech.delivery.service.restauranteService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class restauranteServiceImpl implements restauranteService {

    @Autowired
    private restauranteRepository restauranteRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public RestauranteResponseDTO cadastrar(RestauranteRequestDTO dto) {
        // Validar nome único
        if (restauranteRepository.findByNome(dto.getNome()).isPresent()) {
            throw new BusinessException("Restaurante já cadastrado: " + dto.getNome());
        }
        // Converter DTO para entidade
        Restaurante restaurante = modelMapper.map(dto, Restaurante.class);
        // Salvar cliente
        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);
        // Retornar DTO de resposta
        return modelMapper.map(restauranteSalvo, RestauranteResponseDTO.class);
    }

    @Override
    public RestauranteResponseDTO buscarPorId(Long id) {
        // Buscar restaurante por ID
        Restaurante restaurante = restauranteRepository.findById(id).orElseThrow(() -> new BusinessException("Restaurante não encontrado com ID: " + id));
        // Converter entidade para DTO
        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }

    @Override
    public RestauranteResponseDTO atualizar(Long id, RestauranteRequestDTO dto) {
        // Buscar restaurante existente
        Restaurante restauranteExistente = restauranteRepository.findById(id).orElseThrow(() -> new BusinessException("Restaurante não encontrado com ID: " + id));

        // Validação de segurança
        // Se quem está logado for um RESTAURANTE, ele só pode alterar se o ID for o dele mesmo.
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = SecurityUtils.getCurrentRestauranteId();

            if (!id.equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Acesso negado: Você só pode alterar os dados do seu próprio restaurante.");
            }
        }

        // Atualizar campos do restaurante
        restauranteExistente.setNome(dto.getNome());
        restauranteExistente.setCategoria(dto.getCategoria());
        restauranteExistente.setTelefone(dto.getTelefone());
        restauranteExistente.setAvaliacao(dto.getAvaliacao());
        restauranteExistente.setEndereco(dto.getEndereco());
        restauranteExistente.setTelefone(dto.getTelefone());

        // Salvar as alterações
        Restaurante restauranteAtualizado = restauranteRepository.save(restauranteExistente);

        // Retornar DTO atualizado
        return modelMapper.map(restauranteAtualizado, RestauranteResponseDTO.class);
    }

    @Override
    public RestauranteResponseDTO ativarDesativarRestaurante(Long id) {
        // Buscar restaurante por ID
        Restaurante restaurante = restauranteRepository.findById(id).orElseThrow(() -> new BusinessException("Restaurante não encontrado com ID: " + id));
        // Alternar status de ativo/desativado
        restaurante.setAtivo(!restaurante.getAtivo());
        // Salvar as alterações
        Restaurante restauranteAtualizado = restauranteRepository.save(restaurante);
        // Retornar DTO atualizado
        return modelMapper.map(restauranteAtualizado, RestauranteResponseDTO.class);
    }

    @Override
    public RestauranteResponseDTO buscarPorNome(String nome) {
        // Buscar restaurante por nome
        Restaurante restaurante = restauranteRepository.findByNomeAndAtivoTrue(nome);
        if (restaurante == null) {
        throw new EntityNotFoundException("Restaurante não encontrado ou inativo: " + nome);
        }

        return modelMapper.map(restaurante, RestauranteResponseDTO.class);
    }

    @Override
    public List<RestauranteResponseDTO> buscarPorCategoria(String categoria) {
        // Buscar restaurantes por categoria
        List<Restaurante> restaurantes = restauranteRepository.findByCategoria(categoria);
        if (restaurantes.isEmpty()) {
            throw new BusinessException("Nenhum restaurante encontrado na categoria: " + categoria);
        }
        // Converter lista de entidades para lista de DTOs
        return restaurantes.stream().map(restaurante -> modelMapper.map(restaurante, RestauranteResponseDTO.class)).toList();
    }

    @Override
    public List<RestauranteResponseDTO> buscarPorPreco(BigDecimal precoMinimo, BigDecimal precoMaximo) {
        // Buscar restaurantes por taxa de entrega dentro do intervalo
        List<Restaurante> restaurantes = restauranteRepository.findByTaxaEntregaBetween(precoMinimo, precoMaximo);
        if (restaurantes.isEmpty()) {
            throw new BusinessException("Nenhum restaurante encontrado com taxa de entrega entre " + precoMinimo + " e " + precoMaximo);
        }
        // Converter lista de entidades para lista de DTOs
        return restaurantes.stream().map(restaurante -> modelMapper.map(restaurante, RestauranteResponseDTO.class)).toList();
    }

    @Override
    public List<RestauranteResponseDTO> listarAtivos() {
        // Buscar todos os restaurantes ativos
        List<Restaurante> restaurantesAtivos = restauranteRepository.findByAtivoTrue();
        if (restaurantesAtivos.isEmpty()) {
            throw new BusinessException("Nenhum restaurante ativo encontrado.");
        }
        // Converter lista de entidades para lista de DTOs
        return restaurantesAtivos.stream().map(restaurante -> modelMapper.map(restaurante, RestauranteResponseDTO.class)).toList();
    }

    @Override
    public List<RestauranteResponseDTO> listarTop5PorNome() {
        // Buscar os 5 primeiros restaurantes por nome
        List<Restaurante> top5Restaurantes = restauranteRepository.findTop5ByOrderByNomeAsc();
        if (top5Restaurantes.isEmpty()) {
            throw new BusinessException("Nenhum restaurante encontrado.");
        }
        // Converter lista de entidades para lista de DTOs
        return top5Restaurantes.stream().map(restaurante -> modelMapper.map(restaurante, RestauranteResponseDTO.class)).toList();
    }

    @Override
    public List<RelatorioVendas> relatorioVendasPorRestaurante() {
        // Buscar relatório de vendas por restaurante
        List<RelatorioVendas> relatorio = restauranteRepository.relatorioVendasPorRestaurante();
        if (relatorio.isEmpty()) {
            throw new BusinessException("Nenhum dado de vendas encontrado.");
        }
        // Converter lista de entidades para lista de DTOs
        return relatorio.stream().map(restaurante -> modelMapper.map(restaurante, RelatorioVendas.class)).toList();
    }

    @Override
    public List<RestauranteResponseDTO> buscarPorTaxaEntrega(BigDecimal taxaEntrega) {
        // Buscar restaurantes por taxa de entrega
        List<Restaurante> restaurantes = restauranteRepository.findByTaxaEntregaLessThanEqual(taxaEntrega);
        if (restaurantes.isEmpty()) {
            throw new BusinessException("Nenhum restaurante encontrado com taxa de entrega menor ou igual a: " + taxaEntrega);
        }
        // Converter lista de entidades para lista de DTOs
        return restaurantes.stream().map(restaurante -> modelMapper.map(restaurante, RestauranteResponseDTO.class)).toList();
    }
    @Override
    public RestauranteResponseDTO inativarRestaurante(Long id) {
        // Buscar restaurante por ID
        Restaurante restaurante = restauranteRepository.findById(id).orElseThrow(() -> new BusinessException("Restaurante não encontrado com ID: " + id));
        // Verificação de segurança
        if (SecurityUtils.isRestaurante()) {
            Long idLogado = SecurityUtils.getCurrentRestauranteId();
            if (!id.equals(idLogado)) {
                throw new AccessDeniedException("Você só pode inativar o seu próprio restaurante.");
        }
    }
        // Verificar se o restaurante já está inativo
        if (!restaurante.getAtivo()) {
            throw new BusinessException("Restaurante já está inativo: " + restaurante.getNome());
        }
        // Inativar o restaurante
        restaurante.setAtivo(false);
        // Salvar as alterações
        Restaurante restauranteInativado = restauranteRepository.save(restaurante);
        // Retornar DTO atualizado
        return modelMapper.map(restauranteInativado, RestauranteResponseDTO.class);
    }

}