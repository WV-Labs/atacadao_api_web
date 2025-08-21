package com.mercado.produtos.dao.repository;

import com.mercado.produtos.dao.model.Categoria;
import com.mercado.produtos.dao.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
  List<Terminal> findAllByAtivoTrueOrderByNomeAscLocalizacaoAsc();

  List<Terminal> findByCategoria(Categoria categoria);

  List<Terminal> findByAtivo(Boolean ativo);

  Optional<Terminal> findByCategoriaAndNrTerminal(Categoria categoria, Integer nrTerminal);

  @Query(
      "SELECT t FROM Terminal t INNER JOIN TerminalConteudo tc ON t.id = tc.terminal.id" +
              " WHERE t.categoria.id IN (SELECT c.id FROM Categoria c WHERE UPPER(c.nome) = UPPER( ?1 )) " +
              " AND t.nrTerminal = ?2")
  Optional<Terminal> findByNomeCategoriaAssociadoAndNrTerminal(
      String categoriaNome, Integer nrTerminal);
}
