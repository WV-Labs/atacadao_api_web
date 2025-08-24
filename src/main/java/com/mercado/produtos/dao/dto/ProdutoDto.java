package com.mercado.produtos.dao.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoDto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private BigDecimal precoPromocao = BigDecimal.valueOf(0.0);
    private int quantidade_estoque;
    private boolean disponivel;
    private boolean conteudo;
    private String categoria;
    private String imagem;
    private String descricaoCategoria;
    private String abreviacaoUM;
    private int tipoConteudo;
    private String caminhoImagemVideo;
    private Long idAgendamentoPrioritario;
}
