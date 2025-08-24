package com.mercado.produtos.dao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "conteudos", schema = "schemamercado")
@Data
@AllArgsConstructor
public class Conteudo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "tipo_conteudo")
  private int tipoConteudo = 3;

  @NotBlank(message = "Título obrigatório!")
  @Size(max=100, message = "Título deve ter no máximo 100 caracteres!")
  @Column(name = "titulo", nullable = false, length = 100)
  private String titulo;

  @NotBlank(message = "Descrição obrigatório!")
  @Size(max=100, message = "Descrição deve ter no máximo 100 caracteres!")
  @Column(name = "descricao", nullable = false, length = 100)
  private String descricao;

  @Column(name = "nome_midia")
  private String nomeMidia;

  @Transient
  private Long idAgendamentoPrioritario;

  // Construtores
  public Conteudo() {}

}
