package com.mercado.produtos.dao.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento_logs", schema = "schemamercado")
public class AgendamentoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

    @Column(name = "data_execucao")
    private LocalDateTime dataExecucao = LocalDateTime.now();

    @Column(name = "status_execucao")
    private String statusExecucao; // SUCCESS, ERROR, TIMEOUT

    @Column(name = "url_chamada", length = 500)
    private String urlChamada;

    @Column(name = "resposta_http")
    private Integer respostaHttp;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "tempo_execucao_ms")
    private Long tempoExecucaoMs;

    // Construtores, getters e setters
    public AgendamentoLog() {}

    public AgendamentoLog(Agendamento agendamento, String url) {
        this.agendamento = agendamento;
        this.urlChamada = url;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }

    public LocalDateTime getDataExecucao() { return dataExecucao; }
    public void setDataExecucao(LocalDateTime dataExecucao) { this.dataExecucao = dataExecucao; }

    public String getStatusExecucao() { return statusExecucao; }
    public void setStatusExecucao(String statusExecucao) { this.statusExecucao = statusExecucao; }

    public String getUrlChamada() { return urlChamada; }
    public void setUrlChamada(String urlChamada) { this.urlChamada = urlChamada; }

    public Integer getRespostaHttp() { return respostaHttp; }
    public void setRespostaHttp(Integer respostaHttp) { this.respostaHttp = respostaHttp; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    public Long getTempoExecucaoMs() { return tempoExecucaoMs; }
    public void setTempoExecucaoMs(Long tempoExecucaoMs) { this.tempoExecucaoMs = tempoExecucaoMs; }
}