package com.sap.service;

import com.sap.dto.LivroResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LivroService {

    private static final String OPEN_LIBRARY_URL =
            "https://openlibrary.org/search.json";

    public List<LivroResponse> buscarLivros(String busca) {

        if (busca == null || busca.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe um termo para buscar livros."
            );
        }

        URI uri = UriComponentsBuilder
                .fromUriString(OPEN_LIBRARY_URL)
                .queryParam("q", busca.trim())
                .queryParam("limit", 8)
                .build()
                .encode()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<
                                Map<String, Object>>() {
                        }
                );

        Map<String, Object> corpo = response.getBody();

        if (corpo == null) {
            return List.of();
        }

        Object documentosObjeto = corpo.get("docs");

        if (!(documentosObjeto instanceof List<?> documentos)) {
            return List.of();
        }

        List<LivroResponse> livros = new ArrayList<>();

        for (Object documentoObjeto : documentos) {

            if (!(documentoObjeto instanceof Map<?, ?> documento)) {
                continue;
            }

            String titulo = obterString(
                    documento.get("title")
            );

            String autor = obterPrimeiroValor(
                    documento.get("author_name")
            );

            Integer anoPublicacao = obterInteger(
                    documento.get("first_publish_year")
            );

            String chave = obterString(
                    documento.get("key")
            );

            livros.add(
                    new LivroResponse(
                            titulo,
                            autor,
                            anoPublicacao,
                            chave
                    )
            );
        }

        return livros;
    }

    private String obterString(Object valor) {

        if (valor == null) {
            return null;
        }

        return valor.toString();
    }

    private String obterPrimeiroValor(Object valor) {

        if (valor instanceof List<?> lista
                && !lista.isEmpty()
                && lista.get(0) != null) {

            return lista.get(0).toString();
        }

        return null;
    }

    private Integer obterInteger(Object valor) {

        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        return null;
    }
}