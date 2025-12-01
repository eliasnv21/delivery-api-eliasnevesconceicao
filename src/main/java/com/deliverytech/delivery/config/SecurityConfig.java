package com.deliverytech.delivery.config;

import com.deliverytech.delivery.security.JwtAuthenticationFilter;
import com.deliverytech.delivery.security.CustomAccessDeniedHandler;
import com.deliverytech.delivery.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/h2-console/**", "/index.html", "/static/**", "/error/**", "/health" ).permitAll()
                        // ENDPOINTS CLIENTE
                        .requestMatchers(HttpMethod.GET, "/clientes").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clientes/email/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clientes/buscar").hasAuthority( "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clientes/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/clientes/{id}/status").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/clientes").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/clientes/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        // ENDPOINTS PEDIDO
                        .requestMatchers(HttpMethod.DELETE, "/pedidos/..").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/status/{status}").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/restaurante/**").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/recentes").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/periodo").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/cliente/**").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/{id}").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pedidos").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pedidos/calcular").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/{pedidoId}/**").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        // ENDPOINTS PRODUTO
                        .requestMatchers(HttpMethod.GET, "/produtos").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/restaurante/**").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/preco").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/preco/***").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/nome/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/categoria/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/disponiveis").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/produtos/{id}").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/produtos/{id}/ativar-desativar").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/produtos").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/produtos/**").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        // ENDPOINTS RESTAURANTE
                        .requestMatchers(HttpMethod.GET, "/restaurantes/").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/top-cinco").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/taxa-entrega").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/relatorio-vendas").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/preco/{precoMinimo}/{precoMaximo}").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/nome/{nome}").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/categoria/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/restaurantes/**").hasAnyAuthority("CLIENTE", "RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/restaurantes/{id}/ativar-desativar").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/restaurantes").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/restaurantes/**").hasAnyAuthority("RESTAURANTE", "ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(customAuthEntryPoint) // 401 Unauthorized
                    .accessDeniedHandler(customAccessDeniedHandler) // 403 Forbidden
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
