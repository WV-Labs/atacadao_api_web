package com.mercado.produtos.dao.repository;

import com.mercado.produtos.dao.model.Conteudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {
    String SQLCOMUM = "select c FROM Agendamento  a "+
            " inner join TerminalConteudo tc on a.terminalConteudo.id = tc.id " +
            " inner join Conteudo c on tc.conteudo.id = c.id " +
            " inner join Terminal t on tc.terminal.id = t.id ";
    String SQLCOMUMWHEREDATA = " AND a.dataInicio <= :agora AND a.dataFim >= :agora";

    @Query(SQLCOMUM + " WHERE a.ativo = true AND tc.terminal.id = :terminalId " + SQLCOMUMWHEREDATA)
    List<Conteudo> findConteudoAtivosPorTerminal(@Param("terminalId") Long terminalId,
                                                 @Param("agora") LocalDateTime agora);
}
