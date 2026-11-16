
package com.sap.validation;

import com.sap.dto.AvaliacaoRequest;
import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvaliacaoValidationTest {

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

    private Avaliacao criarAvaliacaoValida() {
        Assunto assunto = new Assunto();
        assunto.setNome("Álgebra");

        return new Avaliacao("Avaliação de Álgebra", assunto);
    }

    private AvaliacaoRequest criarRequestValido() {
        AvaliacaoRequest request = new AvaliacaoRequest();
        request.setTitulo("Avaliação de Álgebra");
        request.setAssuntoId(1L);

        return request;
    }

    @Test
    void deveRejeitarAvaliacaoComTituloVazio() {
        Avaliacao avaliacao = criarAvaliacaoValida();
        avaliacao.setTitulo("");

        var violacoes = validator.validate(avaliacao);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void deveRejeitarAvaliacaoComTituloMaiorQueCentoECinquentaCaracteres() {
        Avaliacao avaliacao = criarAvaliacaoValida();
        avaliacao.setTitulo("A".repeat(151));

        var violacoes = validator.validate(avaliacao);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void deveRejeitarAvaliacaoSemAssunto() {
        Avaliacao avaliacao = criarAvaliacaoValida();
        avaliacao.setAssunto(null);

        var violacoes = validator.validate(avaliacao);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("assunto")));
    }

    @Test
    void deveAceitarAvaliacaoValida() {
        Avaliacao avaliacao = criarAvaliacaoValida();

        var violacoes = validator.validate(avaliacao);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    void deveRejeitarRequestComTituloVazio() {
        AvaliacaoRequest request = criarRequestValido();
        request.setTitulo("");

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void deveRejeitarRequestComTituloMaiorQueCentoECinquentaCaracteres() {
        AvaliacaoRequest request = criarRequestValido();
        request.setTitulo("A".repeat(151));

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void deveRejeitarRequestComAssuntoInvalido() {
        AvaliacaoRequest request = criarRequestValido();
        request.setAssuntoId(0L);

        var violacoes = validator.validate(request);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("assuntoId")));
    }

    @Test
    void deveAceitarRequestValido() {
        AvaliacaoRequest request = criarRequestValido();

        var violacoes = validator.validate(request);

        assertTrue(violacoes.isEmpty());
    }
}
