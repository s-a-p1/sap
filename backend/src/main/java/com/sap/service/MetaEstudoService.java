
package com.sap.service;

import com.sap.model.Assunto;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.DesempenhoAssunto;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.MetaEstudo;
import com.sap.model.Resultado;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.MetaEstudoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MetaEstudoService {

    private final MetaEstudoRepository metaEstudoRepository;
    private final DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private final ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private final MatriculaDisciplinaRepository matriculaDisciplinaRepository;

    public MetaEstudoService(
            MetaEstudoRepository metaEstudoRepository,
            DesempenhoAssuntoRepository desempenhoAssuntoRepository,
            ConfiguracaoAcademicaRepository configuracaoAcademicaRepository,
            MatriculaDisciplinaRepository matriculaDisciplinaRepository) {

        this.metaEstudoRepository = metaEstudoRepository;
        this.desempenhoAssuntoRepository = desempenhoAssuntoRepository;
        this.configuracaoAcademicaRepository = configuracaoAcademicaRepository;
        this.matriculaDisciplinaRepository = matriculaDisciplinaRepository;
    }

    @Transactional
    public void gerarMetasAutomaticamente(Long usuarioId) {

        if (usuarioId == null) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }

        Set<Long> disciplinasMatriculadas = buscarDisciplinasMatriculadas(usuarioId);

        if (disciplinasMatriculadas.isEmpty()) {
            return;
        }

        List<DesempenhoAssunto> desempenhos =
                desempenhoAssuntoRepository
                        .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(usuarioId);

        Set<Long> assuntosProcessados = new HashSet<>();

        for (DesempenhoAssunto desempenho : desempenhos) {

            Assunto assunto = desempenho.getAssunto();

            if (assunto == null || assunto.getId() == null
                    || assunto.getDisciplina() == null) {
                continue;
            }

            Long disciplinaId = assunto.getDisciplina().getId();

            if (!disciplinasMatriculadas.contains(disciplinaId)) {
                continue;
            }

            Long assuntoId = assunto.getId();

            if (!assuntosProcessados.add(assuntoId)) {
                continue;
            }

            Resultado resultadoAtual = desempenho.getResultado();

            Optional<MetaEstudo> metaExistente =
                    metaEstudoRepository
                            .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(
                                    usuarioId,
                                    assuntoId
                            );

            double percentual = desempenho.getPercentual().doubleValue();

            String prioridade;
            String titulo;
            int diasPrazo;

            if (percentual < 50) {
                prioridade = "ALTA";
                titulo = "Revisar fundamentos de " + assunto.getNome();
                diasPrazo = 2;
            } else if (percentual < 70) {
                prioridade = "MEDIA";
                titulo = "Reforçar conhecimentos em " + assunto.getNome();
                diasPrazo = 4;
            } else {
                prioridade = "BAIXA";
                titulo = "Aprofundar conhecimentos em " + assunto.getNome();
                diasPrazo = 7;
            }

            if (metaExistente.isPresent()) {

                MetaEstudo meta = metaExistente.get();

                if (meta.getResultadoOrigem() != null
                        && meta.getResultadoOrigem().getId()
                                .equals(resultadoAtual.getId())) {
                    continue;
                }

                if (!Boolean.TRUE.equals(meta.getConcluida())) {

                    meta.setTitulo(titulo);
                    meta.setPrioridade(prioridade);
                    meta.setDataPrevista(LocalDate.now().plusDays(diasPrazo));
                    meta.setResultadoOrigem(resultadoAtual);

                    metaEstudoRepository.save(meta);
                    continue;
                }

                if (meta.getResultadoOrigem() == null) {
                    continue;
                }

                if (!resultadoAtual.getDataRealizacao()
                        .isAfter(meta.getResultadoOrigem().getDataRealizacao())) {
                    continue;
                }
            }

            MetaEstudo novaMeta = new MetaEstudo();

            novaMeta.setUsuarioId(usuarioId);
            novaMeta.setAssunto(assunto);
            novaMeta.setResultadoOrigem(resultadoAtual);
            novaMeta.setTitulo(titulo);
            novaMeta.setPrioridade(prioridade);
            novaMeta.setDataPrevista(LocalDate.now().plusDays(diasPrazo));
            novaMeta.setConcluida(false);
            novaMeta.setOrigem("AUTOMATICA");

            metaEstudoRepository.save(novaMeta);
        }
    }

    @Transactional(readOnly = true)
    public List<MetaEstudo> listarMetas(Long usuarioId) {

        if (usuarioId == null) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }

        Set<Long> disciplinasMatriculadas = buscarDisciplinasMatriculadas(usuarioId);

        if (disciplinasMatriculadas.isEmpty()) {
            return List.of();
        }

        return metaEstudoRepository
                .findByUsuarioIdOrderByDataPrevistaAsc(usuarioId)
                .stream()
                .filter(meta -> disciplinasMatriculadas.contains(
                        meta.getAssunto().getDisciplina().getId()
                ))
                .toList();
    }

    @Transactional
    public MetaEstudo atualizarConclusao(
            Long metaId,
            Long usuarioId,
            Boolean concluida) {

        if (metaId == null || usuarioId == null || concluida == null) {
            throw new IllegalArgumentException("Os dados da meta são obrigatórios.");
        }

        MetaEstudo meta = metaEstudoRepository.findById(metaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Meta não encontrada.")
                );

        if (!meta.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Esta meta não pertence ao usuário.");
        }

        Set<Long> disciplinasMatriculadas = buscarDisciplinasMatriculadas(usuarioId);

        if (!disciplinasMatriculadas.contains(
                meta.getAssunto().getDisciplina().getId())) {
            throw new IllegalArgumentException(
                    "Esta meta não pertence à matrícula acadêmica atual."
            );
        }

        boolean estavaConcluida = Boolean.TRUE.equals(meta.getConcluida());

meta.setConcluida(concluida);

MetaEstudo metaAtualizada = metaEstudoRepository.save(meta);

if (Boolean.TRUE.equals(concluida)
        && !estavaConcluida
        && "AUTOMATICA".equals(meta.getOrigem())
        && meta.getResultadoOrigem() != null) {

    String assuntoNome = meta.getAssunto().getNome();
    String tituloAtual = meta.getTitulo();

    String proximoTitulo = null;
    int diasPrazo = 0;

    if (tituloAtual.startsWith("Revisar fundamentos de ")
            || tituloAtual.startsWith("Reforçar conhecimentos em ")
            || tituloAtual.startsWith("Aprofundar conhecimentos em ")) {

        proximoTitulo = "Praticar exercícios de " + assuntoNome;
        diasPrazo = 3;

    } else if (tituloAtual.startsWith("Praticar exercícios de ")) {

        proximoTitulo = "Realizar nova avaliação de " + assuntoNome;
        diasPrazo = 5;
    }

    if (proximoTitulo != null) {

        MetaEstudo proximaMeta = new MetaEstudo();

        proximaMeta.setUsuarioId(usuarioId);
        proximaMeta.setAssunto(meta.getAssunto());
        proximaMeta.setResultadoOrigem(meta.getResultadoOrigem());
        proximaMeta.setTitulo(proximoTitulo);
        proximaMeta.setPrioridade(meta.getPrioridade());
        proximaMeta.setDataPrevista(LocalDate.now().plusDays(diasPrazo));
        proximaMeta.setConcluida(false);
        proximaMeta.setOrigem("AUTOMATICA");

        metaEstudoRepository.save(proximaMeta);
    }
}

return metaAtualizada;
    }

    private Set<Long> buscarDisciplinasMatriculadas(Long usuarioId) {

        Optional<ConfiguracaoAcademica> configuracao =
                configuracaoAcademicaRepository.findByUsuarioId(usuarioId);

        if (configuracao.isEmpty()) {
            return Set.of();
        }

        ConfiguracaoAcademica configuracaoAtual = configuracao.get();

        List<MatriculaDisciplina> matriculas =
                matriculaDisciplinaRepository
                        .findByConfiguracaoAcademicaId(configuracaoAtual.getId());

        return matriculas.stream()
                .filter(matricula ->
                        matricula.getOfertaDisciplina().getCurso().getId()
                                .equals(configuracaoAtual.getCurso().getId())
                        && matricula.getOfertaDisciplina().getSemestre()
                                .equals(configuracaoAtual.getSemestre())
                )
                .map(matricula ->
                        matricula.getOfertaDisciplina().getDisciplina().getId()
                )
                .collect(Collectors.toSet());
    }
}
