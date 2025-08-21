package com.mercado.produtos.dao.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "conteudos_tabela_preco", schema = "schemamercado")
@Data
@AllArgsConstructor
public class ConteudoTabelaPreco {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "conteudo_id")
  @NotNull
  private Conteudo conteudo;

  @ManyToOne
  @JoinColumn(name = "produto_id")
  @NotNull
  private Produto produto;

  // Construtores
  public ConteudoTabelaPreco() {}

}
