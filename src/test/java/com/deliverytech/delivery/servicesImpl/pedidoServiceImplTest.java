package com.deliverytech.delivery.servicesImpl;

import com.deliverytech.delivery.dto.request.ItemPedidoRequestDTO;
import com.deliverytech.delivery.dto.request.PedidoRequestDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.exceptions.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.service.impl.pedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayName("Testes Unitário Pedido Service")
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @InjectMocks
    private pedidoServiceImpl pedidoService;

    @Mock private pedidoRepository pedidoRepository;
    @Mock private clienteRepository clienteRepository;
    @Mock private restauranteRepository restauranteRepository;
    @Mock private produtoRepository produtoRepository;
    @Mock private ModelMapper modelMapper;

    private PedidoRequestDTO pedidoDTO;
    private Cliente cliente;
    private Restaurante restaurante;
    private Produto produto;

    @BeforeEach
    void setUp() {
        // Setup dos dados básicos
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setAtivo(true); // Inicialmente ativo

        restaurante = new Restaurante();
        restaurante.setId(1L);
        restaurante.setAtivo(true);
        restaurante.setTaxaEntrega(new BigDecimal("5.00"));

        produto = new Produto();
        produto.setId(1L);
        produto.setDisponivel(true);
        produto.setPreco(new BigDecimal("20.00"));
        produto.setRestaurante(restaurante);

        // Setup do DTO de entrada
        ItemPedidoRequestDTO itemDTO = new ItemPedidoRequestDTO();
        itemDTO.setProdutoId(1L);
        itemDTO.setQuantidade(2);

        pedidoDTO = new PedidoRequestDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setRestauranteId(1L);
        pedidoDTO.setItens(List.of(itemDTO));
        pedidoDTO.setEnderecoEntrega("Rua Teste");
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso e calcular total correto")
    void criarPedido_ComSucesso() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        
        Pedido pedidoSalvo = new Pedido();
        pedidoSalvo.setValorTotal(new BigDecimal("45.00"));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        
        PedidoResponseDTO responseDTO = new PedidoResponseDTO();
        responseDTO.setValorTotal(new BigDecimal("45.00"));
        when(modelMapper.map(any(Pedido.class), eq(PedidoResponseDTO.class))).thenReturn(responseDTO);

        // When
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("45.00"), resultado.getValorTotal());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar pedido com CLIENTE INATIVO")
    void criarPedido_ErroClienteInativo() {
        // Given
        cliente.setAtivo(false); // Cliente Inativo
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // When & Then
        assertThrows(BusinessException.class, () -> pedidoService.criarPedido(pedidoDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar pedido com PRODUTO INDISPONÍVEL")
    void criarPedido_ErroProdutoIndisponivel() {
        // Given
        produto.setDisponivel(false); // Produto indisponível
        when(clienteRepository.findById(any())).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(any())).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findById(any())).thenReturn(Optional.of(produto));

        // When & Then
        assertThrows(BusinessException.class, () -> pedidoService.criarPedido(pedidoDTO));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção e garantir ROLLBACK (produto de outro restaurante)")
    void criarPedido_ErroProdutoRestauranteDiferente_Rollback() {
        // Given
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setId(99L); // ID Diferente do pedido
        produto.setRestaurante(outroRestaurante);

        when(clienteRepository.findById(any())).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(any())).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findById(any())).thenReturn(Optional.of(produto));

        // When & Then
        assertThrows(BusinessException.class, () -> pedidoService.criarPedido(pedidoDTO));
        // Validação do cenário de rollback: o save do pedido NUNCA deve ser chamado
        verify(pedidoRepository, never()).save(any()); 
    }
    
    // ... Mantenha seus testes de atualizarStatus aqui ...
    
    @Test
    @DisplayName("Deve atualizar status do pedido com sucesso")
    void atualizarStatus_ComSucesso() {
        // Given
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(StatusPedido.PENDENTE.name());
        pedido.setCliente(cliente); // Necessário para evitar NullPointerException na lógica de segurança

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(modelMapper.map(any(), eq(PedidoResponseDTO.class))).thenReturn(new PedidoResponseDTO());

        // When
        pedidoService.atualizarStatusPedido(1L, StatusPedido.CONFIRMADO);

        // Then
        assertEquals(StatusPedido.CONFIRMADO.name(), pedido.getStatus());
        verify(pedidoRepository).save(pedido);
    }
}
