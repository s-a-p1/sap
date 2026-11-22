
package com.sap.controller;

import com.sap.dto.CadastroProfessorRequest;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AdminController controller;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        controller = new AdminController(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveRetornarResumoSemUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        ResponseEntity<Map<String, Long>> resposta =
                controller.obterResumo();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(0L, resposta.getBody().get("totalAlunos"));
        assertEquals(0L, resposta.getBody().get("totalProfessores"));
    }

    @Test
    void deveContarEstudantesEProfessores() {
        List<Usuario> usuarios = List.of(
                usuario("ESTUDANTE"),
                usuario("ESTUDANTE"),
                usuario("PROFESSOR"),
                usuario("ADMIN")
        );

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        ResponseEntity<Map<String, Long>> resposta =
                controller.obterResumo();

        assertEquals(2L, resposta.getBody().get("totalAlunos"));
        assertEquals(1L, resposta.getBody().get("totalProfessores"));
    }

    @Test
    void deveIgnorarOutrosTiposNoResumo() {
        List<Usuario> usuarios = List.of(
                usuario("ADMIN"),
                usuario("OUTRO")
        );

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        ResponseEntity<Map<String, Long>> resposta =
                controller.obterResumo();

        assertEquals(0L, resposta.getBody().get("totalAlunos"));
        assertEquals(0L, resposta.getBody().get("totalProfessores"));
    }

    @Test
    void deveRetornarListaVaziaDeUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<Map<String, Object>>> resposta =
                controller.listarUsuarios();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isEmpty());
    }

    @Test
    void deveListarUsuariosComDadosCorretos() {
        Usuario aluno = usuario("ESTUDANTE");

        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");
        aluno.setAtivo(true);

        when(usuarioRepository.findAll())
                .thenReturn(List.of(aluno));

        ResponseEntity<List<Map<String, Object>>> resposta =
                controller.listarUsuarios();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1, resposta.getBody().size());

        Map<String, Object> dados = resposta.getBody().get(0);

        assertEquals("Aluno Teste", dados.get("nome"));
        assertEquals("aluno@teste.com", dados.get("email"));
        assertEquals("ESTUDANTE", dados.get("tipo"));
        assertEquals(true, dados.get("ativo"));
        assertTrue(dados.containsKey("id"));
    }

    @Test
    void deveRetornarNaoEncontradoAoAlterarUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.alterarAcesso(99L, Map.of("ativo", false));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Usuário não encontrado."),
                resposta.getBody()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoDeveAlterarAcessoDeAdministrador() {
        Usuario administrador = usuario("ADMIN");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(administrador));

        ResponseEntity<?> resposta =
                controller.alterarAcesso(1L, Map.of("ativo", false));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Não é permitido alterar o acesso de administradores."),
                resposta.getBody()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveExigirStatusDeAcesso() {
        Usuario estudante = usuario("ESTUDANTE");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(estudante));

        ResponseEntity<?> resposta =
                controller.alterarAcesso(1L, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Informe o status de acesso."),
                resposta.getBody()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveDesativarEstudante() {
        Usuario estudante = usuario("ESTUDANTE");

        estudante.setNome("Aluno Teste");
        estudante.setAtivo(true);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(estudante));

        ResponseEntity<?> resposta =
                controller.alterarAcesso(1L, Map.of("ativo", false));

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertFalse(estudante.isAtivo());

        verify(usuarioRepository).save(estudante);

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(false, dados.get("ativo"));
        assertEquals("Aluno Teste", dados.get("nome"));
    }

    @Test
    void deveReativarProfessor() {
        Usuario professor = usuario("PROFESSOR");

        professor.setNome("Professor Teste");
        professor.setAtivo(false);

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(professor));

        ResponseEntity<?> resposta =
                controller.alterarAcesso(2L, Map.of("ativo", true));

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertTrue(professor.isAtivo());

        verify(usuarioRepository).save(professor);
    }

    @Test
    void deveRejeitarCadastroComEmailDuplicado() {
        CadastroProfessorRequest request =
                mock(CadastroProfessorRequest.class);

        when(request.getEmail())
                .thenReturn("  PROFESSOR@TESTE.COM  ");

        when(usuarioRepository.existsByEmail("professor@teste.com"))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Já existe um usuário cadastrado com este e-mail."),
                resposta.getBody()
        );

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveCadastrarProfessorComDadosNormalizados() {
        CadastroProfessorRequest request = cadastroValido();

        when(passwordEncoder.encode("senha12345"))
                .thenReturn("senha-criptografada");

        configurarSalvamentoProfessor();

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario professor = captor.getValue();

        assertEquals("Professor Teste", professor.getNome());
        assertEquals("professor@teste.com", professor.getEmail());
        assertEquals("senha-criptografada", professor.getSenha());
        assertEquals("PROFESSOR", professor.getTipo());
        assertEquals(ProvedorAutenticacao.LOCAL, professor.getProvedor());
        assertTrue(professor.isAtivo());
        assertFalse(professor.isAceitouTermos());
        assertNull(professor.getDataAceiteTermos());
        assertNull(professor.getVersaoTermos());
    }

    @Test
    void deveRetornarDadosDoProfessorCadastrado() {
        CadastroProfessorRequest request = cadastroValido();

        when(passwordEncoder.encode("senha12345"))
                .thenReturn("hash");

        configurarSalvamentoProfessor();

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(10L, dados.get("id"));
        assertEquals("Professor Teste", dados.get("nome"));
        assertEquals("professor@teste.com", dados.get("email"));
        assertEquals("PROFESSOR", dados.get("tipo"));
        assertEquals(true, dados.get("ativo"));
    }

    @Test
    void deveCriptografarSenhaDoProfessor() {
        CadastroProfessorRequest request = cadastroValido();

        when(passwordEncoder.encode("senha12345"))
                .thenReturn("hash-seguro");

        configurarSalvamentoProfessor();

        controller.cadastrarProfessor(request);

        verify(passwordEncoder).encode("senha12345");
    }

    private Usuario usuario(String tipo) {
        Usuario usuario = mock(Usuario.class, CALLS_REAL_METHODS);

        usuario.setTipo(tipo);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario@teste.com");

        when(usuario.getId()).thenReturn(1L);

        return usuario;
    }

    private CadastroProfessorRequest cadastroValido() {
        CadastroProfessorRequest request =
                mock(CadastroProfessorRequest.class);

        when(request.getNome())
                .thenReturn("  Professor Teste  ");

        when(request.getEmail())
                .thenReturn("  PROFESSOR@TESTE.COM  ");

        when(request.getSenha())
                .thenReturn("senha12345");

        return request;
    }

    private void configurarSalvamentoProfessor() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario professor = invocacao.getArgument(0);

                    Usuario salvo = mock(Usuario.class, CALLS_REAL_METHODS);

                    salvo.setNome(professor.getNome());
                    salvo.setEmail(professor.getEmail());
                    salvo.setTipo(professor.getTipo());
                    salvo.setAtivo(professor.isAtivo());

                    when(salvo.getId()).thenReturn(10L);

                    return salvo;
                });
    }
}
