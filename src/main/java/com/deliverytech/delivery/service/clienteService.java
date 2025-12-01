package com.deliverytech.delivery.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.deliverytech.delivery.dto.request.ClienteRequestDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;

public interface clienteService {

    ClienteResponseDTO cadastrar(ClienteRequestDTO dto);

    ClienteResponseDTO buscarPorId(Long idLong);

    ClienteResponseDTO buscarPorEmail(String email);

    ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto);

    ClienteResponseDTO ativarDesativarCliente(Long id);

    List<ClienteResponseDTO> listarAtivos();

    List<ClienteResponseDTO> buscarPorNome(String nome);

    Page<ClienteResponseDTO> listarTodosPaginado(Pageable pageable);

}