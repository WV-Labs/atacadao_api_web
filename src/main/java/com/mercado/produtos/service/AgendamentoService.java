package com.mercado.produtos.service;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.AgendamentoLog;
import com.mercado.produtos.dao.model.Terminal;
import com.mercado.produtos.dao.repository.AgendamentoLogRepository;
import com.mercado.produtos.dao.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AgendamentoLogRepository logRepository;

    @Value("${api-tv.tv.base.url:http://localhost:8081/api-tv}")
    private String baseUrl;

    @Value("${agendamento.timeout.conexao:5000}")
    private int timeoutConexao;

    // Controle para evitar execuções duplicadas
    private final Set<Long> agendamentosEmExecucao = ConcurrentHashMap.newKeySet();

    /**
     * Busca agendamentos que devem ser executados agora
     */
    public List<Agendamento> buscarAgendamentosParaExecucao() {
        LocalDateTime agora = LocalDateTime.now();
        return agendamentoRepository.findAgendamentosAtivosPorPeriodo(agora);
    }
    public Optional<Agendamento> findById(Long id) {
        return agendamentoRepository.findById(id);
    }
    /**
     * Busca agendamentos ativos para uma categoria e número específicos
     */
    public List<Agendamento> buscarAgendamentosParaTV(String categoria, Integer numero) {
        LocalDateTime agora = LocalDateTime.now();
        return agendamentoRepository.findAgendamentosAtivosPorCategoriaENumero(categoria, numero, agora);
    }
    /**
     * Executa um agendamento com controle de logs e duplicação
     */
    public void executarAgendamento(Agendamento agendamento) {
        // Previne execuções simultâneas do mesmo agendamento
        if (!agendamentosEmExecucao.add(agendamento.getId())) {
            System.out.println("Agendamento " + agendamento.getId() + " já está em execução");
            return;
        }

        CompletableFuture.runAsync(() -> {
            AgendamentoLog log = null;
            long startTime = System.currentTimeMillis();

            try {
                String url = construirUrl(agendamento);
                log = new AgendamentoLog(agendamento, url);

                System.out.println("Executando agendamento: " + agendamento.getTitulo() + " - URL: " + url);

                // Configura timeout
                RestTemplate restTemplateComTimeout = criarRestTemplateComTimeout();
                ResponseEntity<String> response = restTemplateComTimeout.getForEntity(url, String.class);

                long endTime = System.currentTimeMillis();
                log.setTempoExecucaoMs(endTime - startTime);
                log.setRespostaHttp(response.getStatusCodeValue());
                log.setStatusExecucao("SUCCESS");

                System.out.println("Agendamento executado com sucesso: " + agendamento.getTitulo() +
                        " (Tempo: " + (endTime - startTime) + "ms)");

            } catch (Exception e) {
                long endTime = System.currentTimeMillis();

                if (log == null) {
                    log = new AgendamentoLog(agendamento, construirUrl(agendamento));
                }

                log.setTempoExecucaoMs(endTime - startTime);
                log.setStatusExecucao("ERROR");
                log.setMensagemErro(e.getMessage());

                System.err.println("Erro ao executar agendamento " + agendamento.getTitulo() + ": " + e.getMessage());

            } finally {
                // Salva o log
                if (log != null) {
                    logRepository.save(log);
                }

                // Remove do controle de execução
                agendamentosEmExecucao.remove(agendamento.getId());
            }
        });
    }

    private RestTemplate criarRestTemplateComTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutConexao);
        factory.setReadTimeout(timeoutConexao * 2); // Timeout de leitura um pouco maior

        return new RestTemplate(factory);
    }

    /**
     * Verifica se uma TV específica deve exibir conteúdo agora
     */
    public boolean deveExibirConteudo(String categoria, Integer numero) {
        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> agendamentos = agendamentoRepository.findAgendamentosAtivosPorCategoriaENumero(categoria, numero, agora);
        return !agendamentos.isEmpty();
    }
    /**
     * Busca próximos agendamentos (próximas 24 horas)
     */
    public List<Agendamento> buscarProximosAgendamentos() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime em24h = agora.plusHours(24);
        return agendamentoRepository.findProximosAgendamentos(agora, em24h);
    }
    /**
     * Busca próximos agendamentos para uma TV específica
     */
    public List<Agendamento> buscarProximosAgendamentosParaTV(String categoria, Integer numero) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime em24h = agora.plusHours(24);
        return agendamentoRepository.findProximosAgendamentos(agora, em24h)
                .stream()
                .filter(a -> {
                    String categoriaAgendamento = a.getTerminalConteudo().getTerminal().getCategoria().getNome()
                            .toLowerCase().replaceAll("\\s+", "");
                    Integer numeroAgendamento = a.getTerminalConteudo().getTerminal().getNrTerminal();
                    return categoriaAgendamento.equals(categoria.toLowerCase()) &&
                            numeroAgendamento.equals(numero);
                })
                .toList();
    }

    /**
     * Constrói a URL do agendamento baseada no terminal
     */
    private String construirUrl(Agendamento agendamento) {
        String categoria = agendamento.getTerminalConteudo().getTerminal().getCategoria().getNome()
                .toLowerCase()
                .replaceAll("\\s+", "");
        Integer nrTerminal = agendamento.getTerminalConteudo().getTerminal().getNrTerminal();

        return String.format("%s/%s/%d", baseUrl, categoria, nrTerminal);
    }

    /**
     * Busca logs de execução de um agendamento
     */
    public List<AgendamentoLog> buscarLogsAgendamento(Long agendamentoId) {
        return logRepository.findByAgendamentoId(agendamentoId);
    }

    public List<Agendamento> findProximosAgendamentos(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return agendamentoRepository.findProximosAgendamentos(dataInicio, dataFim);
    }

    /**
     * Verifica se um agendamento já foi executado com sucesso recentemente
     */
    public boolean foiExecutadoRecentemente(Agendamento agendamento, int minutosAtras) {
        LocalDateTime dataLimite = LocalDateTime.now().minusMinutes(minutosAtras);
        Long execucoes = logRepository.countExecucoesSucesso(agendamento.getId(), dataLimite);
        return execucoes > 0;
    }
    public List<Agendamento> findConflitosAgendamento(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return agendamentoRepository.findConflitosAgendamento(dataInicio, dataFim);
    }
    public Agendamento save(Agendamento Agendamento) {
        return agendamentoRepository.save(Agendamento);
    }
    public boolean temConflito(Agendamento agendamento) {
        List<Agendamento> conflitosAgendamento = findConflitosAgendamento(agendamento.getDataInicio(), agendamento.getDataFim());
        return conflitosAgendamento.stream()
                .anyMatch(c -> !c.getId().equals(agendamento.getId()) &&
                        c.getTerminalConteudo().getTerminal().equals(agendamento.getTerminalConteudo().getTerminal()));
    }

    /**
     * Busca todos os agendamentos ativos (independente do horário)
     */
    public List<Agendamento> buscarTodosAgendamentosAtivos() {
        return agendamentoRepository.findAllAgendamentosAtivos();
    }
    /**
     * Conta agendamentos ativos no momento
     */
    public long contarAgendamentosAtivos() {
        LocalDateTime agora = LocalDateTime.now();
        return agendamentoRepository.findAgendamentosAtivosPorPeriodo(agora).size();
    }
    /**
     * Busca logs recentes (últimas 24 horas)
     */
    public List<AgendamentoLog> buscarLogsRecentes() {
        LocalDateTime ontemMesmoHorario = LocalDateTime.now().minusHours(24);
        return logRepository.findLogsRecentes(ontemMesmoHorario);
    }
    /**
     * Busca logs de hoje usando o método do repository
     */
    public List<AgendamentoLog> buscarLogsDeHoje() {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = inicioHoje.plusDays(1);

        return logRepository.findLogsDeHoje(inicioHoje, fimHoje);
    }
    /**
     * Gera relatório de atividades de hoje
     */
    public Map<String, Object> gerarRelatorioDeHoje() {
        List<AgendamentoLog> logsHoje = buscarLogsDeHoje();

        long totalExecucoes = logsHoje.size();
        long execucoesSucesso = logsHoje.stream()
                .filter(log -> "SUCCESS".equals(log.getStatusExecucao()))
                .count();
        long execucoesErro = logsHoje.stream()
                .filter(log -> "ERROR".equals(log.getStatusExecucao()))
                .count();

        // Calcula tempo médio de execução
        OptionalDouble tempoMedio = logsHoje.stream()
                .filter(log -> log.getTempoExecucaoMs() != null)
                .mapToLong(AgendamentoLog::getTempoExecucaoMs)
                .average();

        return Map.of(
                "totalExecucoes", totalExecucoes,
                "execucoesSucesso", execucoesSucesso,
                "execucoesErro", execucoesErro,
                "taxaSucesso", totalExecucoes > 0 ? (execucoesSucesso * 100.0 / totalExecucoes) : 0,
                "tempoMedioMs", tempoMedio.orElse(0),
                "logs", logsHoje
        );
    }
    /**
     * Busca estatísticas rápidas de hoje
     */
    public Map<String, Long> getEstatisticasDeHoje() {
        List<AgendamentoLog> logsHoje = buscarLogsDeHoje();

        Map<String, Long> stats = logsHoje.stream()
                .collect(Collectors.groupingBy(
                        AgendamentoLog::getStatusExecucao,
                        Collectors.counting()
                ));

        stats.putIfAbsent("SUCCESS", 0L);
        stats.putIfAbsent("ERROR", 0L);
        stats.put("TOTAL", (long) logsHoje.size());

        return stats;
    }

    public List<Agendamento> findByTerminal(Terminal terminal) {
        return agendamentoRepository.findByTerminalConteudo_Terminal(terminal);
    }

    public List<Agendamento> findAgendamentosAtivosPorTerminal(Long id, LocalDateTime agora) {
        return agendamentoRepository.findAgendamentosAtivosPorTerminal(id, agora);
    }
}
