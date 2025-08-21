package com.mercado.produtos.dao.repository;

import com.mercado.produtos.dao.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNomeAscDescricaoAsc();
    Optional<Categoria> findByNome(String nome);
    Optional<Categoria> findByNomeEqualsIgnoreCase(String nome);
    boolean existsByNome(String nome);
}
