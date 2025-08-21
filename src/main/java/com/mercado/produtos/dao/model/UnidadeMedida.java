package com.mercado.produtos.dao.model;


public enum UnidadeMedida {
  T("T", "TONELADA", "T"),
  K("K", "QUILOGRAMA", "Kg"),
  G("G", "GRAMA", "Gr"),
  L("L", "LITRO", "L"),
  M("M", "MILILITRO", "L"),
  U("U", "QUILOMETRO", "Km"),
  E("E", "METRO", "M"),
  C("C", "CENTIMETRO", "Cm"),
  I("I", "MILIMETRO", "Mm"),
  N("N", "UNITARIO", "Un"),
  P("P", "PACOTE", "Pct"),
  X("X", "SemCadastro", " ");

  private final String codigo;
  private final String descricao;
  private final String abreviacao;

  UnidadeMedida(String codigo, String descricao, String abreviacao) {
    this.codigo = codigo;
    this.descricao = descricao;
    this.abreviacao = abreviacao;
  }

  public static UnidadeMedida fromCodigo(String codigo) {
    for (UnidadeMedida unidade : values()) {
      if (unidade.codigo.equals(codigo)) {
        return unidade;
      }
    }
    throw new IllegalArgumentException("Código inválido para UnidadeMedida: " + codigo);
  }

  public static UnidadeMedida fromAbreviacao(String abreviacao) {
    for (UnidadeMedida unidade : values()) {
      if (unidade.abreviacao.equals(abreviacao)) {
        return unidade;
      }
    }
    throw new IllegalArgumentException("Abreviação inválido para UnidadeMedida: " + abreviacao);
  }

  public String getCodigo() {
    return codigo;
  }

  public String getDescricao() {
    return descricao;
  }

  public String getAbreviacao() {return abreviacao;  }

  @Override
  public String toString() {
    return "UnidadeMedida{" +
            "codigo='" + codigo + '\'' +
            ", descricao='" + descricao + '\'' +
            '}';
  }
}
