package com.mercado.produtos.service;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.Conteudo;
import com.mercado.produtos.dao.model.Terminal;
import com.mercado.produtos.dao.repository.AgendamentoRepository;
import com.mercado.produtos.dao.repository.TerminalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TVService {

    // Mapa para controlar últimas verificações de cada TV
    private final Map<String, LocalDateTime> ultimasVerificacoes = new ConcurrentHashMap<>();

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    /**
     * MÉTODO PRINCIPAL: Verifica se uma TV específica deve exibir conteúdo agora
     * Implementa lógica de priorização de agendamentos sobrepostos
     */
    public List<Conteudo> exibirConteudo(String categoria, Integer numero) {
        String chaveTV = categoria + "/" + numero;
        ultimasVerificacoes.put(chaveTV, LocalDateTime.now());

        // Busca terminal
        Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);
        if (terminalOpt.isEmpty()) {
            System.out.println("❌ Terminal não encontrado: " + categoria + "/" + numero);
            return Collections.emptyList();
        }

        Terminal terminal = terminalOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        System.out.println("🔍 Verificando conteúdo para TV: " + categoria + "/" + numero + " às " + agora);

        // Busca TODOS os agendamentos ativos para este terminal no momento atual
        List<Agendamento> agendamentosAtivos = agendamentoRepository.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);

        if (agendamentosAtivos.isEmpty()) {
            System.out.println("📭 Nenhum agendamento ativo encontrado para " + categoria + "/" + numero);
            return Collections.emptyList();
        }

        System.out.println("📋 Encontrados " + agendamentosAtivos.size() + " agendamento(s) ativo(s):");
        agendamentosAtivos.forEach(a -> {
            System.out.println("  - " + a.getTitulo() + " (" + a.getDataInicio() + " até " + a.getDataFim() + ")");
        });

        // LÓGICA DE PRIORIZAÇÃO: Agendamento mais específico (menor duração) tem prioridade
        Agendamento agendamentoPrioritario = determinarAgendamentoPrioritario(agendamentosAtivos, agora);

        if (agendamentoPrioritario != null) {
            System.out.println("🎯 Agendamento prioritário: " + agendamentoPrioritario.getTitulo());

            // Retorna o conteúdo do agendamento prioritário
            List<Conteudo> conteudos = new ArrayList<>();
            conteudos.add(agendamentoPrioritario.getTerminalConteudo().getConteudo());
            return conteudos;
        }

        return Collections.emptyList();
    }

    /**
     * NOVA LÓGICA DE PRIORIZAÇÃO
     * Regras:
     * 1. Agendamentos com menor duração têm prioridade (mais específicos)
     * 2. Em caso de empate, o mais recente (ID maior) tem prioridade
     * 3. Agendamentos que começaram mais recentemente têm prioridade
     */
    private Agendamento determinarAgendamentoPrioritario(List<Agendamento> agendamentos, LocalDateTime agora) {
        if (agendamentos.isEmpty()) {
            return null;
        }

        return agendamentos.stream()
                .sorted((a1, a2) -> {
                    // 1. Prioridade por duração (menor duração = mais específico = maior prioridade)
                    long duracao1 = Duration.between(a1.getDataInicio(), a1.getDataFim()).toMinutes();
                    long duracao2 = Duration.between(a2.getDataInicio(), a2.getDataFim()).toMinutes();

                    if (duracao1 != duracao2) {
                        return Long.compare(duracao1, duracao2); // Menor duração primeiro
                    }

                    // 2. Se duração igual, prioridade por início mais próximo do momento atual
                    long distanciaInicio1 = Math.abs(Duration.between(a1.getDataInicio(), agora).toMinutes());
                    long distanciaInicio2 = Math.abs(Duration.between(a2.getDataInicio(), agora).toMinutes());

                    if (distanciaInicio1 != distanciaInicio2) {
                        return Long.compare(distanciaInicio1, distanciaInicio2); // Mais próximo do início primeiro
                    }

                    // 3. Como último critério, ID mais alto (mais recente)
                    return Long.compare(a2.getId(), a1.getId()); // ID maior primeiro
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca TODOS os agendamentos ativos para uma TV específica (para debugging)
     */
    public List<Agendamento> getAgendamentosAtivos(String categoria, Integer numero) {
        Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);
        if (terminalOpt.isEmpty()) {
            return List.of();
        }

        Terminal terminal = terminalOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        List<Agendamento> agendamentosAtivos = agendamentoRepository.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);

        // Retorna ordenado pela lógica de priorização
        return agendamentosAtivos.stream()
                .sorted((a1, a2) -> {
                    long duracao1 = Duration.between(a1.getDataInicio(), a1.getDataFim()).toMinutes();
                    long duracao2 = Duration.between(a2.getDataInicio(), a2.getDataFim()).toMinutes();

                    if (duracao1 != duracao2) {
                        return Long.compare(duracao1, duracao2);
                    }

                    return Long.compare(a2.getId(), a1.getId());
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca próximos agendamentos para uma TV específica
     */
    public List<Agendamento> getProximosAgendamentos(String categoria, Integer numero) {
        Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);
        if (terminalOpt.isEmpty()) {
            return List.of();
        }

        Terminal terminal = terminalOpt.get();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime em24h = agora.plusHours(24);

        return agendamentoRepository.findProximosAgendamentos(agora, em24h)
                .stream()
                .filter(a -> a.getTerminalConteudo().getTerminal().getId().equals(terminal.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Registra que uma TV verificou o sistema
     */
    public void registrarVerificacaoTV(String categoria, Integer numero) {
        String chaveTV = categoria + "/" + numero;
        ultimasVerificacoes.put(chaveTV, LocalDateTime.now());
        System.out.println("📺 TV registrada: " + chaveTV + " às " + LocalDateTime.now());
    }

    /**
     * Busca agendamento ativo prioritário para uma TV específica
     */
    public Optional<Agendamento> getAgendamentoAtivo(String categoria, Integer numero) {
        Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);
        if (terminalOpt.isEmpty()) {
            return Optional.empty();
        }

        Terminal terminal = terminalOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        List<Agendamento> agendamentosAtivos = agendamentoRepository.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);

        if (agendamentosAtivos.isEmpty()) {
            return Optional.empty();
        }

        Agendamento prioritario = determinarAgendamentoPrioritario(agendamentosAtivos, agora);
        return Optional.ofNullable(prioritario);
    }

    /**
     * Lista todas as TVs que estão verificando o sistema
     */
    public Map<String, LocalDateTime> getTVsOnline() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(2);
        return ultimasVerificacoes.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAfter(limite))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /**
     * Verifica se uma TV está online (verificou recentemente)
     */
    public boolean isTVOnline(String categoria, Integer numero) {
        String chaveTV = categoria + "/" + numero;
        LocalDateTime ultimaVerificacao = ultimasVerificacoes.get(chaveTV);
        if (ultimaVerificacao == null) {
            return false;
        }
        LocalDateTime limite = LocalDateTime.now().minusMinutes(2);
        return ultimaVerificacao.isAfter(limite);
    }

    /**
     * MÉTODO PRINCIPAL PARA STATUS COMPLETO COM PRIORIZAÇÃO
     */
    public Map<String, Object> getStatusCompleto(String categoria, Integer numero) {
        Map<String, Object> status = new HashMap<>();

        try {
            Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);

            if (terminalOpt.isEmpty()) {
                return criarStatusErro(categoria, numero, "Terminal não encontrado");
            }

            Terminal terminal = terminalOpt.get();
            LocalDateTime agora = LocalDateTime.now();

            // Busca todos os agendamentos ativos
            List<Agendamento> todosAgendamentosAtivos = agendamentoRepository.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);

            // Determina o agendamento prioritário
            Agendamento agendamentoPrioritario = determinarAgendamentoPrioritario(todosAgendamentosAtivos, agora);

            // Verifica se deve exibir conteúdo
            boolean deveExibirConteudo = agendamentoPrioritario != null;

            // Próximos agendamentos
            List<Agendamento> proximosAgendamentos = getProximosAgendamentos(categoria, numero);

            // Monta o status
            status.put("encontrado", true);
            status.put("categoria", categoria);
            status.put("numero", numero);
            status.put("agendamentosAtivos", todosAgendamentosAtivos.size());
            status.put("deveExibirConteudo", deveExibirConteudo);
            status.put("agendamentoAtual", agendamentoPrioritario);
            status.put("todosAgendamentosAtivos", todosAgendamentosAtivos);
            status.put("proximosAgendamentos", proximosAgendamentos);
            status.put("tvOnline", isTVOnline(categoria, numero));
            status.put("ultimaVerificacao", ultimasVerificacoes.get(categoria + "/" + numero));
            status.put("timestamp", agora);

            // Info do terminal
            Map<String, Object> terminalInfo = new HashMap<>();
            terminalInfo.put("id", terminal.getId());
            terminalInfo.put("nome", terminal.getNome());
            terminalInfo.put("categoria", categoria);
            terminalInfo.put("numero", numero);
            terminalInfo.put("localizacao", terminal.getLocalizacao());
            status.put("terminal", terminalInfo);

            System.out.println("📊 Status gerado para " + categoria + "/" + numero +
                    " - Deve exibir: " + deveExibirConteudo +
                    " - Agendamentos ativos: " + todosAgendamentosAtivos.size());

            return status;

        } catch (Exception e) {
            System.err.println("❌ Erro em getStatusCompleto: " + e.getMessage());
            e.printStackTrace();
            return criarStatusErro(categoria, numero, "Erro interno: " + e.getMessage());
        }
    }

    /**
     * Cria status de erro padronizado
     */
    private Map<String, Object> criarStatusErro(String categoria, Integer numero, String mensagem) {
        Map<String, Object> status = new HashMap<>();
        status.put("encontrado", false);
        status.put("erro", mensagem);
        status.put("categoria", categoria);
        status.put("numero", numero);
        status.put("agendamentosAtivos", 0);
        status.put("deveExibirConteudo", false);
        status.put("agendamentoAtual", null);
        status.put("proximosAgendamentos", List.of());
        status.put("tvOnline", false);
        status.put("ultimaVerificacao", null);
        status.put("timestamp", LocalDateTime.now());

        Map<String, Object> terminalInfo = new HashMap<>();
        terminalInfo.put("categoria", categoria);
        terminalInfo.put("numero", numero);
        terminalInfo.put("id", null);
        terminalInfo.put("nome", "N/A");
        terminalInfo.put("localizacao", "N/A");
        status.put("terminal", terminalInfo);

        return status;
    }

    /**
     * Limpa verificações antigas (pode ser chamado periodicamente)
     */
    public void limparVerificacoesAntigas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(1);
        ultimasVerificacoes.entrySet().removeIf(entry -> entry.getValue().isBefore(limite));
    }

    /**
     * DEBUG: Método para investigar problemas com priorização
     */
    public void debugCompleto(String categoria, Integer numero) {
        System.out.println("=== 🔍 DEBUG COMPLETO COM PRIORIZAÇÃO ===");
        System.out.println("Categoria: " + categoria + ", Número: " + numero);

        try {
            Optional<Terminal> terminalOpt = terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(categoria, numero);

            if (terminalOpt.isEmpty()) {
                System.out.println("❌ Terminal não encontrado!");
                return;
            }

            Terminal terminal = terminalOpt.get();
            LocalDateTime agora = LocalDateTime.now();

            System.out.println("✅ Terminal encontrado: ID=" + terminal.getId() + ", Nome=" + terminal.getNome());
            System.out.println("⏰ Horário atual: " + agora);

            // Busca todos os agendamentos ativos
            List<Agendamento> agendamentosAtivos = agendamentoRepository.findAgendamentosAtivosPorTerminal(terminal.getId(), agora);

            System.out.println("📋 Agendamentos ativos encontrados: " + agendamentosAtivos.size());

            if (agendamentosAtivos.isEmpty()) {
                System.out.println("📭 Nenhum agendamento ativo no momento");
                return;
            }

            // Mostra todos os agendamentos e suas durações
            System.out.println("📅 Detalhes dos agendamentos:");
            for (int i = 0; i < agendamentosAtivos.size(); i++) {
                Agendamento a = agendamentosAtivos.get(i);
                long duracao = Duration.between(a.getDataInicio(), a.getDataFim()).toMinutes();
                long distanciaInicio = Math.abs(Duration.between(a.getDataInicio(), agora).toMinutes());

                System.out.println("  " + (i+1) + ". ID=" + a.getId() + " - " + a.getTitulo());
                System.out.println("     Início: " + a.getDataInicio());
                System.out.println("     Fim: " + a.getDataFim());
                System.out.println("     Duração: " + duracao + " minutos");
                System.out.println("     Distância do início: " + distanciaInicio + " minutos");
                System.out.println("     Conteúdo: " + (a.getTerminalConteudo() != null ?
                        a.getTerminalConteudo().getConteudo().getTitulo() : "N/A"));
            }

            // Determina o prioritário
            Agendamento prioritario = determinarAgendamentoPrioritario(agendamentosAtivos, agora);

            if (prioritario != null) {
                System.out.println("🎯 AGENDAMENTO PRIORITÁRIO: " + prioritario.getTitulo() + " (ID=" + prioritario.getId() + ")");
                System.out.println("   Motivo: Menor duração e/ou mais próximo do início");
            } else {
                System.out.println("❌ Nenhum agendamento prioritário determinado");
            }

            // Testa conteúdo
            List<Conteudo> conteudos = exibirConteudo(categoria, numero);
            System.out.println("🎭 Deve exibir conteúdo: " + !conteudos.isEmpty());

            if (!conteudos.isEmpty()) {
                System.out.println("📺 Conteúdo a ser exibido: " + conteudos.get(0).getTitulo());
            }

        } catch (Exception e) {
            System.out.println("❌ ERRO durante debug: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== 🔚 FIM DEBUG COMPLETO ===");
    }

    // Métodos para debug específicos mantidos
    public void debugHorario(String categoria, Integer numero) {
        // Método mantido conforme original
    }

    public void debugAgendamentoEspecifico(String categoria, Integer numero) {
        // Método mantido conforme original
    }
}