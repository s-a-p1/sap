
package com.sap.service;

import com.sap.dto.CadastroUsuarioRequest;
import com.sap.model.AnaliseDesempenho;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.MetaEstudo;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.AnaliseDesempenhoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.LogAuditoriaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.MetaEstudoRepository;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.PreferenciaNotificacaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.repository.TokenRecuperacaoSenhaRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private MatriculaDisciplinaRepository matriculaDisciplinaRepository;
    private NotificacaoRepository notificacaoRepository;
    private PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;
    private TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;
    private LogAuditoriaRepository logAuditoriaRepository;
    private MetaEstudoRepository metaEstudoRepository;
    private ResultadoRepository resultadoRepository;
    private AnaliseDesempenhoRepository analiseDesempenhoRepository;
    private DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        configuracaoAcademicaRepository = mock(ConfiguracaoAcademicaRepository.class);
        matriculaDisciplinaRepository = mock(MatriculaDisciplinaRepository.class);
        notificacaoRepository = mock(NotificacaoRepository.class);
        preferenciaNotificacaoRepository = mock(PreferenciaNotificacaoRepository.class);
        tokenRecuperacaoSenhaRepository = mock(TokenRecuperacaoSenhaRepository.class);
        logAuditoriaRepository = mock(LogAuditoriaRepository.class);
        metaEstudoRepository = mock(MetaEstudoRepository.class);
        resultadoRepository = mock(ResultadoRepository.class);
        analiseDesempenhoRepository = mock(AnaliseDesempenhoRepository.class);
        desempenhoAssuntoRepository = mock(DesempenhoAssuntoRepository.class);

        service = new UsuarioService(
                usuarioRepository,
                passwordEncoder,
                configuracaoAcademicaRepository,
                matriculaDisciplinaRepository,
                notificacaoRepository,
                preferenciaNotificacaoRepository,
                tokenRecuperacaoSenhaRepository,
                logAuditoriaRepository,
                metaEstudoRepository,
                resultadoRepository,
                analiseDesempenhoRepository,
                desempenhoAssuntoRepository
        );
    }

    @Test
    void deveCadastrarUsuarioComDadosNormalizados() {
        CadastroUsuarioRequest request = cadastroValido();

        when(passwordEncoder.encode("senha12345"))
                .thenReturn("senha-criptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario resultado = service.cadastrar(request);

        assertEquals("Aluno Teste", resultado.getNome());
        assertEquals("aluno@teste.com", resultado.getEmail());
        assertEquals("senha-criptografada", resultado.getSenha());
        assertEquals("ESTUDANTE", resultado.getTipo());
        assertEquals(ProvedorAutenticacao.LOCAL, resultado.getProvedor());
        assertTrue(resultado.isAtivo());
        assertTrue(resultado.isAceitouTermos());
        assertEquals("1.0", resultado.getVersaoTermos());
        assertNotNull(resultado.getDataAceiteTermos());

        verify(usuarioRepository).existsByEmail("aluno@teste.com");
        verify(usuarioRepository).save(resultado);
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {
        CadastroUsuarioRequest request = cadastroValido();

        when(usuarioRepository.existsByEmail("aluno@teste.com"))
                .thenReturn(true);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(request)
        );

        assertEquals(
                "Já existe um usuário cadastrado com este e-mail.",
                erro.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveExigirAceiteDosTermos() {
        CadastroUsuarioRequest request = mock(CadastroUsuarioRequest.class);

        when(request.getEmail()).thenReturn("aluno@teste.com");
        when(request.isAceitouTermos()).thenReturn(false);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(request)
        );

        assertEquals(
                "É necessário aceitar os Termos de Uso e a Política de Privacidade.",
                erro.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveCriptografarSenhaAoCadastrar() {
        CadastroUsuarioRequest request = cadastroValido();

        when(passwordEncoder.encode("senha12345"))
                .thenReturn("hash-gerado");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario usuario = service.cadastrar(request);

        assertEquals("hash-gerado", usuario.getSenha());
        verify(passwordEncoder).encode("senha12345");
    }

    @Test
    void deveRejeitarAtualizacaoDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        99L, "Novo Nome", "novo@teste.com"
                )
        );

        assertEquals("Usuário não encontrado.", erro.getMessage());
    }

    @Test
    void deveRejeitarNomeNulo() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, null, "novo@teste.com"
                )
        );
    }

    @Test
    void deveRejeitarNomeVazio() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "   ", "novo@teste.com"
                )
        );
    }

    @Test
    void deveRejeitarEmailNulo() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Novo Nome", null
                )
        );
    }

    @Test
    void deveRejeitarEmailVazio() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Novo Nome", "   "
                )
        );
    }

    @Test
    void deveRejeitarNomeComMaisDeCemCaracteres() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "A".repeat(101), "novo@teste.com"
                )
        );
    }

    @Test
    void deveRejeitarEmailComMaisDeCentoECinquentaCaracteres() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Novo Nome", "a".repeat(145) + "@teste.com"
                )
        );
    }

    @Test
    void deveRejeitarEmailInvalido() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Novo Nome", "email-invalido"
                )
        );
    }

    @Test
    void deveRejeitarEmailUtilizadoPorOutroUsuario() {
        configurarUsuarioExistente();

        when(usuarioRepository.existsByEmail("outro@teste.com"))
                .thenReturn(true);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Novo Nome", "outro@teste.com"
                )
        );

        assertEquals("Este e-mail já está cadastrado.", erro.getMessage());
    }

    @Test
    void deveAtualizarNomeEEmail() {
        Usuario usuario = configurarUsuarioExistente();

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.atualizarPerfil(
                1L, "  Nome Atualizado  ", "  NOVO@TESTE.COM  "
        );

        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("novo@teste.com", resultado.getEmail());

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void devePermitirManterMesmoEmail() {
        Usuario usuario = configurarUsuarioExistente();

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        service.atualizarPerfil(
                1L, "Nome Atualizado", "ALUNO@TESTE.COM"
        );

        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRejeitarAlteracaoDeSenhaDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        99L, "senhaAtual", "novaSenha@123"
                )
        );
    }

    @Test
    void deveExigirSenhaAtual() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        1L, "   ", "novaSenha@123"
                )
        );
    }

    @Test
    void deveExigirNovaSenhaComOitoCaracteres() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        1L, "senhaAtual", "1234567"
                )
        );
    }

    @Test
    void deveRejeitarSenhaAtualIncorreta() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaErrada", "hash-atual"))
                .thenReturn(false);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        1L, "senhaErrada", "novaSenha@123"
                )
        );

        assertEquals("Senha atual incorreta.", erro.getMessage());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNovaSenhaIgualAAntiga() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("SenhaAtual@123", "hash-atual"))
                .thenReturn(true);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        1L, "SenhaAtual@123", "SenhaAtual@123"
                )
        );

        assertEquals(
                "A nova senha deve ser diferente da senha atual.",
                erro.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAlterarSenhaComSucesso() {
        Usuario usuario = configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        when(passwordEncoder.matches("novaSenha@123", "hash-atual"))
                .thenReturn(false);

        when(passwordEncoder.encode("novaSenha@123"))
                .thenReturn("novo-hash");

        service.alterarSenha(
                1L, "senhaAtual", "novaSenha@123"
        );

        assertEquals("novo-hash", usuario.getSenha());

        verify(passwordEncoder).encode("novaSenha@123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRejeitarValidacaoDeExclusaoSemUsuario() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarSenhaExclusao(99L, "senhaAtual")
        );
    }

    @Test
    void deveExigirSenhaParaExclusao() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarSenhaExclusao(1L, "")
        );
    }

    @Test
    void deveRejeitarSenhaIncorretaNaExclusao() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaErrada", "hash-atual"))
                .thenReturn(false);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.validarSenhaExclusao(
                        1L, "senhaErrada"
                )
        );

        assertEquals("Senha incorreta.", erro.getMessage());
    }

    @Test
    void deveValidarSenhaCorretaParaExclusao() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.validarSenhaExclusao(
                        1L, "senhaAtual"
                )
        );
    }

    @Test
    void naoDeveExcluirContaComSenhaIncorreta() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaErrada", "hash-atual"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.excluirConta(1L, "senhaErrada")
        );

        verify(usuarioRepository, never()).delete(any());
        verifyNoInteractions(metaEstudoRepository);
    }

    @Test
    void deveExcluirContaSemDadosRelacionados() {
        Usuario usuario = configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        when(metaEstudoRepository.findByUsuarioIdOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of());

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of());

        when(configuracaoAcademicaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        when(notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of());

        when(preferenciaNotificacaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        when(tokenRecuperacaoSenhaRepository.findByUsuarioId(1L))
                .thenReturn(List.of());

        when(logAuditoriaRepository.findByUsuarioId(1L))
                .thenReturn(List.of());

        service.excluirConta(1L, "senhaAtual");

        verify(usuarioRepository).delete(usuario);
        verify(usuarioRepository).flush();
        verify(metaEstudoRepository).flush();
        verify(resultadoRepository).flush();
    }

    @Test
    void deveExcluirMetasAntesDosResultados() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        MetaEstudo meta = new MetaEstudo();

        when(metaEstudoRepository.findByUsuarioIdOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of());

        service.excluirConta(1L, "senhaAtual");

        InOrder ordem = inOrder(
                metaEstudoRepository,
                resultadoRepository
        );

        ordem.verify(metaEstudoRepository).deleteAll(List.of(meta));
        ordem.verify(metaEstudoRepository).flush();
        ordem.verify(resultadoRepository).deleteAll(List.of());
    }

    @Test
    void deveExcluirAnalisesAntesDosResultados() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        Resultado resultado = mock(Resultado.class);
        AnaliseDesempenho analise = mock(AnaliseDesempenho.class);

        when(resultado.getId()).thenReturn(20L);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        when(analiseDesempenhoRepository.findByResultado_Id(20L))
                .thenReturn(Optional.of(analise));

        service.excluirConta(1L, "senhaAtual");

        InOrder ordem = inOrder(
                analiseDesempenhoRepository,
                resultadoRepository
        );

        ordem.verify(analiseDesempenhoRepository).delete(analise);
        ordem.verify(analiseDesempenhoRepository).flush();
        ordem.verify(resultadoRepository).deleteAll(List.of(resultado));
    }

    @Test
    void deveExcluirMatriculasAntesDaConfiguracaoAcademica() {
        configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        ConfiguracaoAcademica configuracao =
                mock(ConfiguracaoAcademica.class);

        MatriculaDisciplina matricula =
                mock(MatriculaDisciplina.class);

        when(configuracao.getId()).thenReturn(10L);

        when(configuracaoAcademicaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaDisciplinaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of(matricula));

        service.excluirConta(1L, "senhaAtual");

        InOrder ordem = inOrder(
                matriculaDisciplinaRepository,
                configuracaoAcademicaRepository
        );

        ordem.verify(matriculaDisciplinaRepository)
                .deleteAll(List.of(matricula));

        ordem.verify(matriculaDisciplinaRepository).flush();

        ordem.verify(configuracaoAcademicaRepository)
                .delete(configuracao);
    }

    @Test
    void deveExcluirUsuarioPorUltimo() {
        Usuario usuario = configurarUsuarioExistente();

        when(passwordEncoder.matches("senhaAtual", "hash-atual"))
                .thenReturn(true);

        service.excluirConta(1L, "senhaAtual");

        InOrder ordem = inOrder(
                metaEstudoRepository,
                resultadoRepository,
                usuarioRepository
        );

        ordem.verify(metaEstudoRepository).flush();
        ordem.verify(resultadoRepository).flush();
        ordem.verify(usuarioRepository).delete(usuario);
        ordem.verify(usuarioRepository).flush();
    }

    private CadastroUsuarioRequest cadastroValido() {
        CadastroUsuarioRequest request =
                mock(CadastroUsuarioRequest.class);

        when(request.getNome()).thenReturn("  Aluno Teste  ");
        when(request.getEmail()).thenReturn("  ALUNO@TESTE.COM  ");
        when(request.getSenha()).thenReturn("senha12345");
        when(request.isAceitouTermos()).thenReturn(true);

        return request;
    }

    private Usuario configurarUsuarioExistente() {
        Usuario usuario = new Usuario();

        usuario.setNome("Aluno Teste");
        usuario.setEmail("aluno@teste.com");
        usuario.setSenha("hash-atual");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        return usuario;
    }

    @Test
    void devePermitirManterEmailAntigoSemExtensao() {
        Usuario usuario = configurarUsuarioExistente();
        usuario.setEmail("admin@sap");

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.atualizarPerfil(
                1L, "Nome Atualizado", "admin@sap"
        );

        assertEquals("Nome Atualizado", resultado.getNome());
        assertEquals("admin@sap", resultado.getEmail());

        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRejeitarAlteracaoParaEmailSemExtensao() {
        configurarUsuarioExistente();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarPerfil(
                        1L, "Nome Atualizado", "novo@sap"
                )
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNovaSenhaSemComplexidade() {
        configurarUsuarioExistente();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarSenha(
                        1L, "senhaAtual", "novaSenha123"
                )
        );

        assertEquals(
                com.sap.validation.ValidadorCredenciais.MENSAGEM_SENHA,
                erro.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
    }

}
