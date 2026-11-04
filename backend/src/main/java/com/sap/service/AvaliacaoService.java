package com.sap.service;

import com.sap.model.Avaliacao;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ConfiguracaoAcademicaRepository configuracaoRepository;
    private final MatriculaDisciplinaRepository matriculaRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ConfiguracaoAcademicaRepository configuracaoRepository,
            MatriculaDisciplinaRepository matriculaRepository) {

        this.avaliacaoRepository = avaliacaoRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAll();
    }

    public List<Avaliacao> listarDoEstudante(Usuario usuario) {
        Optional<ConfiguracaoAcademica> configuracaoEncontrada =
                configuracaoRepository.findByUsuarioId(usuario.getId());

        if (configuracaoEncontrada.isEmpty()) {
            return List.of();
        }

        ConfiguracaoAcademica configuracao = configuracaoEncontrada.get();

        List<MatriculaDisciplina> matriculas =
                matriculaRepository.findByConfiguracaoAcademicaId(configuracao.getId());

        List<Avaliacao> avaliacoes = new ArrayList<>();

        for (MatriculaDisciplina matricula : matriculas) {
            Long disciplinaId = matricula.getOfertaDisciplina().getDisciplina().getId();

            avaliacoes.addAll(
                    avaliacaoRepository.findByAssuntoDisciplinaId(disciplinaId)
            );
        }

        return avaliacoes;
    }
}