package com.mercado.produtos.dao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "terminais", schema = "schemamercado")
public class Terminal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Nome obrigatório!")
  @Size(max=50, message = "Nome deve ter no máximo 50 caracteres!")
  @Column(name = "nome", nullable = false, length = 50)
  private String nome;

  @NotBlank(message = "Localização obrigatório!")
  @Size(max=100, message = "Localização deve ter no máximo 100 caracteres!")
  @Column(name = "localizacao", nullable = false, length = 100)
  private String localizacao;

  @ManyToOne
  @JoinColumn(name = "categoria_id")
  @NotNull
  private Categoria categoria;

  @NotNull
  @Column(name = "nr_terminal")
  private Integer nrTerminal;

  private String url;

  private boolean ativo = true;

  // Construtores

  public Terminal() {}

  public Terminal(String nome, String localizacao, Categoria categoria, Integer nrTerminal) {
    this.nome = nome;
    this.localizacao = localizacao;
    this.categoria = categoria;
    this.nrTerminal = nrTerminal;
    this.url = gerarUrl();
  }

  public String gerarUrl() {
    if (categoria != null && nrTerminal != null) {
      return "/" + categoria.getNome().toLowerCase().replaceAll(" ", "") + "/" + nrTerminal;
    }
    return null;
  }

  public void setCategoria(Categoria categoria) {
    this.categoria = categoria;
    this.url = gerarUrl();
  }

  public void setNrTerminal(Integer nrTerminal) {
    this.nrTerminal = nrTerminal;
    this.url = gerarUrl();
  }

  public Categoria getCategoria() {
    return categoria;
  }

  public Integer getNrTerminal() {
    return nrTerminal;
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

  public String getLocalizacao() {
    return localizacao;
  }

  public void setLocalizacao(String localizacao) {
    this.localizacao = localizacao;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }
}
