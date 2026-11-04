
package com.sap.controller;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.sap.model.DesempenhoAssunto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resultados")

public class ResultadoController {

    private final ResultadoRepository resultadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DesempenhoAssuntoRepository desempenhoAssuntoRepository;



public ResultadoController(
        ResultadoRepository resultadoRepository,
        UsuarioRepository usuarioRepository,
        DesempenhoAssuntoRepository desempenhoAssuntoRepository) {

    this.resultadoRepository = resultadoRepository;
    this.usuarioRepository = usuarioRepository;
    this.desempenhoAssuntoRepository = desempenhoAssuntoRepository;
}


    @GetMapping("/meu-historico")
    public ResponseEntity<?> meuHistorico(Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(
                String.valueOf(usuario.getTipo()))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        List<Resultado> resultados = resultadoRepository
                .findByUsuarioIdOrderByDataRealizacaoDesc(usuario.getId());

        List<Map<String, Object>> historico = resultados.stream()
                .map(resultado -> Map.<String, Object>of(
                        "resultadoId", resultado.getId(),
                        "avaliacaoId", resultado.getAvaliacao().getId(),
                        "disciplinaId", resultado.getAvaliacao().getAssunto().getDisciplina().getId(),
                        "disciplinaNome", resultado.getAvaliacao().getAssunto().getDisciplina().getNome(),
                        "totalQuestoes", resultado.getTotalQuestoes(),
                        "acertos", resultado.getAcertos(),
                        "erros", resultado.getErros(),
                        "percentual", resultado.getPercentual(),
                        "dataRealizacao", resultado.getDataRealizacao()
                ))
                .toList();

        return ResponseEntity.ok(historico);
    }


@GetMapping("/meu-desempenho-assuntos")
public ResponseEntity<?> meuDesempenhoAssuntos(
        Authentication authentication) {

    Optional<Usuario> usuarioEncontrado =
            buscarUsuarioAutenticado(authentication);

    if (usuarioEncontrado.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Usuário não autenticado."));
    }

    Usuario usuario = usuarioEncontrado.get();

    if (!"ESTUDANTE".equalsIgnoreCase(
            String.valueOf(usuario.getTipo()))) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
    }

    List<DesempenhoAssunto> desempenhos =
            desempenhoAssuntoRepository
                    .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(
                            usuario.getId()
                    );

    List<Map<String, Object>> resposta = new ArrayList<>();

    for (DesempenhoAssunto desempenho : desempenhos) {

        BigDecimal percentual = desempenho.getPercentual();

        String prioridade;

        if (percentual.compareTo(BigDecimal.valueOf(50)) < 0) {
            prioridade = "ALTA";
        } else if (percentual.compareTo(BigDecimal.valueOf(70)) < 0) {
            prioridade = "MEDIA";
        } else {
            prioridade = "SEM_PRIORIDADE";
        }

        Map<String, Object> item = new LinkedHashMap<>();

        item.put("resultadoId", desempenho.getResultado().getId());
        item.put("avaliacaoId", desempenho.getResultado().getAvaliacao().getId());
        item.put("assuntoId", desempenho.getAssunto().getId());
        item.put("assuntoNome", desempenho.getAssunto().getNome());
        item.put("disciplinaId", desempenho.getAssunto().getDisciplina().getId());
        item.put("disciplinaNome", desempenho.getAssunto().getDisciplina().getNome());
        item.put("totalQuestoes", desempenho.getTotalQuestoes());
        item.put("acertos", desempenho.getAcertos());
        item.put("erros", desempenho.getErros());
        item.put("percentual", percentual);
        item.put("prioridade", prioridade);
        item.put("dataRealizacao", desempenho.getResultado().getDataRealizacao());

        resposta.add(item);
    }

    return ResponseEntity.ok(resposta);
}

    private Optional<Usuario> buscarUsuarioAutenticado(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

            String registrationId =
                    oauthToken.getAuthorizedClientRegistrationId();

            OAuth2User oauth2User = oauthToken.getPrincipal();

            if ("github".equalsIgnoreCase(registrationId)) {

                Object id = oauth2User.getAttribute("id");

                if (id == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GITHUB,
                        id.toString()
                );
            }

            if ("google".equalsIgnoreCase(registrationId)) {

                Object sub = oauth2User.getAttribute("sub");

                if (sub == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GOOGLE,
                        sub.toString()
                );
            }

            return Optional.empty();
        }

        return usuarioRepository.findByEmail(authentication.getName());
    }
}
