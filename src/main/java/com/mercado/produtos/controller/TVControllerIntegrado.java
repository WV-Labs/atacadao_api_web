package com.mercado.produtos.controller;

import com.mercado.produtos.dao.dto.ProdutoDto;
import com.mercado.produtos.dao.model.Conteudo;
import com.mercado.produtos.service.ProdutoApiService;
import com.mercado.produtos.service.TVService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class TVControllerIntegrado {

    @Autowired
    private TVService tvService;

    @Autowired
    private ProdutoApiService produtoApiService; // Usa seu serviço existente

    @Value("${api-tv.tv.verificacao.intervalo}")
    private int intervaloVerificacao;
    @Value("${api-tv.tv.base.url}")
    private String baseUrl;
    @Value("${api-tv.serverIP}")
    private String serverIP;
    @Value("${api-tv.tv.qtdeLinhas}")
    private int qtdeLinhas;
    @Value("${app-tv.uploads}")
    private String uploadDir;
    @Value("${api-tv.debugDisplay}")
    private String debugDisplay;

    @GetMapping("/")
    public String index(Model model) {
        return this.index("Mercearia", 1, model);
    }

    /**
     * Página de standby/configuração da TV - COM CONFIGURAÇÕES
     */
    @GetMapping("/tv")
    public String paginaTV(Model model) {
        // Passa configurações do application.properties para o template
        model.addAttribute("intervaloVerificacao", intervaloVerificacao);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("serverIP", serverIP);
        model.addAttribute("debugDisplay", debugDisplay);
        model.addAttribute("qtdeLinhas", qtdeLinhas);
        return "tv-sistema";
    }

    /**
     * Página de conteúdo da TV com lógica de priorização
     * Esta URL substitui temporariamente seu endpoint original
     */
    @GetMapping("/interno/{categoria}/{numero}/{idAgendamentoPrioritario}")
    public @ResponseBody List<ProdutoDto> indexAjax(@PathVariable String categoria, @PathVariable Integer numero, @PathVariable Long idAgendamentoPrioritario, Model model) {
        log.info("🔍 Verificando agendamentos para categoria '{}' da TV '{}'", categoria, numero);

        try {
            List<Conteudo> conteudos = getConteudos(categoria, numero, idAgendamentoPrioritario);
            if (conteudos.isEmpty()) {
                log.info("📭 Nenhum agendamento ativo para {}/{} - redirecionando para standby", categoria, numero);
                return null;
            }
            return getListaProdutos(categoria, numero, model, conteudos);

        } catch (Exception e) {
            log.error("❌ Erro ao verificar agendamentos para {}/{}: {}", categoria, numero, e.getMessage());

            // Em caso de erro, redireciona para standby
            return null;
        }
    }

    /**
     * Página de conteúdo da TV com lógica de priorização
     * Esta URL substitui temporariamente seu endpoint original
     */
    @GetMapping("/{categoria}/{numero}")
    public String index(@PathVariable String categoria, @PathVariable Integer numero, Model model) {
        log.info("🔍 Verificando agendamentos para categoria '{}' da TV '{}'", categoria, numero);

        try {
            List<Conteudo> conteudos = getConteudos(categoria, numero);
            if (conteudos.isEmpty()) {
                log.info("📭 Nenhum agendamento ativo para {}/{} - redirecionando para standby", categoria, numero);
                return "redirect:/tv?categoria=" + categoria + "&numero=" + numero;
            }
            getListaProdutos(categoria, numero, model, conteudos);
            return "index";

        } catch (Exception e) {
            log.error("❌ Erro ao verificar agendamentos para {}/{}: {}", categoria, numero, e.getMessage());

            // Em caso de erro, redireciona para standby
            return "redirect:/tv?categoria=" + categoria + "&numero=" + numero;
        }
    }

    private List<ProdutoDto> getListaProdutos(String categoria, Integer numero, Model model, List<Conteudo> conteudos) {
        // Pega o conteúdo prioritário (primeiro da lista já vem priorizado)
        Conteudo conteudoPrioritario = conteudos.get(0);
        log.info("🎯 Conteúdo prioritário encontrado: {} (Tipo: {})",
                conteudoPrioritario.getTitulo(), conteudoPrioritario.getTipoConteudo());

        // Busca informações de status completo para o template
        Map<String, Object> statusCompleto = tvService.getStatusCompleto(categoria, numero);

        // Prepara model baseado no tipo de conteúdo
        List<ProdutoDto> produtoDtos = prepararModelParaConteudo(model, conteudoPrioritario, categoria, numero);

        // Adiciona informações de status e priorização
        model.addAttribute("agendamentosAtivos", statusCompleto.get("agendamentosAtivos"));
        model.addAttribute("deveExibirConteudo", statusCompleto.get("deveExibirConteudo"));

        // Adiciona configurações básicas
        model.addAttribute("intervaloVerificacao", intervaloVerificacao);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("serverIP", serverIP);
        model.addAttribute("debugDisplay", debugDisplay);
        model.addAttribute("categoria", categoria);
        model.addAttribute("numero", numero);
        model.addAttribute("qtdeLinhas", qtdeLinhas);

        // Log adicional para debugging
        log.info("📊 Status: agendamentosAtivos={}, categoria={}, produtos={}",
                statusCompleto.get("agendamentosAtivos"), categoria,
                model.getAttribute("produtos") != null ?
                        ((List<?>) model.getAttribute("produtos")).size() : 0);
        return produtoDtos;
    }

    private List<Conteudo> getConteudos(String categoria, Integer numero) {
        return getConteudos(categoria, numero, 0L);
    }

    private List<Conteudo> getConteudos(String categoria, Integer numero, Long idAgendamentoPrioritario) {
        // Registra que a TV está verificando o sistema
        tvService.registrarVerificacaoTV(categoria, numero);

        // Verifica se deve exibir conteúdo agendado (COM LÓGICA DE PRIORIZAÇÃO)
        List<Conteudo> conteudos = tvService.exibirConteudo(categoria, numero, idAgendamentoPrioritario);
        return conteudos;
    }

    private List<ProdutoDto> prepararModelParaConteudo(Model model, Conteudo conteudo, String categoria, Integer numero) {
        List<ProdutoDto> produtoDtos = new ArrayList<>();

        // Tipo 3 = conteúdo de produtos (busca na API)
        if (conteudo.getTipoConteudo() == 3) {
            try {
                // *** IMPORTANTE: Busca produtos para a categoria CORRETA ***
                produtoDtos = produtoApiService.buscarProdutosRemoto(categoria, numero);

                if (!produtoDtos.isEmpty()) {
                    ProdutoDto primeiro = produtoDtos.get(0);
                    model.addAttribute("descCategoria", primeiro.getDescricaoCategoria());
                    model.addAttribute("tipoConteudo", primeiro.getTipoConteudo());
                    model.addAttribute("caminhoCompletoArquivo",
                            primeiro.getCaminhoImagemVideo() != null ? primeiro.getCaminhoImagemVideo() : "");

                    log.info("📦 Produtos carregados: {} itens para {}/{}", produtoDtos.size(), categoria, numero);
                } else {
                    // Sem produtos, usa dados do conteúdo mas com categoria correta
                    model.addAttribute("descCategoria", categoria.toUpperCase());
                    model.addAttribute("tipoConteudo", conteudo.getTipoConteudo());
                    model.addAttribute("caminhoCompletoArquivo", conteudo.getNomeMidia() != null ? conteudo.getNomeMidia() : "");

                    log.warn("⚠️ Nenhum produto encontrado para {}/{}, usando dados padrão", categoria, numero);
                }
            } catch (Exception e) {
                log.warn("⚠️ Erro ao buscar produtos para {}/{}: {}", categoria, numero, e.getMessage());

                // Fallback para dados do conteúdo com categoria correta
                model.addAttribute("descCategoria", categoria.toUpperCase());
                model.addAttribute("tipoConteudo", conteudo.getTipoConteudo());
                model.addAttribute("caminhoCompletoArquivo", conteudo.getNomeMidia() != null ? conteudo.getNomeMidia() : "");
            }
        } else {
            // Outros tipos de conteúdo (vídeo, imagem, etc.)
            model.addAttribute("descCategoria", categoria.toUpperCase());
            model.addAttribute("tipoConteudo", conteudo.getTipoConteudo());
            model.addAttribute("caminhoCompletoArquivo", uploadDir + conteudo.getNomeMidia());
            ProdutoDto produtoDto = new ProdutoDto();
            produtoDto.setCaminhoImagemVideo(uploadDir + conteudo.getNomeMidia());
            produtoDto.setTipoConteudo(conteudo.getTipoConteudo());
            produtoDto.setDescricaoCategoria(categoria.toUpperCase());
            produtoDtos.add(produtoDto);
            log.info("🎬 Conteúdo de mídia: {} (Tipo: {})", conteudo.getTitulo(), conteudo.getTipoConteudo());
        }

        for (ProdutoDto p : produtoDtos) {
            p.setIdAgendamentoPrioritario(conteudo.getIdAgendamentoPrioritario());
        }

        // Adiciona dados básicos do conteúdo
        model.addAttribute("produtos", produtoDtos);
        model.addAttribute("conteudoTitulo", conteudo.getTitulo());
        model.addAttribute("conteudoDescricao", conteudo.getDescricao());
        model.addAttribute("conteudoId", conteudo.getId());

        // *** GARANTE QUE A CATEGORIA SEJA SEMPRE A CORRETA ***
        model.addAttribute("categoria", categoria);
        model.addAttribute("numero", numero);
        return produtoDtos;
    }

    /**
     * API para detectar configuração da TV baseada na URL
     */
    @GetMapping("/api/tv/detectar/{categoria}/{numero}")
    public String detectarTV(@PathVariable String categoria, @PathVariable Integer numero) {

        return "redirect:/tv?categoria=" + categoria + "&numero=" + numero;
    }

    /**
     * API que retorna os produtos (integra com seu ProdutoService)
     * Esta é a URL que o JavaScript da TV vai chamar
     */
    @GetMapping("/api-tv/{categoria}/{numero}")
    @ResponseBody
    public ResponseEntity<List<ProdutoDto>> getProdutosParaTV(@PathVariable String categoria, @PathVariable Integer numero) {
        try {
            log.info("🔌 API chamada para produtos: {}/{}", categoria, numero);

            // Registra verificação da TV
            List<Conteudo> conteudosAtivos = getConteudos(categoria, numero);

            if (conteudosAtivos.isEmpty()) {
                log.info("📭 Nenhum agendamento ativo para API {}/{}", categoria, numero);
                return ResponseEntity.ok(List.of());
            }

            Conteudo conteudoAtivo = conteudosAtivos.get(0);

            // Se é conteúdo tipo 3 (produtos), busca na API PARA A CATEGORIA CORRETA
            if (conteudoAtivo.getTipoConteudo() == 3) {
                // *** BUSCA PRODUTOS SEMPRE PARA A CATEGORIA DA URL ***
                List<ProdutoDto> produtoDtos = produtoApiService.buscarProdutosRemoto(categoria, numero);

                log.info("📦 API retornando {} produtos para {}/{}", produtoDtos.size(), categoria, numero);

                if (!produtoDtos.isEmpty()) {
                    // Verifica se os produtos são realmente da categoria correta
                    long produtosCategoriaCorreta = produtoDtos.stream()
                            .filter(p -> p.getDescricaoCategoria() != null &&
                                    p.getDescricaoCategoria().toLowerCase().contains(categoria.toLowerCase()))
                            .count();

                    if (produtosCategoriaCorreta == 0) {
                        log.warn("⚠️ Produtos retornados não são da categoria '{}', filtrando...", categoria);
                        // Se nenhum produto é da categoria correta, pode ser problema na API
                        // Mas retorna os produtos mesmo assim para não quebrar a exibição
                    }
                }

                return ResponseEntity.ok(produtoDtos);
            } else {
                // Para outros tipos, retorna lista vazia (conteúdo é mídia, não produtos)
                log.info("🎬 Conteúdo não é de produtos (tipo {}), retornando lista vazia", conteudoAtivo.getTipoConteudo());
                return ResponseEntity.ok(List.of());
            }

        } catch (Exception e) {
            log.error("❌ Erro na API de produtos para {}/{}: {}", categoria, numero, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }


    /**
     * NOVO ENDPOINT: Debug para verificar status completo de uma TV
     */
    @GetMapping("/debug/{categoria}/{numero}")
    @ResponseBody
    public ResponseEntity<Object> debugTV(@PathVariable String categoria, @PathVariable Integer numero) {
        try {
            log.info("🔍 Debug solicitado para {}/{}", categoria, numero);

            // Executa debug completo
            tvService.debugCompleto(categoria, numero);

            // Retorna status completo
            return ResponseEntity.ok(tvService.getStatusCompleto(categoria, numero));

        } catch (Exception e) {
            log.error("❌ Erro no debug para {}/{}: {}", categoria, numero, e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "erro", e.getMessage(),
                    "categoria", categoria,
                    "numero", numero
            ));
        }
    }
}
