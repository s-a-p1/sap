
package com.sap.service;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.RespostaQuestaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.AnaliseDesempenho;
import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.DesempenhoAssunto;
import com.sap.model.Questao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CorrecaoAvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final ResultadoRepository resultadoRepository;
    private final DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private final ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private final MatriculaDisciplinaRepository matriculaDisciplinaRepository;
    private final AnaliseDesempenhoService analiseDesempenhoService;
    private final AuditoriaService auditoriaService;

    public CorrecaoAvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            QuestaoRepository questaoRepository,
            ResultadoRepository resultadoRepository,
            DesempenhoAssuntoRepository desempenhoAssuntoRepository,
            ConfiguracaoAcademicaRepository configuracaoAcademicaRepository,
            MatriculaDisciplinaRepository matriculaDisciplinaRepository,
            AnaliseDesempenhoService analiseDesempenhoService,
            AuditoriaService auditoriaService) {

        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoRepository = questaoRepository;
        this.resultadoRepository = resultadoRepository;
        this.desempenhoAssuntoRepository = desempenhoAssuntoRepository;
        this.configuracaoAcademicaRepository = configuracaoAcademicaRepository;
        this.matriculaDisciplinaRepository = matriculaDisciplinaRepository;
        this.analiseDesempenhoService = analiseDesempenhoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ResultadoAvaliacaoResponse corrigir(
            FinalizarAvaliacaoRequest request,
            Usuario usuario) {

        if (request == null || request.getAvaliacaoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma avaliação válida."
            );
        }

        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado."
            );
        }

        Avaliacao avaliacao = avaliacaoRepository
                .findById(request.getAvaliacaoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Avaliação não encontrada."
                ));

        if (avaliacao.getAssunto() == null ||
                avaliacao.getAssunto().getDisciplina() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A avaliação não possui uma disciplina válida."
            );
        }

        ConfiguracaoAcademica configuracao =
                configuracaoAcademicaRepository
                        .findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Configure sua matrícula antes de realizar avaliações."
                        ));

        Long disciplinaId = avaliacao.getAssunto()
                .getDisciplina().getId();

        boolean matriculado = matriculaDisciplinaRepository
                .existsByConfiguracaoAcademica_Usuario_IdAndOfertaDisciplina_Disciplina_IdAndOfertaDisciplina_Curso_IdAndOfertaDisciplina_Semestre(
                        usuario.getId(),
                        disciplinaId,
                        configuracao.getCurso().getId(),
                        configuracao.getSemestre()
                );

        if (!matriculado) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não está matriculado na disciplina desta avaliação."
            );
        }

        List<Questao> questoes = questaoRepository
                .findByAvaliacao_Id(request.getAvaliacaoId());

        if (questoes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A avaliação não possui questões cadastradas."
            );
        }

        Map<Long, Questao> questoesPorId = new HashMap<>();

        for (Questao questao : questoes) {
            questoesPorId.put(questao.getId(), questao);
        }

        Map<Long, String> respostasPorQuestao = new HashMap<>();
        Set<Long> questoesRespondidas = new HashSet<>();

        if (request.getRespostas() != null) {
            for (RespostaQuestaoRequest resposta : request.getRespostas()) {

                if (resposta == null || resposta.getQuestaoId() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Resposta com questão inválida."
                    );
                }

                Long questaoId = resposta.getQuestaoId();

                if (!questoesPorId.containsKey(questaoId)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Questão não pertence à avaliação."
                    );
                }

                if (!questoesRespondidas.add(questaoId)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Uma questão foi respondida mais de uma vez."
                    );
                }

                String alternativa = resposta.getResposta();

                if (alternativa != null && !alternativa.isBlank()) {
                    alternativa = alternativa.trim().toUpperCase();

                    if (!Set.of("A", "B", "C", "D").contains(alternativa)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Alternativa inválida."
                        );
                    }

                    respostasPorQuestao.put(questaoId, alternativa);
                }
            }
        }

        int totalQuestoes = questoes.size();
        int acertos = 0;

        Map<Long, int[]> contagemPorAssunto = new HashMap<>();
        Map<Long, Assunto> assuntosPorId = new HashMap<>();

        for (Questao questao : questoes) {

            Assunto assunto = questao.getAssunto();

            if (assunto == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Existem questões sem assunto associado."
                );
            }

            if (!assunto.getDisciplina().getId().equals(disciplinaId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Uma questão possui assunto de outra disciplina."
                );
            }

            Long assuntoId = assunto.getId();

            assuntosPorId.put(assuntoId, assunto);

            int[] contagem = contagemPorAssunto.computeIfAbsent(
                    assuntoId,
                    id -> new int[2]
            );

            contagem[0]++;

            String resposta = respostasPorQuestao.get(questao.getId());

            if (resposta != null &&
                    resposta.equalsIgnoreCase(questao.getRespostaCorreta())) {
                acertos++;
                contagem[1]++;
            }
        }

        int erros = totalQuestoes - acertos;

        BigDecimal percentual = calcularPercentual(
                acertos,
                totalQuestoes
        );

        Resultado resultado = new Resultado(
                usuario.getId(),
                avaliacao,
                totalQuestoes,
                acertos,
                erros,
                percentual,
                LocalDateTime.now()
        );

        resultado = resultadoRepository.save(resultado);

        for (Map.Entry<Long, int[]> entrada : contagemPorAssunto.entrySet()) {

            Long assuntoId = entrada.getKey();
            int[] contagem = entrada.getValue();

            int totalAssunto = contagem[0];
            int acertosAssunto = contagem[1];
            int errosAssunto = totalAssunto - acertosAssunto;

            DesempenhoAssunto desempenho = new DesempenhoAssunto(
                    resultado,
                    assuntosPorId.get(assuntoId),
                    totalAssunto,
                    acertosAssunto,
                    errosAssunto,
                    calcularPercentual(acertosAssunto, totalAssunto)
            );

            desempenhoAssuntoRepository.save(desempenho);
        }

        AnaliseDesempenho analise =
                analiseDesempenhoService.analisarResultado(resultado);

        auditoriaService.registrar(
                usuario.getId(),
                usuario.getEmail(),
                "FINALIZOU_AVALIACAO",
                "Avaliação " + avaliacao.getId()
                        + " finalizada. Resultado: "
                        + percentual + "%."
        );

        return new ResultadoAvaliacaoResponse(
                resultado.getId(),
                totalQuestoes,
                acertos,
                erros,
                percentual,
                analise.getDificuldade(),
                analise.getPrioridade(),
                analise.getRecomendacao()
        );
    }

    private BigDecimal calcularPercentual(int acertos, int totalQuestoes) {
        return BigDecimal.valueOf(acertos)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalQuestoes),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
