package com.deliverytech.delivery.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id", unique = true)
    private Long id;

    private String nome;

    private String email;

    private String telefone;

    private String endereco;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(nullable = true)
    private Boolean ativo;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Pedido> pedidos;

    public void inativar() {
        this.ativo = false;
    }

    public boolean isAtivo() {
        return this.ativo != null && this.ativo;
    }

    
}
