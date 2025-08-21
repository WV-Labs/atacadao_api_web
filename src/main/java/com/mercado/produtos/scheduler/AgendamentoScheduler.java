package com.mercado.produtos.scheduler;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.service.AgendamentoService;
import com.mercado.produtos.service.TVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AgendamentoScheduler {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private TVService tvService;

    @Value("${agendamento.verificacao.intervalo:30000}")
    private long intervaloVerificacao;

    private int contadorErros = 0;
    private LocalDateTime ultimaVerificacao = LocalDateTime.now();

    /**
     * Verifica agendamentos periodicamente (a cada 30 segundos por padrão)
     */
    @Scheduled(fixedRateString = "${agendamento.verificacao.intervalo:30000}")
    public void verificarAgendamentos() {
        try {
            System.out.println("🔍 Verificando agendamentos... " + LocalDateTime.now().toString().substring(11, 19));

            List<Agendamento> agendamentos = agendamentoService.buscarAgendamentosParaExecucao();

            if (!agendamentos.isEmpty()) {
                System.out.println("📋 Encontrados " + agendamentos.size() + " agendamento(s) para execução:");

                for (Agendamento agendamento : agendamentos) {
                    // Verifica se já foi executado nos últimos 5 minutos para evitar execuções duplicadas
                    if (!agendamentoService.foiExecutadoRecentemente(agendamento, 5)) {
                        System.out.println("⚡ Executando: " + agendamento.getTitulo() +
                                " (" + agendamento.getTerminalConteudo().getTerminal().getCategoria().getNome() +
                                "/" + agendamento.getTerminalConteudo().getTerminal().getNrTerminal() + ")");
                        agendamentoService.executarAgendamento(agendamento);
                    } else {
                        System.out.println("⏭️ Agendamento " + agendamento.getTitulo() + " já foi executado recentemente");
                    }
                }
            } else {
                // Log silencioso para não poluir o console
                if (LocalDateTime.now().getMinute() % 5 == 0) { // Log a cada 5 minutos quando não há agendamentos
                    System.out.println("📭 Nenhum agendamento ativo no momento");
                }
            }

            ultimaVerificacao = LocalDateTime.now();
            contadorErros = 0; // Reset contador de erros em caso de sucesso

        } catch (Exception e) {
            contadorErros++;
            System.err.println("❌ Erro na verificação de agendamentos (tentativa " + contadorErros + "): " + e.getMessage());

            // Se houver muitos erros consecutivos, pode implementar notificação ou pausa
            if (contadorErros >= 5) {
                System.err.println("🚨 ATENÇÃO: Muitos erros consecutivos no scheduler de agendamentos!");
                // Aqui poderia enviar email, notificação, etc.
            }
        }
    }

    /**
     * Relatório de status a cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora
    public void relatorioStatus() {
        try {
            List<Agendamento> ativos = agendamentoService.buscarAgendamentosParaExecucao();
            Map<String, LocalDateTime> tvsOnline = tvService.getTVsOnline();

            System.out.println("");
            System.out.println("=== 📊 RELATÓRIO DE STATUS ===");
            System.out.println("🕐 Horário: " + LocalDateTime.now());
            System.out.println("📅 Agendamentos ativos: " + ativos.size());
            System.out.println("📺 TVs online: " + tvsOnline.size());
            System.out.println("🔍 Última verificação: " + ultimaVerificacao);
            System.out.println("⚠️ Erros consecutivos: " + contadorErros);

            if (!tvsOnline.isEmpty()) {
                System.out.println("📺 TVs conectadas:");
                tvsOnline.forEach((tv, ultimaVerificacao) -> {
                    System.out.println("   - " + tv + " (última verificação: " +
                            ultimaVerificacao.toString().substring(11, 19) + ")");
                });
            }

            if (!ativos.isEmpty()) {
                System.out.println("📋 Agendamentos ativos:");
                ativos.forEach(agendamento -> {
                    System.out.println("   - " + agendamento.getTitulo() +
                            " (" + agendamento.getTerminalConteudo().getTerminal().getCategoria().getNome() +
                            "/" + agendamento.getTerminalConteudo().getTerminal().getNrTerminal() + ")");
                });
            }

            System.out.println("==============================");
            System.out.println("");

        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar relatório de status: " + e.getMessage());
        }
    }

    /**
     * Limpeza de verificações antigas das TVs (a cada 2 horas)
     */
    @Scheduled(fixedRate = 7200000) // 2 horas
    public void limpezaVerificacoesAntigas() {
        try {
            tvService.limparVerificacoesAntigas();
            System.out.println("🧹 Limpeza de verificações antigas das TVs realizada");
        } catch (Exception e) {
            System.err.println("❌ Erro na limpeza de verificações antigas: " + e.getMessage());
        }
    }

    /**
     * Verificação de saúde do sistema (a cada 15 minutos)
     */
    @Scheduled(fixedRate = 900000) // 15 minutos
    public void verificacaoSaude() {
        try {
            long agendamentosAtivos = agendamentoService.contarAgendamentosAtivos();
            Map<String, LocalDateTime> tvsOnline = tvService.getTVsOnline();

            // Log apenas se houver atividade significativa
            if (agendamentosAtivos > 0 || tvsOnline.size() > 0) {
                System.out.println("💓 Health Check - Agendamentos: " + agendamentosAtivos +
                        ", TVs Online: " + tvsOnline.size());
            }

            // Verifica se há problemas
            if (contadorErros >= 3) {
                System.out.println("⚠️ Sistema com " + contadorErros + " erros consecutivos");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro na verificação de saúde: " + e.getMessage());
        }
    }
}