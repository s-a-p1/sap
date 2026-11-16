
package com.sap.validation;

import com.sap.model.Disciplina;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisciplinaValidationTest {

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

    @Test
    void deveRejeitarDisciplinaComNomeVazio() {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("");

        var violacoes = validator.validate(disciplina);

        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream()
                .anyMatch(violacao ->
                        violacao.getPropertyPath().toString().equals("nome")));
    }

    @Test
    void deveRejeitarDisciplinaComNomeMaiorQueCemCaracteres() {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("A".repeat(101));

        var violacoes = validator.validate(disciplina);

        assertFalse(violacoes.isEmpty());
    }

    @Test
    void deveAceitarDisciplinaComNomeValido() {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Matemática");

        var violacoes = validator.validate(disciplina);

        assertTrue(violacoes.isEmpty());
    }
}
