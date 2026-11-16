
package com.sap.validation;

import com.sap.model.Assunto;
import com.sap.model.Disciplina;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssuntoValidationTest {

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

    private Assunto criarAssuntoValido() {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Matemática");

        return new Assunto(
                "Álgebra",
                "Estudo de expressões algébricas",
                disciplina
        );
    }

    @Test
    void deveRejeitarAssuntoComNomeVazio() {
        Assunto assunto = criarAssuntoValido();
        assunto.setNome("");

        var violacoes = validator.validate(assunto);

        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("nome")));
    }

    @Test
    void deveRejeitarAssuntoComNomeMaiorQueCemCaracteres() {
        Assunto assunto = criarAssuntoValido();
        assunto.setNome("A".repeat(101));

        var violacoes = validator.validate(assunto);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("nome")));
    }

    @Test
    void deveRejeitarAssuntoSemDisciplina() {
        Assunto assunto = criarAssuntoValido();
        assunto.setDisciplina(null);

        var violacoes = validator.validate(assunto);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("disciplina")));
    }

    @Test
    void deveRejeitarDescricaoMaiorQueDuzentosECinquentaECincoCaracteres() {
        Assunto assunto = criarAssuntoValido();
        assunto.setDescricao("A".repeat(256));

        var violacoes = validator.validate(assunto);

        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    void deveAceitarAssuntoValido() {
        Assunto assunto = criarAssuntoValido();

        var violacoes = validator.validate(assunto);

        assertTrue(violacoes.isEmpty());
    }
}
