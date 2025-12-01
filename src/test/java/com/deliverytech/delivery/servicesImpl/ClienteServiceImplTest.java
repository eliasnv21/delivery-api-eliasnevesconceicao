package com.deliverytech.delivery.servicesImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.mockito.MockedStatic;


import com.deliverytech.delivery.security.SecurityUtils; 
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.dto.request.ClienteRequestDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.exceptions.EntityNotFoundException;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.service.impl.clienteServiceImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("Testes Unitário Cliente Service")
@ExtendWith(MockitoExtension.class)
class clienteServiceImplTest {

    @Mock
    private clienteRepository clienteRepository;

    @InjectMocks
    private clienteServiceImpl clienteService;

    @Mock
    private ModelMapper modelMapper;

    private Cliente cliente;
    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();

        // Inicializando o DTO de requisição
        clienteRequestDTO = new ClienteRequestDTO();
        cliente.setId(1L);
        clienteRequestDTO.setNome("Maria Silva");
        clienteRequestDTO.setEmail("maria@email.com");
        clienteRequestDTO.setTelefone("11999999999");
        clienteRequestDTO.setEndereco("Rua Exemplo, 123, São Paulo, SP");
    }

    @Test
    @DisplayName("Deve salvar cliente com dados válidos")
    void cadastrar() {
        //Given
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        // No teste ClienteServiceImplTest.java
        when(modelMapper.map(any(ClienteRequestDTO.class), eq(Cliente.class)))
                .thenReturn(new Cliente());
        // When
        clienteResponseDTO = clienteService.cadastrar(clienteRequestDTO);

        // Then
        assertNotNull(clienteRequestDTO);
        assertEquals("Maria Silva", clienteRequestDTO.getNome());
        assertEquals("maria@email.com", clienteRequestDTO.getEmail());
        verify(clienteRepository).save(any(Cliente.class));
        verify(clienteRepository).existsByEmail(eq(clienteRequestDTO.getEmail()));
    }

    @Test
    @DisplayName("Deve atualizar cliente com dados válidos")
    void atualizar() {
        // Given
        Long clienteId = 1L;
        cliente.setId(clienteId);
        cliente.setAtivo(true);
        cliente.setEmail("maria@maria.com"); // Email do Cliente no Mock

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            // Simula que a Maria está logada e que ela é a dona do dado que está sendo atualizado
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn("maria@maria.com");
            utilities.when(SecurityUtils::isAdmin).thenReturn(false);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
            when(clienteRepository.existsByEmail(anyString())).thenReturn(false); // Permite atualização de email

            when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                    .thenReturn(new ClienteResponseDTO() {{
                        setNome("Maria Silva Atualizado");
                        setEmail("maria@maria.com");
                        setAtivo(true);
                    }});
            
            // When
            ClienteResponseDTO responseDTO = clienteService.atualizar(clienteId, clienteRequestDTO);

            // Then
            assertNotNull(responseDTO);
            assertEquals("Maria Silva Atualizado", responseDTO.getNome());
            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).save(any(Cliente.class));
        }

    }

    @Test
    @DisplayName("Deve ativar/desativar cliente")
    void ativarDesativarCliente() {
        // Given
        Long clienteId = 1L;
        cliente.setId(clienteId);
        cliente.setAtivo(true);
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@maria.com"); // Email do Cliente no Mock

        // Setup do usuário logado para o Mock
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn("maria@maria.com");
            utilities.when(SecurityUtils::isAdmin).thenReturn(false);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

            when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                    .thenReturn(new ClienteResponseDTO() {{
                        // ... outros setAtivo ...
                        setAtivo(false); // Inverte o status de ativo
                    }});
            
            // When
            ClienteResponseDTO responseDTO = clienteService.ativarDesativarCliente(clienteId);
            
            // Then
            assertNotNull(responseDTO);
            assertFalse(responseDTO.getAtivo()); // Verifica se o status foi invertido
            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).save(any(Cliente.class));
        }
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void buscarPorId() {
        // Given
        Long clienteId = 1L;
        cliente.setId(clienteId);
        cliente.setEmail("maria@maria.com");

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            // Simula que o usuário logado está buscando o próprio ID
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn("maria@maria.com");
            utilities.when(SecurityUtils::isAdmin).thenReturn(false);

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                    .thenReturn(new ClienteResponseDTO() {{
                        setEmail("maria@maria.com");
                        setAtivo(true);
                    }});
            
            // When
            ClienteResponseDTO responseDTO = clienteService.buscarPorId(clienteId);
            
            // Then
            assertNotNull(responseDTO);
            verify(clienteRepository).findById(clienteId);
        }

    }

    @Test
    @DisplayName("Deve buscar cliente por email")
    void buscarPorEmail() {
        // Given
        String email = "maria@maria.com";
        cliente.setEmail(email);

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            // Simula que o usuário logado está buscando o próprio email
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(email);
            utilities.when(SecurityUtils::isAdmin).thenReturn(false);

            when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
            when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                    .thenReturn(new ClienteResponseDTO() {{
                        setEmail(email);
                        setAtivo(true);
                    }});
            
            // When
            ClienteResponseDTO responseDTO = clienteService.buscarPorEmail(email);
            
            // Then
            assertNotNull(responseDTO);
            assertEquals(email, responseDTO.getEmail());
            verify(clienteRepository).findByEmail(email);
        }

    }

    @Test
    @DisplayName("Deve listar todos os clientes ativos")
    void listarAtivos() {
        // Given
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@maria.com");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua Exemplo, 123, São Paulo, SP");

        when(clienteRepository.findByAtivoTrue()).thenReturn(List.of(cliente));
        when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                .thenReturn(new ClienteResponseDTO() {{
                    setNome("Maria Silva");
                    setEmail("maria@maria.com");
                    setTelefone("11999999999");
                    setEndereco("Rua Exemplo, 123, São Paulo, SP");
                    setAtivo(true);
                }});
        // When
        var responseList = clienteService.listarAtivos();
        // Then
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals("Maria Silva", responseList.get(0).getNome());
        assertEquals("maria@maria.com", responseList.get(0).getEmail());
        assertTrue(responseList.get(0).getAtivo());
        verify(clienteRepository).findByAtivoTrue();
        verify(modelMapper).map(any(Cliente.class), eq(ClienteResponseDTO.class));

    }


    @Test
    @DisplayName("Deve listar clientes por nome")
    void buscarPorNome() {
    // Given
        String nome = "Maria";
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@maria.com");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua Exemplo, 123, São Paulo, SP");

        when(clienteRepository.findByNomeContainingIgnoreCase(nome)).thenReturn(List.of(cliente));
        when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class)))
                .thenReturn(new ClienteResponseDTO() {{
                    setNome("Maria Silva");
                    setEmail("maria@maria.com");
                    setTelefone("11999999999");
                    setEndereco("Rua Exemplo, 123, São Paulo, SP");
                    setAtivo(true);
                }});
        // When
        List<ClienteResponseDTO> responseList = clienteService.buscarPorNome(nome);
        // Then
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals("Maria Silva", responseList.get(0).getNome());
        assertEquals("maria@maria.com", responseList.get(0).getEmail());
        assertTrue(responseList.get(0).getAtivo());
        verify(clienteRepository).findByNomeContainingIgnoreCase(nome);
        verify(modelMapper).map(any(Cliente.class), eq(ClienteResponseDTO.class));

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar email duplicado")
    void cadastrar_ErroEmailDuplicado() {
        // Given
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true); // Simula que já existe
        // When & Then
        assertThrows(BusinessException.class, () -> clienteService.cadastrar(clienteRequestDTO));
        // Verifica que NUNCA chamou o save()
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ID inexistente")
    void buscarPorId_NaoEncontrado() {
        // Given
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        // When & Then
        assertThrows(BusinessException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve listar clientes com paginação")
    void listarTodosPaginado() {
        // Given
        Pageable pageable = PageRequest.of(0, 10); // Cria um objeto Pageable
        Page<Cliente> paginaCliente = new PageImpl<>(List.of(cliente), pageable, 1);
        
        when(clienteRepository.findAll(pageable)).thenReturn(paginaCliente);
        
        // When
        Page<ClienteResponseDTO> resultado = clienteService.listarTodosPaginado(pageable);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getContent().size());
        verify(clienteRepository).findAll(pageable);
    }
}