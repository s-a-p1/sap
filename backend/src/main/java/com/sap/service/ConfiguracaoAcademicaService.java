package com.sap.service;

import com.sap.dto.ConfiguracaoAcademicaRequest;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Curso;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.OfertaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.CursoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.OfertaDisciplinaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConfiguracaoAcademicaService {

    private final ConfiguracaoAcademicaRepository configuracaoRepository;
    private final CursoRepository cursoRepository;
    private final OfertaDisciplinaRepository ofertaRepository;
    private final MatriculaDisciplinaRepository matriculaRepository;

    public ConfiguracaoAcademicaService(
            ConfiguracaoAcademicaRepository configuracaoRepository,
            CursoRepository cursoRepository,
            OfertaDisciplinaRepository ofertaRepository,
            MatriculaDisciplinaRepository matriculaRepository) {

        this.configuracaoRepository = configuracaoRepository;
        this.cursoRepository = cursoRepository;
        this.ofertaRepository = ofertaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public void salvar(Usuario usuario, ConfiguracaoAcademicaRequest request) {

        if (usuario == null || !"ESTUDANTE".equalsIgnoreCase(usuario.getTipo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas a estudantes."
            );
        }

        if (request == null
                || request.cursoId() == null
                || request.semestre() == null
                || request.ofertasDisciplinasIds() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o curso, o semestre e as disciplinas."
            );
        }

        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Curso não encontrado."
                ));

        if (request.semestre() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Semestre inválido."
            );
        }

        List<OfertaDisciplina> ofertasDisponiveis =
                ofertaRepository.findByCursoIdAndSemestre(
                        curso.getId(),
                        request.semestre()
                );

        if (ofertasDisponiveis.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não existem disciplinas disponíveis para esse curso e semestre."
            );
        }

        List<Long> idsSelecionados = request.ofertasDisciplinasIds();

        if (idsSelecionados.isEmpty() || idsSelecionados.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione pelo menos uma disciplina válida."
            );
        }

        Set<Long> idsUnicos = new HashSet<>(idsSelecionados);

        if (idsUnicos.size() != idsSelecionados.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Existem disciplinas selecionadas mais de uma vez."
            );
        }

        Set<Long> idsDisponiveis = new HashSet<>();

        for (OfertaDisciplina oferta : ofertasDisponiveis) {
            idsDisponiveis.add(oferta.getId());
        }

        if (!idsDisponiveis.containsAll(idsUnicos)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uma ou mais disciplinas não pertencem ao curso e semestre selecionados."
            );
        }

        ConfiguracaoAcademica configuracao = configuracaoRepository
                .findByUsuarioId(usuario.getId())
                .orElseGet(ConfiguracaoAcademica::new);

        configuracao.setUsuario(usuario);
        configuracao.setCurso(curso);
        configuracao.setSemestre(request.semestre());

        configuracao = configuracaoRepository.save(configuracao);

        List<MatriculaDisciplina> matriculasAnteriores =
                matriculaRepository.findByConfiguracaoAcademicaId(
                        configuracao.getId()
                );

        matriculaRepository.deleteAll(matriculasAnteriores);
        matriculaRepository.flush();

        List<OfertaDisciplina> ofertasSelecionadas =
                ofertaRepository.findAllById(idsSelecionados);

        for (OfertaDisciplina oferta : ofertasSelecionadas) {
            MatriculaDisciplina matricula = new MatriculaDisciplina();
            matricula.setConfiguracaoAcademica(configuracao);
            matricula.setOfertaDisciplina(oferta);
            matriculaRepository.save(matricula);
        }
    }
}