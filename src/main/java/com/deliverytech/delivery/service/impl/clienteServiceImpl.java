package com.deliverytech.delivery.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverytech.delivery.dto.request.ClienteRequestDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.service.clienteService;

@Service
@Transactional
public class clienteServiceImpl implements clienteService {

    @Autowired
    private clienteRepository clienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        // Verifica se o email já está cadastrado
        if(clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email " + dto.getEmail() + " já cadastrado.");
        }
        // Tranforma de DTO para entidade
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());
        // Salva os dados do cliente
        Cliente saveSalvo = clienteRepository.save(cliente);
        // Retorna o DTO de resposta
        return modelMapper.map(saveSalvo, ClienteResponseDTO.class);
        
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {
        // Buscar cliente por ID
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado: " + id));
        // Converter entidade para DTO
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        // Procura pelo id
        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow(() -> new BusinessException("Cliente com o id " + id + " não encontrado."));
        // Verifica se o email já está cadastrado
        if(clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email " + dto.getEmail() + " já cadastrado.");
        }
        // Verifica se o nome está vazio
        if (dto.getNome() == null || dto.getNome().isEmpty()) {
            throw new BusinessException("Nome não pode ser vazio!");  
        }
        // Verifica se o email está vazio
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new BusinessException("Email não pode ser vazio!");
        }
        // Atualiza os novos dados
        clienteExistente.setNome(dto.getNome());
        clienteExistente.setEmail(dto.getEmail());
        clienteExistente.setTelefone(dto.getTelefone());
        // Salva as atualizações
        Cliente saveAtualizado = clienteRepository.save(clienteExistente);
        //Retorna o DTO de resposta
        return modelMapper.map(saveAtualizado, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO ativarDesativarCliente(Long id) {
        // Buscar cliente existente
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado: " + id));
        // Inverter status de ativo
        clienteExistente.setAtivo(!clienteExistente.getAtivo());
        // Salvar cliente atualizado
        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);
        // Retornar DTO de resposta
        return modelMapper.map(clienteAtualizado, ClienteResponseDTO.class);
    }

    @Override
    public List<ClienteResponseDTO> listarAtivos() {
        // Buscar clientes ativos
        List<Cliente> clientesAtivos = clienteRepository.findByAtivoTrue();
        // Converter lista de entidades para lista de DTOs
        return clientesAtivos.stream().map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class)).toList();
    }

    @Override
    public List<ClienteResponseDTO> buscarPorNome(String nome) {
        // Buscar clientes por nome
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCase(nome);
        // Converter lista de entidades para lista de DTOs
        return clientes.stream().map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class)).toList();
    }

    @Override
    public ClienteResponseDTO buscarPorEmail(String email) {
        // Buscar cliente por email
        Cliente cliente = clienteRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Cliente não encontrado com email: " + email));
        // Converter entidade para DTO
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }


}
