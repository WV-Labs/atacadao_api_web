package com.mercado.produtos.service;

import com.mercado.produtos.dao.model.Categoria;
import com.mercado.produtos.dao.model.Terminal;
import com.mercado.produtos.dao.repository.TerminalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TerminalService {
    @Autowired
    private TerminalRepository terminalRepository;

    public List<Terminal> findAll() {
        return terminalRepository.findAllByAtivoTrueOrderByNomeAscLocalizacaoAsc();
    }

    public Optional<Terminal> findById(Long id) {
        return terminalRepository.findById(id);
    }

    public List<Terminal> findByCategoria(Categoria categoria) {
        return terminalRepository.findByCategoria(categoria);
    }

    public List<Terminal> findByAtivo(Boolean ativo) {
        return terminalRepository.findByAtivo(ativo);
    }

    public Optional<Terminal> findByCategoriaAndNumero(Categoria categoria, Integer numero) {
        return terminalRepository.findByCategoriaAndNrTerminal(categoria, numero);
    }

    public Optional<Terminal> findByCategoriaNomeAndNumero(String nomeCategoria, Integer numero)  {
        return terminalRepository.findByNomeCategoriaAssociadoAndNrTerminal(nomeCategoria, numero);
    }

    public Terminal save(Terminal terminal, List<Long> produtosSelecionados) {
        Terminal save = terminalRepository.save(terminal);
        return save;
    }

    public void deleteById(long id) {
        Optional<Terminal> terminal = findById(id);
        terminalRepository.deleteById(id);
    }
}
