package com.mercado.produtos.dao.repository;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.AgendamentoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoLogRepository extends JpaRepository<AgendamentoLog, Long> {
    List<AgendamentoLog> findByAgendamento(Agendamento agendamento);
    List<AgendamentoLog> findByStatusExecucao(String status);
    @Query("SELECT al FROM AgendamentoLog al WHERE al.dataExecucao BETWEEN :inicio AND :fim ORDER BY al.dataExecucao DESC")
    List<AgendamentoLog> findLogsPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    @Query("SELECT COUNT(al) FROM AgendamentoLog al WHERE al.agendamento.id = :agendamentoId AND al.statusExecucao = 'SUCCESS' AND al.dataExecucao >= :dataLimite")
    Long countExecucoesSucesso(@Param("agendamentoId") Long agendamentoId, @Param("dataLimite") LocalDateTime dataLimite);
    @Query("SELECT al FROM AgendamentoLog al WHERE al.agendamento.id = :agendamentoId ORDER BY al.dataExecucao DESC")
    List<AgendamentoLog> findByAgendamentoId(@Param("agendamentoId") Long agendamentoId);
    @Query("SELECT al FROM AgendamentoLog al WHERE al.agendamento.id = :agendamentoId " +
            "ORDER BY al.dataExecucao DESC LIMIT :limite")
    List<AgendamentoLog> findUltimosLogsPorAgendamento(@Param("agendamentoId") Long agendamentoId, @Param("limite") int limite);
    @Query("SELECT al FROM AgendamentoLog al WHERE al.statusExecucao = 'ERROR' ORDER BY al.dataExecucao DESC")
    List<AgendamentoLog> findLogsComErro();
    @Query("SELECT al FROM AgendamentoLog al WHERE " +
            "al.dataExecucao >= :inicioHoje AND al.dataExecucao < :fimHoje " +
            "ORDER BY al.dataExecucao DESC")
    List<AgendamentoLog> findLogsDeHoje(@Param("inicioHoje") LocalDateTime inicioHoje,
                                                   @Param("fimHoje") LocalDateTime fimHoje);

    @Query("SELECT al.statusExecucao, COUNT(al) FROM AgendamentoLog al GROUP BY al.statusExecucao")
    List<Object[]> countPorStatus();
    @Query("SELECT al FROM AgendamentoLog al WHERE al.dataExecucao >= :dataLimite ORDER BY al.dataExecucao DESC")
    List<AgendamentoLog> findLogsRecentes(@Param("dataLimite") LocalDateTime dataLimite);
}
