package com.mercado.produtos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.produtos.dao.dto.ProdutoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdutoApiService {
    @Value("${app-mercado.host}")
    private String host;
    @Value("${app-mercado.port}")
    private String port;
    @Value("${app-mercado.path}")
    private String path;
    @Value("${app-mercado.endpoint-export}")
    private String endpoint;
    @Value("${app-mercado.produtos-endpoint}")
    private String produtos_endpoint;
    @Value("${app-mercado.produtos-endpoint-oferta}")
    private String produtos_endpoint_oferta;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<ProdutoDto> buscarProdutosRemoto(String nomeCategoria, int numero) {
        String url = host + ":" + port + path + endpoint + produtos_endpoint + "/" + nomeCategoria + "/" + numero;

        try {
            ProdutoDto[] produtosLidos = restTemplate.getForObject(url, ProdutoDto[].class);
            ObjectMapper mapper = new ObjectMapper();
            List<ProdutoDto> conteudos = Arrays.stream(produtosLidos)
                    .filter(produtoDto -> produtoDto.isConteudo())
                    .collect(Collectors.toList());
            List<ProdutoDto> lista;

            if(!conteudos.isEmpty())
                lista = conteudos;
            else
                lista = Arrays.asList(produtosLidos);

            return lista;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.out.println("Erro HTTP: " + ex.getStatusCode());
            System.out.println("Corpo retornado: " + ex.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<ProdutoDto> buscarProdutosOferta(Long conteudoId) {
        String url = host + ":" + port + path + endpoint + produtos_endpoint_oferta + "/" + conteudoId;

        try {
            ProdutoDto[] produtosLidos = restTemplate.getForObject(url, ProdutoDto[].class);
            ObjectMapper mapper = new ObjectMapper();
            List<ProdutoDto> conteudos = Arrays.stream(produtosLidos)
                    .filter(produtoDto -> produtoDto.isConteudo())
                    .collect(Collectors.toList());
            List<ProdutoDto> lista;

            if(!conteudos.isEmpty())
                lista = conteudos;
            else
                lista = Arrays.asList(produtosLidos);

            return lista;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.out.println("Erro HTTP: " + ex.getStatusCode());
            System.out.println("Corpo retornado: " + ex.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
