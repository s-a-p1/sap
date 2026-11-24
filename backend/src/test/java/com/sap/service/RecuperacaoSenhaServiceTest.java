
package com.sap.service;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.TokenRecuperacaoSenha;
import com.sap.model.Usuario;
import com.sap.repository.TokenRecuperacaoSenhaRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecuperacaoSenhaServiceTest {

    private UsuarioRepository usuarioRepository;
    private TokenRecuperacaoSenhaRepository tokenRepository;
    private GeradorTokenRecuperacao geradorToken;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private TransactionTemplate transactionTemplate;
    private RecuperacaoSenhaService service;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        tokenRepository = mock(TokenRecuperacaoSenhaRepository.class);
        geradorToken = mock(GeradorTokenRecuperacao.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        transactionTemplate = mock(TransactionTemplate.class);

        service = new RecuperacaoSenhaService(
                usuarioRepository,
                tokenRepository,
                geradorToken,
                emailService,
                passwordEncoder,
                transactionTemplate,
                "http://localhost:5173/"
        );

        doAnswer(invocacao -> {
            Consumer<TransactionStatus> callback =
                    invocacao.getArgument(0);

            callback.accept(mock(TransactionStatus.class));

            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        doAnswer(invocacao -> {
            TransactionCallback<?> callback =
                    invocacao.getArgument(0);

            return callback.doInTransaction(
                    mock(TransactionStatus.class)
            );
        }).when(transactionTemplate).execute(any());
    }

    @Test
    void naoDeveSolicitarRecuperacaoComEmailNulo() {
        service.solicitarRecuperacao(null);

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void naoDeveSolicitarRecuperacaoComEmailVazio() {
        service.solicitarRecuperacao("");

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void naoDeveSolicitarRecuperacaoComEspacos() {
        service.solicitarRecuperacao("   ");

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveNormalizarEspacosDoEmail() {
        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.empty());

        service.solicitarRecuperacao("  teste@email.com  ");

        verify(usuarioRepository).findByEmail("teste@email.com");
    }

    @Test
    void naoDeveEnviarEmailParaUsuarioInexistente() {
        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        service.solicitarRecuperacao("inexistente@email.com");

        verifyNoInteractions(emailService);
        verifyNoInteractions(geradorToken);
    }

    @Test
    void naoDeveEnviarEmailParaUsuarioInativo() {
        Usuario usuario = usuario(false, ProvedorAutenticacao.LOCAL);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        service.solicitarRecuperacao("teste@email.com");

        verifyNoInteractions(emailService);
        verifyNoInteractions(geradorToken);
    }

    @Test
    void naoDeveEnviarEmailParaUsuarioDeOutroProvedor() {
        Usuario usuario = usuario(true, ProvedorAutenticacao.GOOGLE);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        service.solicitarRecuperacao("teste@email.com");

        verifyNoInteractions(emailService);
        verifyNoInteractions(geradorToken);
    }

    @Test
    void naoDeveGerarTokenQuandoSolicitacaoForRecente() {
        configurarUsuarioValido();

        when(tokenRepository.existsByUsuarioIdAndDataCriacaoAfter(
                eq(1L), any(LocalDateTime.class)
        )).thenReturn(true);

        service.solicitarRecuperacao("teste@email.com");

        verifyNoInteractions(geradorToken);
        verifyNoInteractions(emailService);
    }

    @Test
    void naoDeveGerarTokenQuandoLimiteHorarioForAtingido() {
        configurarUsuarioValido();

        when(tokenRepository.countByUsuarioIdAndDataCriacaoAfter(
                eq(1L), any(LocalDateTime.class)
        )).thenReturn(5L);

        service.solicitarRecuperacao("teste@email.com");

        verifyNoInteractions(geradorToken);
        verifyNoInteractions(emailService);
    }

    @Test
    void devePermitirSolicitacaoAbaixoDoLimiteHorario() {
        configurarUsuarioValido();

        when(tokenRepository.countByUsuarioIdAndDataCriacaoAfter(
                eq(1L), any(LocalDateTime.class)
        )).thenReturn(4L);

        when(geradorToken.gerarToken()).thenReturn("token-seguro");
        when(geradorToken.gerarHash("token-seguro"))
                .thenReturn("hash-seguro");

        when(tokenRepository.findByUsuarioIdAndDataUtilizacaoIsNull(1L))
                .thenReturn(List.of());

        service.solicitarRecuperacao("teste@email.com");

        verify(geradorToken).gerarToken();

        verify(emailService).enviarRecuperacaoSenha(
                eq("teste@email.com"),
                eq("http://localhost:5173/redefinir-senha?token=token-seguro")
        );
    }

    @Test
    void deveInvalidarTokensAnteriores() {
        configurarUsuarioValido();

        TokenRecuperacaoSenha anterior =
                mock(TokenRecuperacaoSenha.class);

        when(geradorToken.gerarToken()).thenReturn("token-novo");
        when(geradorToken.gerarHash("token-novo"))
                .thenReturn("hash-novo");

        when(tokenRepository.findByUsuarioIdAndDataUtilizacaoIsNull(1L))
                .thenReturn(List.of(anterior));

        service.solicitarRecuperacao("teste@email.com");

        verify(anterior).setDataUtilizacao(any(LocalDateTime.class));
        verify(tokenRepository).save(anterior);

        verify(emailService).enviarRecuperacaoSenha(
                eq("teste@email.com"),
                contains("token-novo")
        );
    }

    @Test
    void deveCriarTokenComExpiracaoDeQuinzeMinutos() {
        configurarUsuarioValido();

        when(geradorToken.gerarToken()).thenReturn("token-novo");
        when(geradorToken.gerarHash("token-novo"))
                .thenReturn("hash-novo");

        when(tokenRepository.findByUsuarioIdAndDataUtilizacaoIsNull(1L))
                .thenReturn(List.of());

        service.solicitarRecuperacao("teste@email.com");

        ArgumentCaptor<TokenRecuperacaoSenha> captor =
                ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);

        verify(tokenRepository).save(captor.capture());

        TokenRecuperacaoSenha token = captor.getValue();

        assertEquals("hash-novo", token.getTokenHash());

        assertEquals(
                token.getDataCriacao().plusMinutes(15),
                token.getDataExpiracao()
        );

        assertNotNull(token.getUsuario());
    }

    @Test
    void deveRejeitarTokenNulo() {
        assertFalse(service.redefinirSenha(null, "novaSenha@123"));

        verifyNoInteractions(geradorToken);
    }

    @Test
    void deveRejeitarTokenVazio() {
        assertFalse(service.redefinirSenha("   ", "novaSenha@123"));

        verifyNoInteractions(geradorToken);
    }

    @Test
    void deveRejeitarSenhaNula() {
        assertFalse(service.redefinirSenha("token", null));

        verifyNoInteractions(geradorToken);
    }

    @Test
    void deveRejeitarSenhaComMenosDeOitoCaracteres() {
        assertFalse(service.redefinirSenha("token", "Ab@1234"));

        verifyNoInteractions(geradorToken);
    }

    @Test
    void deveRejeitarSenhaComMaisDeSetentaEDoisCaracteres() {
        assertFalse(service.redefinirSenha("token", "Ab@1" + "a".repeat(69)));

        verifyNoInteractions(geradorToken);
    }

    @Test
    void deveAceitarSenhaComOitoCaracteres() {
        when(geradorToken.gerarHash("token")).thenReturn("hash");

        when(tokenRepository.findByTokenHashComBloqueio("hash"))
                .thenReturn(Optional.empty());

        assertFalse(service.redefinirSenha("token", "Ab@12345"));

        verify(tokenRepository).findByTokenHashComBloqueio("hash");
    }

    @Test
    void deveAceitarSenhaComSetentaEDoisCaracteres() {
        when(geradorToken.gerarHash("token")).thenReturn("hash");

        when(tokenRepository.findByTokenHashComBloqueio("hash"))
                .thenReturn(Optional.empty());

        assertFalse(service.redefinirSenha("token", "Ab@1" + "a".repeat(68)));

        verify(tokenRepository).findByTokenHashComBloqueio("hash");
    }

    @Test
    void deveRejeitarTokenInexistente() {
        when(geradorToken.gerarHash("token")).thenReturn("hash");

        when(tokenRepository.findByTokenHashComBloqueio("hash"))
                .thenReturn(Optional.empty());

        assertFalse(service.redefinirSenha("token", "novaSenha@123"));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRejeitarTokenJaUtilizado() {
        TokenRecuperacaoSenha recuperacao =
                mock(TokenRecuperacaoSenha.class);

        when(recuperacao.getDataUtilizacao())
                .thenReturn(LocalDateTime.now().minusMinutes(1));

        configurarTokenEncontrado(recuperacao);

        assertFalse(service.redefinirSenha("token", "novaSenha@123"));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        TokenRecuperacaoSenha recuperacao =
                mock(TokenRecuperacaoSenha.class);

        when(recuperacao.getDataExpiracao())
                .thenReturn(LocalDateTime.now().minusSeconds(1));

        configurarTokenEncontrado(recuperacao);

        assertFalse(service.redefinirSenha("token", "novaSenha@123"));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRejeitarRedefinicaoParaUsuarioInativo() {
        TokenRecuperacaoSenha recuperacao =
                tokenValido(usuario(false, ProvedorAutenticacao.LOCAL));

        configurarTokenEncontrado(recuperacao);

        assertFalse(service.redefinirSenha("token", "novaSenha@123"));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRejeitarRedefinicaoParaOutroProvedor() {
        TokenRecuperacaoSenha recuperacao =
                tokenValido(usuario(true, ProvedorAutenticacao.GOOGLE));

        configurarTokenEncontrado(recuperacao);

        assertFalse(service.redefinirSenha("token", "novaSenha@123"));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRedefinirSenhaComTokenValido() {
        Usuario usuario = usuario(true, ProvedorAutenticacao.LOCAL);

        TokenRecuperacaoSenha recuperacao = tokenValido(usuario);

        configurarTokenEncontrado(recuperacao);

        when(passwordEncoder.encode("novaSenha@123"))
                .thenReturn("senha-criptografada");

        boolean resultado =
                service.redefinirSenha("token", "novaSenha@123");

        assertTrue(resultado);

        verify(usuario).setSenha("senha-criptografada");
        verify(usuarioRepository).save(usuario);

        verify(recuperacao).setDataUtilizacao(any(LocalDateTime.class));
        verify(tokenRepository).save(recuperacao);
    }

    private Usuario usuario(
            boolean ativo,
            ProvedorAutenticacao provedor
    ) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.isAtivo()).thenReturn(ativo);

        if (ativo) {
            when(usuario.getProvedor()).thenReturn(provedor);
        }

        return usuario;
    }

    private void configurarUsuarioValido() {
        Usuario usuario = usuario(true, ProvedorAutenticacao.LOCAL);

        when(usuario.getId()).thenReturn(1L);
        when(usuario.getEmail()).thenReturn("teste@email.com");

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));
    }

    private TokenRecuperacaoSenha tokenValido(Usuario usuario) {
        TokenRecuperacaoSenha recuperacao =
                mock(TokenRecuperacaoSenha.class);

        when(recuperacao.getDataExpiracao())
                .thenReturn(LocalDateTime.now().plusMinutes(10));

        when(recuperacao.getUsuario()).thenReturn(usuario);

        return recuperacao;
    }

    private void configurarTokenEncontrado(
            TokenRecuperacaoSenha recuperacao
    ) {
        when(geradorToken.gerarHash("token")).thenReturn("hash");

        when(tokenRepository.findByTokenHashComBloqueio("hash"))
                .thenReturn(Optional.of(recuperacao));
    }

    @Test
    void deveRejeitarRedefinicaoComSenhaSemComplexidade() {
        assertFalse(
                service.redefinirSenha("token", "novaSenha123")
        );

        verifyNoInteractions(geradorToken);
        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(passwordEncoder);
    }

}
