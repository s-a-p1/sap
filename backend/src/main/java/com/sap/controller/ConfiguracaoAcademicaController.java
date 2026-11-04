package com.sap.controller;

import com.sap.dto.ConfiguracaoAcademicaRequest;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.service.ConfiguracaoAcademicaService;
import com.sap.service.UsuarioAutenticadoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/configuracao-academica")
public class ConfiguracaoAcademicaController {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ConfiguracaoAcademicaService configuracaoService;
    private final ConfiguracaoAcademicaRepository configuracaoRepository;
    private final MatriculaDisciplinaRepository matriculaRepository;

    public ConfiguracaoAcademicaController(
            UsuarioAutenticadoService usuarioAutenticadoService,
            ConfiguracaoAcademicaService configuracaoService,
            ConfiguracaoAcademicaRepository configuracaoRepository,
            MatriculaDisciplinaRepository matriculaRepository) {

        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.configuracaoService = configuracaoService;
        this.configuracaoRepository = configuracaoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @GetMapping
    public ResponseEntity<?> consultar(Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        Optional<ConfiguracaoAcademica> configuracaoEncontrada =
                configuracaoRepository.findByUsuarioId(usuario.getId());

        if (configuracaoEncontrada.isEmpty()) {
            return ResponseEntity.ok(Map.of("configurado", false));
        }

        ConfiguracaoAcademica configuracao = configuracaoEncontrada.get();

        List<MatriculaDisciplina> matriculas =
                matriculaRepository.findByConfiguracaoAcademicaId(
                        configuracao.getId()
                );

        List<Long> ofertasIds = matriculas.stream()
                .map(matricula -> matricula.getOfertaDisciplina().getId())
                .toList();

        Map<String, Object> resposta = new LinkedHashMap<>();

        resposta.put("configurado", true);
        resposta.put("cursoId", configuracao.getCurso().getId());
        resposta.put("semestre", configuracao.getSemestre());
        resposta.put("ofertasDisciplinasIds", ofertasIds);

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            Authentication authentication,
            @RequestBody ConfiguracaoAcademicaRequest request) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        configuracaoService.salvar(usuarioEncontrado.get(), request);

        return ResponseEntity.ok(
                Map.of("mensagem", "Configuração acadêmica salva com sucesso.")
        );
    }
}