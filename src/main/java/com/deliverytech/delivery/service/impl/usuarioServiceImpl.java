package com.deliverytech.delivery.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.deliverytech.delivery.dto.request.LoginRequestDTO;
import com.deliverytech.delivery.dto.request.RegisterRequestDTO;
import com.deliverytech.delivery.dto.response.LoginResponseDTO;
import com.deliverytech.delivery.dto.response.UsuarioResponseDTO;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.exceptions.BusinessException;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.repository.restauranteRepository;
import com.deliverytech.delivery.repository.usuarioRepository;
import com.deliverytech.delivery.security.JwtUtil;
import com.deliverytech.delivery.service.usuarioService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class usuarioServiceImpl implements usuarioService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private usuarioRepository usuarioRepository;

    @Autowired
    private clienteRepository clienteRepository;

    @Autowired
    restauranteRepository restauranteRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public UsuarioResponseDTO cadastrar(RegisterRequestDTO dto) {
        if(usuarioRepository.existsByEmail(dto.getEmail())){
            throw new BusinessException("Email já cadastrado: " + dto.getEmail());
        }
        Usuario usuario = Usuario.builder()
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .nome(dto.getNome())
                .role(dto.getRole() != null ? dto.getRole() : Role.CLIENTE)
                .dataCriacao(LocalDateTime.now())
                .ativo(true)
                .restauranteId(dto.getRestauranteId())
                .build();
        usuarioRepository.save(usuario);

        return modelMapper.map(usuario, UsuarioResponseDTO.class);

    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException("Usuário inválido", String.valueOf(HttpStatus.UNAUTHORIZED)));

        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setUsuario(modelMapper.map(usuario, UsuarioResponseDTO.class));
        responseDTO.setTipo("Bearer");
        responseDTO.setExpiracao(86400000L); // 1 dia em segundos
        responseDTO.setToken(jwtUtil.generateToken(User.withUsername(usuario.getEmail()).password(usuario.getSenha()).authorities("ROLE_" + usuario.getRole().name()).build(), usuario));

        return responseDTO;

    }
}