package com.mercado.produtos.dao.dto;

import java.math.BigDecimal;

public class ProdutoDto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private BigDecimal precoPromocao = BigDecimal.valueOf(0.0);
    private int quantidade_estoque;
    private boolean disponivel;

    public boolean isConteudo() {
        return conteudo;
    }

    public void setConteudo(boolean conteudo) {
        this.conteudo = conteudo;
    }

    private boolean conteudo;
    private String categoria;
    private String imagem;
    private String descricaoCategoria;
    private String abreviacaoUM;
    private int tipoConteudo;
    private String caminhoImagemVideo;

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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public BigDecimal getPrecoPromocao() {
        return precoPromocao;
    }

    public void setPrecoPromocao(BigDecimal precoPromocao) {
        this.precoPromocao = precoPromocao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public int getQuantidade_estoque() {
        return quantidade_estoque;
    }

    public void setQuantidade_estoque(int quantidade_estoque) {
        this.quantidade_estoque = quantidade_estoque;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getDescricaoCategoria() {
        return descricaoCategoria;
    }

    public void setDescricaoCategoria(String descricaoCategoria) {
        this.descricaoCategoria = descricaoCategoria;
    }

    public String getAbreviacaoUM() {
        return abreviacaoUM;
    }

    public void setAbreviacaoUM(String abreviacaoUM) {
        this.abreviacaoUM = abreviacaoUM;
    }

    public int getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(int tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    public String getCaminhoImagemVideo() {
        return caminhoImagemVideo;
    }

    public void setCaminhoImagemVideo(String caminhoImagemVideo) {
        this.caminhoImagemVideo = caminhoImagemVideo;
    }
}
