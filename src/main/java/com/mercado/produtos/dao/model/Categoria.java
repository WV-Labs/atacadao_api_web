package com.mercado.produtos.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "categorias", schema = "schemamercado")
public class Categoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Nome obrigatório!")
  @Size(max=50, message = "Nome deve ter no máximo 50 caracteres!")
  @Column(name = "nome", nullable = false, length = 50)
  private String nome;

  @NotBlank(message = "Descrição obrigatório!")
  @Size(max=100, message = "Nome deve ter no máximo 100 caracteres!")
  @Column(name = "descricao", nullable = false, length = 100)
  private String descricao;

  @NotBlank(message = "Nome exibição obrigatório!")
  @Size(max=50, message = "Nome ter no máximo 50 caracteres!")
  @Column(name = "nome_exibicao", nullable = false, length = 50)
  private String nomeExibicao;

  @JsonIgnore
  @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Terminal> terminals;

  @JsonIgnore
  @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Produto> produtos;

  public Categoria() {
  }

  public Categoria(String nome) {
    new Categoria(nome, "");
  }

  public Categoria(String nome, String descricao) {
    this.nome = nome;
    this.descricao = descricao;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public List<Terminal> getTerminals() {
    return terminals;
  }

  public void setTerminals(List<Terminal> terminals) {
    this.terminals = terminals;
  }

  public String getNomeExibicao() {
    return nomeExibicao;
  }

  public void setNomeExibicao(String nomeExibicao) {
    this.nomeExibicao = nomeExibicao;
  }
}
