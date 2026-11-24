package com.sap.validation;

import com.sap.dto.CadastroUsuarioRequest;
import com.sap.dto.CadastroProfessorRequest;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorCredenciaisTest {

    @Test
    void deveAceitarEmailComCom() {
        assertTrue(ValidadorCredenciais.emailValido("aluno@sap.com"));
    }

    @Test
    void deveAceitarEmailComComBr() {
        assertTrue(ValidadorCredenciais.emailValido("aluno@gmail.com.br"));
    }

    @Test
    void deveAceitarEmailInstitucional() {
        assertTrue(ValidadorCredenciais.emailValido("aluno@usp.br"));
        assertTrue(ValidadorCredenciais.emailValido("aluno@umc.br"));
    }

    @Test
    void deveRejeitarEmailSemExtensao() {
        assertFalse(ValidadorCredenciais.emailValido("aluno@sap"));
    }

    @Test
    void deveRejeitarEmailSemArroba() {
        assertFalse(ValidadorCredenciais.emailValido("alunosap.com"));
    }

    @Test
    void deveRejeitarEmailNulo() {
        assertFalse(ValidadorCredenciais.emailValido(null));
    }

    @Test
    void deveRejeitarEmailVazio() {
        assertFalse(ValidadorCredenciais.emailValido(""));
    }

    @Test
    void deveRejeitarEmailComEspacos() {
        assertFalse(ValidadorCredenciais.emailValido("aluno teste@sap.com"));
    }

    @Test
    void deveAceitarSenhaComplexa() {
        assertTrue(ValidadorCredenciais.senhaValida("Sap@2026"));
    }

    @Test
    void deveRejeitarSenhaSemMaiuscula() {
        assertFalse(ValidadorCredenciais.senhaValida("sap@2026"));
    }

    @Test
    void deveRejeitarSenhaSemMinuscula() {
        assertFalse(ValidadorCredenciais.senhaValida("SAP@2026"));
    }

    @Test
    void deveRejeitarSenhaSemNumero() {
        assertFalse(ValidadorCredenciais.senhaValida("Sap@Teste"));
    }

    @Test
    void deveRejeitarSenhaSemCaractereEspecial() {
        assertFalse(ValidadorCredenciais.senhaValida("Sap2026"));
    }

    @Test
    void deveRejeitarSenhaNula() {
        assertFalse(ValidadorCredenciais.senhaValida(null));
    }

    @Test
    void deveRejeitarSenhaVazia() {
        assertFalse(ValidadorCredenciais.senhaValida(""));
    }

    @Test
    void deveAceitarDiferentesCaracteresEspeciais() {
        assertTrue(ValidadorCredenciais.senhaValida("Sap@2026"));
        assertTrue(ValidadorCredenciais.senhaValida("Sap#2026"));
        assertTrue(ValidadorCredenciais.senhaValida("Sap$2026"));
        assertTrue(ValidadorCredenciais.senhaValida("Sap%2026"));
    }

    @Test
    void deveAceitarCadastroEstudanteValido() {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest();
        request.setNome("Estudante Teste");
        request.setEmail("estudante@usp.br");
        request.setSenha("Sap@2026");
        request.setAceitouTermos(true);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
        }
    }

    @Test
    void deveRejeitarCadastroEstudanteComEmailInvalido() {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest();
        request.setNome("Estudante Teste");
        request.setEmail("estudante@sap");
        request.setSenha("Sap@2026");
        request.setAceitouTermos(true);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(request, "email").isEmpty());
        }
    }

    @Test
    void deveRejeitarCadastroEstudanteComSenhaFraca() {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest();
        request.setSenha("Sap2026");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(request, "senha").isEmpty());
        }
    }

    @Test
    void deveAceitarCadastroProfessorValido() {
        CadastroProfessorRequest request = new CadastroProfessorRequest();
        request.setNome("Professor Teste");
        request.setEmail("professor@umc.br");
        request.setSenha("Sap@2026");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
        }
    }

    @Test
    void deveRejeitarCadastroProfessorComEmailInvalido() {
        CadastroProfessorRequest request = new CadastroProfessorRequest();
        request.setEmail("professor@sap");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(request, "email").isEmpty());
        }
    }

    @Test
    void deveRejeitarCadastroProfessorComSenhaFraca() {
        CadastroProfessorRequest request = new CadastroProfessorRequest();
        request.setSenha("Sap2026");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(request, "senha").isEmpty());
        }
    }
}
