package com.mercado.produtos.service;

import com.mercado.produtos.dao.model.Categoria;
import com.mercado.produtos.dao.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAllByOrderByNomeAscDescricaoAsc();
    }

    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Optional<Categoria> findByNome(String nome) {
        return categoriaRepository.findByNomeEqualsIgnoreCase(nome);
    }

    public Categoria save(Categoria categoria) {
        Optional<Categoria> categoriaFind = categoriaRepository.findByNomeEqualsIgnoreCase(categoria.getNome());
        if (categoriaFind.isPresent()) {
            return null;
        }
        return categoriaRepository.save(categoria);
    }

    public void deleteById(long id) {
        categoriaRepository.deleteById(id);
    }

    public boolean existsByNome(String nome) {
        return categoriaRepository.existsByNome(nome);
    }
}
