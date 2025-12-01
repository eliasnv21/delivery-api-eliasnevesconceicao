package com.deliverytech.delivery.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.deliverytech.delivery.dto.request.ItemPedidoRequestDTO;
import com.deliverytech.delivery.dto.request.PedidoRequestDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.exceptions.EntityNotFoundException;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.repository.pedidoRepository;
import com.deliverytech.delivery.repository.produtoRepository;
import com.deliverytech.delivery.repository.restauranteRepository;
import com.deliverytech.delivery.security.SecurityUtils;
import com.deliverytech.delivery.service.pedidoService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class pedidoServiceImpl implements pedidoService {

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private clienteRepository clienteRepository;

    @Autowired
    private restauranteRepository restauranteRepository;

    @Autowired
    private produtoRepository produtoRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        // 1. Validar cliente existe e está ativo
        Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente inativo não pode fazer pedidos");
        }
        // 2. Validar restaurante existe e está ativo
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new BusinessException("Restaurante não encontrado"));
        if (!restaurante.isAtivo()) {
            throw new BusinessException("Restaurante não está disponível");
        }
        // 3. Validar todos os produtos existem e estão disponíveis
        List<ItemPedido> itensPedido = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedidoRequestDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));
            if (!produto.isAtivo()) {
                throw new BusinessException("Produto indisponível: " + produto.getNome());
            }
            if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                throw new BusinessException("Produto não pertence ao restaurante selecionado");
            }
            // Criar item do pedido
            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));

            itensPedido.add(item);
            subtotal = subtotal.add(item.getSubtotal());
        }
        // 4. Calcular total do pedido
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);
        // 5. Salvar pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(dto.getNumeroPedido());
        pedido.setObservacoes(dto.getObservacoes());
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE.name());
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorTotal);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        // 6. Salvar itens do pedido
        for (ItemPedido item : itensPedido) {
            item.setPedido(pedidoSalvo);
        }
        pedidoSalvo.setItens(itensPedido);
        // 7. Atualizar estoque (se aplicável) - Simulação
        // Em um cenário real, aqui seria decrementado o estoque

        // 8. Retornar pedido criado
        return modelMapper.map(pedidoSalvo, PedidoResponseDTO.class);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        // Buscar pedido por ID
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        // Validação de Segurança
        Usuario usuarioLogado = SecurityUtils.getCurrentUser();
        // Se for CLIENTE, só pode ver se o pedido for dele (email bate)
        if (SecurityUtils.isCliente() && !pedido.getCliente().getEmail().equals(usuarioLogado.getEmail())) {
            throw new AccessDeniedException("Você não tem permissão para visualizar este pedido.");
        }
        // Se for RESTAURANTE, só pode ver se o pedido for para ele
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = usuarioLogado.getRestauranteId();
            if (!pedido.getRestaurante().getId().equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Este pedido não pertence ao seu restaurante.");
            }
        }
        // Converter entidade para DTO
        return modelMapper.map(pedido, PedidoResponseDTO.class);
    }

    @Override
    public List<PedidoResponseDTO> listarPedidosPorCliente(Long clienteId) {
        // Validação de segurança
        // Se não for Admin, o ID passado na URL tem que ser o ID do usuário logado (ou vinculado ao email)
        // Como Cliente e Usuario são tabelas diferentes, validamos pelo email do Cliente buscado
        if (!SecurityUtils.isAdmin()) {
             Cliente clienteAlvo = clienteRepository.findById(clienteId).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));  
             String emailLogado = SecurityUtils.getCurrentUserEmail();
             
             if (!clienteAlvo.getEmail().equals(emailLogado)) {
                 throw new AccessDeniedException("Você não pode visualizar os pedidos de outro cliente.");
             }
        }
        // Buscar pedidos por cliente ID
        List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId);
        if (pedidos.isEmpty()) {
            throw new EntityNotFoundException("Nenhum pedido encontrado para o cliente com ID: " + clienteId);
        }
        // Converter lista de entidades para lista de DTOs
        return pedidos.stream().map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class)).toList();
    }

    @Override
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status) {
        // Buscar pedido por ID
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        // Validação de segurança
        // Apenas Admin e o Restaurante dono podem mudar o status
        if (SecurityUtils.isRestaurante()) {
            Long restauranteIdLogado = SecurityUtils.getCurrentUser().getRestauranteId();
            if (!pedido.getRestaurante().getId().equals(restauranteIdLogado)) {
                throw new AccessDeniedException("Acesso negado: Você não pode alterar o status de pedidos de outro restaurante.");
            }
        }
        // Atualizar status do pedido
        isTransicaoValida(status, status);
        if (!isTransicaoValida(StatusPedido.valueOf(pedido.getStatus()), status)) {
            throw new BusinessException("Transição de status inválida para o pedido com ID: " + id);
        }
        pedido.setStatus(status.name());
        // Salvar pedido atualizado
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        // Converter entidade para DTO
        return modelMapper.map(pedidoAtualizado, PedidoResponseDTO.class);
    }

    @Override
    public BigDecimal calcularValorTotalPedido(List<ItemPedidoRequestDTO> itens) {
        // Calcular o valor total do pedido somando os preços dos itens
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemPedidoRequestDTO item : itens) {
            Produto produto = produtoRepository.findById(item.getProdutoId()).orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + item.getProdutoId()));
            valorTotal = valorTotal.add(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        return valorTotal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findByRestauranteIdOrderByDataPedidoDesc(Long restauranteId) {
        // Opcional: Validar se restaurante existe
        if (!restauranteRepository.existsById(restauranteId)) {
            throw new EntityNotFoundException("Restaurante não encontrado com ID: " + restauranteId);
        }

        List<Pedido> pedidos = pedidoRepository.findByRestauranteIdOrderByDataPedidoDesc(restauranteId);
        
        return pedidos.stream().map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findByStatus(StatusPedido status) {
        // Atenção: Se sua entidade Pedido usa String para status, o Repository pode precisar de ajuste
        // ou o Spring Data converte automaticamente se estiver tudo certo.
        List<Pedido> pedidos = pedidoRepository.findByStatus(status.name());

        return pedidos.stream().map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findTop10ByOrderByDataPedidoDesc() {
        List<Pedido> pedidos = pedidoRepository.findTop10ByOrderByDataPedidoDesc();
        
        return pedidos.stream().map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim) {
        List<Pedido> pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim);
        
        return pedidos.stream().map(pedido -> modelMapper.map(pedido, PedidoResponseDTO.class)).collect(Collectors.toList());
    }

    @Override
    public PedidoResponseDTO cancelarPedido(Long id) {
        // Buscar pedido por ID
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        // Validação de segurança
        // Apenas Admin e o Cliente dono do pedido podem cancelar
        if (SecurityUtils.isCliente()) {
            String emailLogado = SecurityUtils.getCurrentUserEmail();
            if (!pedido.getCliente().getEmail().equals(emailLogado)) {
                throw new AccessDeniedException("Acesso negado: Você só pode cancelar pedidos que você fez.");
            }
        }
        // Verificar se o pedido já está cancelado
        if (pedido.getStatus().equals(StatusPedido.CANCELADO.name())) {
            throw new RuntimeException("Pedido já está cancelado: " + id);
        }
        // Atualizar status do pedido para CANCELADO
        podeSerCancelado(StatusPedido.valueOf(pedido.getStatus()));
        if (!podeSerCancelado(StatusPedido.valueOf(pedido.getStatus()))) {  
            throw new BusinessException("Pedido não pode ser cancelado, status atual: " + pedido.getStatus());
        }
        pedido.setStatus(StatusPedido.CANCELADO.name());
        // Salvar pedido atualizado
        pedidoRepository.save(pedido);
        // Converter entidade para DTO
        return modelMapper.map(pedido, PedidoResponseDTO.class);
    }

    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {
        // Implementar lógica de transições válidas
        switch (statusAtual) {
            case PENDENTE:
                return novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO:
                return novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO:
                return novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA:
                return novoStatus == StatusPedido.ENTREGUE;
            default:
                return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }
}
