package com.mercado.produtos.controller.api;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.Conteudo;
import com.mercado.produtos.dao.model.Terminal;
import com.mercado.produtos.dao.repository.TerminalRepository;
import com.mercado.produtos.service.AgendamentoService;
import com.mercado.produtos.service.TVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tv")
@Slf4j
@CrossOrigin(origins = "*")
public class TVApiController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private TVService tvService;

    @Autowired
    private TerminalRepository terminalRepository;

    /**
     * Busca agendamentos específicos para uma TV
     */
    @GetMapping("/{categoria}/{numero}/agendamentos")
    public ResponseEntity<List<Agendamento>> getAgendamentosTV(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);

        if (terminalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Terminal terminal = terminalOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        // Busca agendamentos ativos para este terminal
        List<Agendamento> agendamentos = agendamentoService.findConflitosAgendamento(agora, agora)
                .stream()
                .filter(a -> a.getTerminalConteudo().getTerminal().getId().equals(terminal.getId()) && a.isAtivo())
                .collect(Collectors.toList());

        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Verifica se a TV deve exibir conteúdo agora
     */
    @GetMapping("/{categoria}/{numero}/status")
    public ResponseEntity<Map<String, Object>> getStatusTV(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Map<String, Object> response = new HashMap<>();

        try{
            log.info("Verificando status categoria {} TV {}", categoria, numero);
            Map<String, Object> status = tvService.getStatusCompleto(categoria, numero);

            System.out.println("Status retornado: " + status);
            System.out.println("=============================");

            if (status != null) {
                // Copia todos os valores de forma segura
                status.forEach((key, value) -> response.put(key, value));
                System.out.println("Status obtido: " + response.get("encontrado"));
            } else {
                // Fallback se status for null
                criarStatusPadrao(response, categoria, numero, "Status retornado é null");
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar status da TV: " + e.getMessage());
            e.printStackTrace();
            criarStatusPadrao(response, categoria, numero, e.getMessage());
        }

        System.out.println("=== Retornando response ===");
        return ResponseEntity.ok(response);
    }
    /**
     * Registra que uma TV foi aberta (para logs)
     */
    @PostMapping("/{categoria}/{numero}/abrir")
    public ResponseEntity<Map<String, String>> registrarAberturaTV(
            @PathVariable String categoria,
            @PathVariable Integer numero,
            @RequestBody(required = false) Map<String, Object> dados) {

        // USA HASHMAP EM VEZ DE Map.of()
        Map<String, String> response = new HashMap<>();

        try {
            tvService.registrarVerificacaoTV(categoria, numero);
            System.out.println("TV aberta: " + categoria + "/" + numero + " - " + LocalDateTime.now());

            response.put("status", "success");
            response.put("message", "Abertura registrada com sucesso");
            response.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            System.err.println("Erro ao registrar abertura da TV: " + e.getMessage());

            response.put("status", "error");
            response.put("message", "Erro ao registrar abertura: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
        }

        return ResponseEntity.ok(response);
    }
    /**
     * Cria um status padrão quando há erro
     */
    private void criarStatusPadrao(Map<String, Object> response, String categoria, Integer numero, String erro) {
        response.put("encontrado", false);
        response.put("erro", erro);
        response.put("categoria", categoria);
        response.put("numero", numero);
        response.put("agendamentosAtivos", 0);
        response.put("deveExibirConteudo", false);
        response.put("agendamentoAtual", null);
        response.put("proximosAgendamentos", List.of());
        response.put("tvOnline", false);
        response.put("ultimaVerificacao", null);
        response.put("timestamp", LocalDateTime.now());

        // Info básica do terminal
        Map<String, Object> terminalInfo = new HashMap<>();
        terminalInfo.put("categoria", categoria);
        terminalInfo.put("numero", numero);
        terminalInfo.put("id", null);
        terminalInfo.put("nome", "N/A");
        terminalInfo.put("localizacao", "N/A");
        response.put("terminal", terminalInfo);
    }
    /**
     * DEBUG: Endpoint para investigar problemas - VERSÃO SIMPLES
     */
    @GetMapping("/{categoria}/{numero}/debug")
    public ResponseEntity<Map<String, Object>> debugTV(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Map<String, Object> debug = new HashMap<>();

        try {
            System.out.println("=== 🔍 ENDPOINT DEBUG CHAMADO ===");

            // Chama o debug completo
            tvService.debugCompleto(categoria, numero);

            debug.put("categoria", categoria);
            debug.put("numero", numero);
            debug.put("timestamp", LocalDateTime.now());
            debug.put("debug", "Verifique o console do servidor para logs detalhados");
            debug.put("status", "Debug executado com sucesso");

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("erro", e.getMessage());
            debug.put("categoria", categoria);
            debug.put("numero", numero);
            debug.put("timestamp", LocalDateTime.now());
            e.printStackTrace();
            return ResponseEntity.ok(debug);
        }
    }

    /**
     * DEBUG: Endpoint para testar apenas horário
     */
    @GetMapping("/{categoria}/{numero}/debug-horario")
    public ResponseEntity<Map<String, Object>> debugHorario(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== ⏰ DEBUG HORÁRIO CHAMADO ===");

            tvService.debugHorario(categoria, numero);

            response.put("categoria", categoria);
            response.put("numero", numero);
            response.put("horarioAtual", LocalDateTime.now());
            response.put("debug", "Verifique o console para comparação de horários");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("erro", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(response);
        }
    }
    /**
     * DEBUG: Teste rápido sem logs excessivos
     */
    @GetMapping("/{categoria}/{numero}/teste-rapido")
    public ResponseEntity<Map<String, Object>> testeRapido(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Dados básicos
            response.put("categoria", categoria);
            response.put("numero", numero);
            response.put("horarioAtual", LocalDateTime.now());

            // Testa se terminal existe
            Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);
            response.put("terminalEncontrado", terminalOpt.isPresent());

            if (terminalOpt.isPresent()) {
                Terminal terminal = terminalOpt.get();
                response.put("terminalId", terminal.getId());
                response.put("terminalNome", terminal.getNome());

                // Testa agendamentos usando método que existe
                List<Agendamento> agendamentos = agendamentoService.findByTerminal(terminal);
                response.put("totalAgendamentos", agendamentos.size());

                // Testa query específica
                LocalDateTime agora = LocalDateTime.now();
                List<Agendamento> ativosQuery = agendamentoService.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);
                response.put("agendamentosAtivos", ativosQuery.size());

                // Lista os agendamentos encontrados
                List<Map<String, Object>> agendamentosInfo = new ArrayList<>();
                for (Agendamento a : agendamentos) {
                    Map<String, Object> agendInfo = new HashMap<>();
                    agendInfo.put("id", a.getId());
                    agendInfo.put("titulo", a.getTitulo());
                    agendInfo.put("inicio", a.getDataInicio());
                    agendInfo.put("fim", a.getDataFim());
                    agendInfo.put("ativo", a.isAtivo());

                    // Verifica se deveria estar ativo
                    boolean inicioOk = a.getDataInicio().isBefore(agora) || a.getDataInicio().isEqual(agora);
                    boolean fimOk = a.getDataFim().isAfter(agora) || a.getDataFim().isEqual(agora);
                    agendInfo.put("deveriaEstarAtivo", inicioOk && fimOk && a.isAtivo());

                    agendamentosInfo.add(agendInfo);
                }
                response.put("agendamentosDetalhes", agendamentosInfo);

                // Testa service
                List<Conteudo> conteudos = tvService.exibirConteudo(categoria, numero);
                response.put("deveExibirConteudo", !conteudos.isEmpty());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("erro", e.getMessage());
            response.put("categoria", categoria);
            response.put("numero", numero);
            e.printStackTrace();
            return ResponseEntity.ok(response);
        }
    }
    /**
     * DEBUG: Investigar agendamento específico
     */
    @GetMapping("/{categoria}/{numero}/debug-agendamento")
    public ResponseEntity<Map<String, Object>> debugAgendamentoEspecifico(
            @PathVariable String categoria,
            @PathVariable Integer numero) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== 🔍 DEBUG AGENDAMENTO ESPECÍFICO VIA API ===");

            // Chama o debug específico
            tvService.debugAgendamentoEspecifico(categoria, numero);

            // Busca dados para retornar na API também
            Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);

            if (terminalOpt.isPresent()) {
                Terminal terminal = terminalOpt.get();
                List<Agendamento> todosAgendamentos = agendamentoService.findByTerminal(terminal);

                LocalDateTime agora = LocalDateTime.now();

                response.put("terminal", Map.of(
                        "id", terminal.getId(),
                        "nome", terminal.getNome(),
                        "categoria", categoria,
                        "numero", numero
                ));

                response.put("horarioAtual", agora);
                response.put("totalAgendamentos", todosAgendamentos.size());

                // Detalhes de cada agendamento
                List<Map<String, Object>> agendamentosDetalhes = new ArrayList<>();

                for (Agendamento a : todosAgendamentos) {
                    Map<String, Object> detalhe = new HashMap<>();
                    detalhe.put("id", a.getId());
                    detalhe.put("titulo", a.getTitulo());
                    detalhe.put("inicio", a.getDataInicio());
                    detalhe.put("fim", a.getDataFim());
                    detalhe.put("ativo", a.isAtivo());

                    // Verificações
                    boolean inicioOk = a.getDataInicio().isBefore(agora) || a.getDataInicio().isEqual(agora);
                    boolean fimOk = a.getDataFim().isAfter(agora) || a.getDataFim().isEqual(agora);

                    detalhe.put("inicioOk", inicioOk);
                    detalhe.put("fimOk", fimOk);
                    detalhe.put("deveriaEstarAtivo", inicioOk && fimOk && a.isAtivo());

                    // Diferença em segundos
                    long diferencaSegundos = java.time.Duration.between(a.getDataInicio(), agora).getSeconds();
                    detalhe.put("diferencaSegundos", diferencaSegundos);

                    agendamentosDetalhes.add(detalhe);
                }

                response.put("agendamentos", agendamentosDetalhes);

                // Testar query
                List<Agendamento> queryResult = agendamentoService.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);
                response.put("queryResultado", queryResult.size());

            } else {
                response.put("erro", "Terminal não encontrado");
            }

            response.put("categoria", categoria);
            response.put("numero", numero);
            response.put("debug", "Verifique o console para logs detalhados");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("erro", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}