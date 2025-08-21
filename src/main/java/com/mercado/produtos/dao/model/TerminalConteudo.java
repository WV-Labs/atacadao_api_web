package com.mercado.produtos.dao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "terminal_conteudo", schema = "schemamercado")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TerminalConteudo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "terminal_id")
  @NotNull
  private Terminal terminal;

  @ManyToOne
  @JoinColumn(name = "conteudo_id")
  @NotNull
  private Conteudo conteudo;

}
