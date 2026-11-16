
package com.sap.validation;

import com.sap.dto.QuestaoRequest;
import com.sap.dto.QuestaoEdicaoRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestaoRequestValidationTest {

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

    private QuestaoRequest criarCadastroValido() {
        QuestaoRequest request = new QuestaoRequest();
        request.setAvaliacaoId(1L);
        request.setEnunciado("Quanto é 2 + 2?");
        request.setAlternativaA("2");
        request.setAlternativaB("3");
        request.setAlternativaC("4");
        request.setAlternativaD("5");
        request.setRespostaCorreta("C");
        return request;
    }

    private QuestaoEdicaoRequest criarEdicaoValida() {
        QuestaoEdicaoRequest request = new QuestaoEdicaoRequest();
        request.setEnunciado("Quanto é 3 + 3?");
        request.setAlternativaA("4");
        request.setAlternativaB("5");
        request.setAlternativaC("6");
        request.setAlternativaD("7");
        request.setRespostaCorreta("C");
        return request;
    }

    @Test
    void deveRejeitarCadastroSemAvaliacao() {
        QuestaoRequest request = criarCadastroValido();
        request.setAvaliacaoId(null);

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("avaliacaoId")));
    }

    @Test
    void deveRejeitarCadastroComAvaliacaoInvalida() {
        QuestaoRequest request = criarCadastroValido();
        request.setAvaliacaoId(0L);

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("avaliacaoId")));
    }

    @Test
    void deveRejeitarCadastroComEnunciadoVazio() {
        QuestaoRequest request = criarCadastroValido();
        request.setEnunciado("");

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("enunciado")));
    }

    @Test
    void deveRejeitarCadastroComAlternativaMuitoLonga() {
        QuestaoRequest request = criarCadastroValido();
        request.setAlternativaA("A".repeat(256));

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("alternativaA")));
    }

    @Test
    void deveAceitarCadastroValido() {
        QuestaoRequest request = criarCadastroValido();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveRejeitarEdicaoComRespostaInvalida() {
        QuestaoEdicaoRequest request = criarEdicaoValida();
        request.setRespostaCorreta("E");

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("respostaCorreta")));
    }

    @Test
    void deveRejeitarEdicaoComEnunciadoVazio() {
        QuestaoEdicaoRequest request = criarEdicaoValida();
        request.setEnunciado("");

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("enunciado")));
    }

    @Test
    void deveAceitarEdicaoValida() {
        QuestaoEdicaoRequest request = criarEdicaoValida();

        assertTrue(validator.validate(request).isEmpty());
    }
}
