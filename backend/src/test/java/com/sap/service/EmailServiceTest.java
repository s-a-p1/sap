
package com.sap.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService service;
    private MimeMessage mensagem;

    @BeforeEach
    void configurar() {
        mailSender = mock(JavaMailSender.class);

        mensagem = new MimeMessage(
                Session.getInstance(new Properties())
        );

        when(mailSender.createMimeMessage()).thenReturn(mensagem);

        service = new EmailService(
                mailSender,
                "noreply@sap.com",
                "Equipe SAP"
        );
    }

    @Test
    void deveEnviarEmailDeRecuperacao() {
        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost:5173/redefinir?token=123"
        );

        verify(mailSender).send(mensagem);
    }

    @Test
    void deveConfigurarDestinatarioCorretamente()
            throws MessagingException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        assertEquals(
                "estudante@teste.com",
                mensagem.getAllRecipients()[0].toString()
        );
    }

    @Test
    void deveConfigurarRemetenteCorretamente()
            throws MessagingException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        assertTrue(
                mensagem.getFrom()[0].toString()
                        .contains("noreply@sap.com")
        );

        assertTrue(
                mensagem.getFrom()[0].toString()
                        .contains("Equipe SAP")
        );
    }

    @Test
    void deveConfigurarAssuntoCorretamente()
            throws MessagingException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        assertEquals(
                "SAP - Recuperação de senha",
                mensagem.getSubject()
        );
    }

    @Test
    void deveIncluirLinkDeRecuperacao()
            throws MessagingException, IOException {

        String link = "http://localhost:5173/redefinir?token=abc123";

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                link
        );

        String conteudo = mensagem.getContent().toString();

        assertTrue(conteudo.contains(link));
    }

    @Test
    void deveInformarValidadeDeQuinzeMinutos()
            throws MessagingException, IOException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        String conteudo = mensagem.getContent().toString();

        assertTrue(conteudo.contains("15 minutos"));
    }

    @Test
    void deveInformarUsoUnicoDoLink()
            throws MessagingException, IOException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        String conteudo = mensagem.getContent().toString();

        assertTrue(conteudo.contains("apenas uma vez"));
    }

    @Test
    void deveOrientarUsuarioQueNaoSolicitouRecuperacao()
            throws MessagingException, IOException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        String conteudo = mensagem.getContent().toString();

        assertTrue(
                conteudo.contains("Se você não solicitou")
        );
    }

    @Test
    void deveEnviarEmailComoTextoSimples()
            throws MessagingException {

        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        assertTrue(mensagem.isMimeType("text/plain"));
    }

    @Test
    void deveEnviarApenasUmaMensagem() {
        service.enviarRecuperacaoSenha(
                "estudante@teste.com",
                "http://localhost/redefinir"
        );

        verify(mailSender, times(1)).send(mensagem);
    }

    @Test
    void devePropagarExcecaoParaDestinatarioNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRecuperacaoSenha(
                        null,
                        "http://localhost/redefinir"
                )
        );

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void devePermitirEnderecoSemValidacaoCompletaDeFormato() {
        service.enviarRecuperacaoSenha(
                "email-invalido",
                "http://localhost/redefinir"
        );

        verify(mailSender).send(mensagem);
    }
}
