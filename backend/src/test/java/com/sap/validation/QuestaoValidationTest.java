
package com.sap.validation;

import com.sap.model.Avaliacao;
import com.sap.model.Questao;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestaoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        validatorFactory.close();
    }

    private Questao criarQuestaoValida() {
        Avaliacao avaliacao = new Avaliacao();

        return new Questao(
                "Quanto é 2 + 2?",
                "2",
                "3",
                "4",
                "5",
                "C",
                avaliacao
        );
    }

    @Test
    void deveRejeitarEnunciadoVazio() {
        Questao questao = criarQuestaoValida();
        questao.setEnunciado("");

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("enunciado")));
    }

    @Test
    void deveRejeitarEnunciadoMaiorQueCincoMilCaracteres() {
        Questao questao = criarQuestaoValida();
        questao.setEnunciado("A".repeat(5001));

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("enunciado")));
    }

    @Test
    void deveRejeitarAlternativaAVazia() {
        Questao questao = criarQuestaoValida();
        questao.setAlternativaA("");

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("alternativaA")));
    }

    @Test
    void deveRejeitarAlternativaMaiorQueDuzentosECinquentaECincoCaracteres() {
        Questao questao = criarQuestaoValida();
        questao.setAlternativaB("A".repeat(256));

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("alternativaB")));
    }

    @Test
    void deveRejeitarRespostaCorretaVazia() {
        Questao questao = criarQuestaoValida();
        questao.setRespostaCorreta("");

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("respostaCorreta")));
    }

    @Test
    void deveRejeitarRespostaCorretaInvalida() {
        Questao questao = criarQuestaoValida();
        questao.setRespostaCorreta("E");

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("respostaCorreta")));
    }

    @Test
    void deveRejeitarQuestaoSemAvaliacao() {
        Questao questao = criarQuestaoValida();
        questao.setAvaliacao(null);

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("avaliacao")));
    }

    @Test
    void deveAceitarQuestaoValida() {
        Questao questao = criarQuestaoValida();

        var violacoes = validator.validate(questao);

        assertTrue(violacoes.isEmpty());
    }
}
