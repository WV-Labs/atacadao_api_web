package com.mercado.produtos.dao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos", schema = "schemamercado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Nome obrigatório!")
  @Size(max=50, message = "Nome deve ter no máximo 50 caracteres!")
  @Column(name = "nome", nullable = false, length = 50)
  private String nome;

  @Column(name = "cd_txtimport", nullable = true)
  private Long cdTxtimport;

  @NotBlank(message = "Descrição obrigatório!")
  @Size(max=100, message = "Nome deve ter no máximo 100 caracteres!")
  @Column(name = "descricao", nullable = false, length = 100)
  private String descricao;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Column(name = "preco", nullable = false, precision = 10, scale = 2)
  private BigDecimal  preco;

  @Column(name = "preco_promocao", precision = 10, scale = 2)
  private BigDecimal precoPromocao = BigDecimal.valueOf(0.0);

  @Column(name = "codigo_barras")
  private String codigoBarras = "";

  @Column(name = "estoque")
  private Integer estoque = 0;

  @Column(name = "importado")
  private boolean importado = true;

  @Column(name = "ativo")
  private boolean ativo = true;

  @Convert(converter = UnidadeMedidaConverter.class)
  @Column(name = "unidade_medida")
  private UnidadeMedida unidadeMedida = UnidadeMedida.K;

  @ManyToOne
  @JoinColumn(name = "categoria_id")
  private Categoria categoria;

  private String imagem;

  // Construtores
  public Produto(
          String nome, String descricao, BigDecimal preco) {
    this.nome = nome;
    this.descricao = descricao;
    this.preco = preco;
  }
}
