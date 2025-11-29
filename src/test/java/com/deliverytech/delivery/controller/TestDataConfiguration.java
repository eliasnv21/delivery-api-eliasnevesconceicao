package com.deliverytech.delivery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.repository.clienteRepository;
import com.deliverytech.delivery.repository.produtoRepository;

import java.math.BigDecimal;

@TestConfiguration
public class TestDataConfiguration {

    @Autowired
    private clienteRepository clienteRepository;

    @Autowired
    private produtoRepository produtoRepository;

    @BeforeTestMethod
    public void setupTestData() {
        // Limpar dados existentes
        clienteRepository.deleteAll();
        produtoRepository.deleteAll();

        // Criar dados de teste
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao.teste@email.com");
        cliente.setTelefone("11999999999");
        clienteRepository.save(cliente);

        Produto produto = new Produto();
        produto.setNome("Pizza Teste");
        produto.setDescricao("Pizza para testes");
        produto.setPreco(BigDecimal.valueOf(29.90));
        produtoRepository.save(produto);
    }
}