package com.mercado.produtos.dao.repository;

import com.mercado.produtos.dao.model.Agendamento;
import com.mercado.produtos.dao.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
  List<Agendamento> findByTerminalConteudo_Terminal(Terminal terminalConteudoTerminal);
  @Query("SELECT a FROM Agendamento a WHERE a.dataInicio <= :fim AND a.dataFim >= :inicio")
  List<Agendamento> findConflitosAgendamento(LocalDateTime inicio, LocalDateTime fim);
  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true AND a.dataInicio <= :agora AND a.dataFim >= :agora")
  List<Agendamento> findAgendamentosAtivosPorPeriodo(@Param("agora") LocalDateTime agora);
  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true AND a.dataInicio BETWEEN :inicio AND :fim ORDER BY a.dataInicio ASC")
  List<Agendamento> findProximosAgendamentos(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);


  String SQLORDER = " ORDER BY a.dataInicio ASC";
  String SQLCOMUMWHEREDATA = " AND a.dataInicio <= :agora AND a.dataFim >= :agora" ;
  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true and a.terminalConteudo.terminal.id = :terminalId " + SQLCOMUMWHEREDATA + SQLORDER)
  List<Agendamento> findAgendamentosAtivosPorTerminal(@Param("terminalId") Long terminalId,
                                                      @Param("agora") LocalDateTime agora);
  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true AND UPPER(a.terminalConteudo.terminal.categoria.nome) = UPPER(:nomeCategoria) " + SQLCOMUMWHEREDATA)
  List<Agendamento> findAgendamentosAtivosPorCategoria(@Param("nomeCategoria") String nomeCategoria,
                                                       @Param("agora") LocalDateTime agora);
  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true AND UPPER(a.terminalConteudo.terminal.categoria.nome) = UPPER(:nomeCategoria) " +
          " AND a.terminalConteudo.terminal.nrTerminal = :nrTerminal " + SQLCOMUMWHEREDATA)
  List<Agendamento> findAgendamentosAtivosPorCategoriaENumero(@Param("nomeCategoria") String nomeCategoria,
                                                              @Param("nrTerminal") Integer nrTerminal,
                                                              @Param("agora") LocalDateTime agora);

  @Query("SELECT a FROM Agendamento a WHERE a.ativo = true ORDER BY a.dataInicio ASC")
  List<Agendamento> findAllAgendamentosAtivos();

}
