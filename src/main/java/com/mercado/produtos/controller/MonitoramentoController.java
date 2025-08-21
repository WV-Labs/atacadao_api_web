package com.mercado.produtos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoramentoController {

    @GetMapping("/monitoramento")
    public String paginaMonitoramento() {
        return "monitoramento"; // Retorna a view monitoramento.html
    }

    @GetMapping("/agendamentos")
    public String paginaAgendamentos() {
        return "agendamentos"; // Página para gerenciar agendamentos
    }
}