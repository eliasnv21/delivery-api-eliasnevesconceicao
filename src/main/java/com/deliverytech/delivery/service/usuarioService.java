package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.LoginRequestDTO;
import com.deliverytech.delivery.dto.request.RegisterRequestDTO;
import com.deliverytech.delivery.dto.response.LoginResponseDTO;
import com.deliverytech.delivery.dto.response.UsuarioResponseDTO;

public interface usuarioService {

    UsuarioResponseDTO cadastrar(RegisterRequestDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);

}
