package com.mercado.produtos.controller.api;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.AgendamentoLog;
import com.mercado.produtos.dao.repository.AgendamentoRepository;
import com.mercado.produtos.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*") // Para permitir chamadas do frontend
public class AgendamentoApiControllerCompleto {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Value("${api-tv.tv.base.url:http://localhost:8081/api-tv}")
    private String baseUrl;
    /**
     * Endpoint para frontend verificar agendamentos ativos
     */
    @GetMapping("/ativos")
    public ResponseEntity<List<Agendamento>> getAgendamentosAtivos() {
        List<Agendamento> agendamentos = agendamentoService.buscarAgendamentosParaExecucao();
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Buscar próximos agendamentos (próximas 24 horas)
     */
    @GetMapping("/proximos")
    public ResponseEntity<List<Agendamento>> getProximosAgendamentos() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime em24h = agora.plusHours(24);

        List<Agendamento> proximos = agendamentoService.findProximosAgendamentos(agora, em24h);
        return ResponseEntity.ok(proximos);
    }

    /**
     * Endpoint para executar um agendamento manualmente
     */
    @PostMapping("/{id}/executar")
    public ResponseEntity<Map<String, String>> executarAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamentoOpt = agendamentoService.findById(id);

        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Agendamento agendamento = agendamentoOpt.get();

        try {
            agendamentoService.executarAgendamento(agendamento);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Agendamento executado com sucesso",
                    "agendamento", agendamento.getTitulo()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "Erro ao executar agendamento: " + e.getMessage(),
                    "agendamento", agendamento.getTitulo()
            ));
        }
    }

    /**
     * Buscar logs de um agendamento específico
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<AgendamentoLog>> getLogsAgendamento(@PathVariable Long id) {
        List<AgendamentoLog> logs = agendamentoService.buscarLogsAgendamento(id);
        return ResponseEntity.ok(logs);
    }

    /**
     * Status geral do sistema
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatusSistema() {
        List<Agendamento> ativos = agendamentoService.buscarAgendamentosParaExecucao();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime em24h = agora.plusHours(24);
        List<Agendamento> proximos = agendamentoService.findProximosAgendamentos(agora, em24h);

        return ResponseEntity.ok(Map.of(
                "agendamentosAtivos", ativos.size(),
                "proximosAgendamentos", proximos.size(),
                "ultimaVerificacao", LocalDateTime.now(),
                "sistemaTvUrl", baseUrl,
                "status", "online"
        ));
    }
    /**
     * Busca logs de hoje
     */
    @GetMapping("/logs/hoje")
    public ResponseEntity<List<AgendamentoLog>> getLogsDeHoje() {
        List<AgendamentoLog> logs = agendamentoService.buscarLogsDeHoje();
        return ResponseEntity.ok(logs);
    }
    /**
     * Relatório completo de hoje
     */
    @GetMapping("/relatorio/hoje")
    public ResponseEntity<Map<String, Object>> getRelatorioDeHoje() {
        Map<String, Object> relatorio = agendamentoService.gerarRelatorioDeHoje();
        return ResponseEntity.ok(relatorio);
    }
    /**
     * Estatísticas resumidas de hoje
     */
    @GetMapping("/estatisticas/hoje")
    public ResponseEntity<Map<String, Long>> getEstatisticasDeHoje() {
        Map<String, Long> stats = agendamentoService.getEstatisticasDeHoje();
        return ResponseEntity.ok(stats);
    }
}