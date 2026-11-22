
package com.sap.controller;

import com.sap.dto.ConfiguracaoAcademicaRequest;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Curso;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.OfertaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.service.ConfiguracaoAcademicaService;
import com.sap.service.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfiguracaoAcademicaControllerTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;
    private ConfiguracaoAcademicaService configuracaoService;
    private ConfiguracaoAcademicaRepository configuracaoRepository;
    private MatriculaDisciplinaRepository matriculaRepository;
    private Authentication authentication;
    private ConfiguracaoAcademicaController controller;

    @BeforeEach
    void configurar() {
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        configuracaoService = mock(ConfiguracaoAcademicaService.class);
        configuracaoRepository = mock(ConfiguracaoAcademicaRepository.class);
        matriculaRepository = mock(MatriculaDisciplinaRepository.class);
        authentication = mock(Authentication.class);

        controller = new ConfiguracaoAcademicaController(
                usuarioAutenticadoService,
                configuracaoService,
                configuracaoRepository,
                matriculaRepository
        );
    }

    @Test
    void deveNegarConsultaSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta = controller.consultar(authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
        verifyNoInteractions(configuracaoRepository, matriculaRepository);
    }

    @Test
    void deveNegarConsultaParaProfessor() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta = controller.consultar(authentication);

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
        verifyNoInteractions(configuracaoRepository, matriculaRepository);
    }

    @Test
    void deveRetornarNaoConfiguradoQuandoNaoExisteConfiguracao() {
        autenticar("ESTUDANTE");

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta = controller.consultar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(Map.of("configurado", false), resposta.getBody());

        verify(configuracaoRepository).findByUsuarioId(1L);
        verifyNoInteractions(matriculaRepository);
    }

    @Test
    void deveConsultarConfiguracaoExistente() {
        autenticar("ESTUDANTE");
        ConfiguracaoAcademica configuracao = configuracao();

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta = controller.consultar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(true, dados.get("configurado"));
        assertEquals(2L, dados.get("cursoId"));
        assertEquals(3, dados.get("semestre"));
        assertEquals(List.of(), dados.get("ofertasDisciplinasIds"));
    }

    @Test
    void deveConsultarConfiguracaoComDisciplinasMatriculadas() {
        autenticar("ESTUDANTE");

        ConfiguracaoAcademica configuracao = configuracao();
        MatriculaDisciplina matricula1 = matricula(20L);
        MatriculaDisciplina matricula2 = matricula(21L);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of(matricula1, matricula2));

        ResponseEntity<?> resposta = controller.consultar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(true, dados.get("configurado"));
        assertEquals(List.of(20L, 21L), dados.get("ofertasDisciplinasIds"));

        verify(matriculaRepository).findByConfiguracaoAcademicaId(10L);
    }

    @Test
    void deveNegarSalvamentoSemAutenticacao() {
        semAutenticacao();

        ConfiguracaoAcademicaRequest request = request();

        ResponseEntity<?> resposta =
                controller.salvar(authentication, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());

        verifyNoInteractions(configuracaoService);
    }

    @Test
    void deveSalvarConfiguracaoComSucesso() {
        Usuario estudante = autenticar("ESTUDANTE");
        ConfiguracaoAcademicaRequest request = request();

        ResponseEntity<?> resposta =
                controller.salvar(authentication, request);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());

        verify(configuracaoService).salvar(estudante, request);
    }

    @Test
    void deveEncaminharDadosCorretosAoService() {
        Usuario estudante = autenticar("ESTUDANTE");

        ConfiguracaoAcademicaRequest request =
                new ConfiguracaoAcademicaRequest(
                        5L,
                        6,
                        List.of(30L, 31L, 32L)
                );

        controller.salvar(authentication, request);

        verify(configuracaoService).salvar(estudante, request);

        assertEquals(5L, request.cursoId());
        assertEquals(6, request.semestre());
        assertEquals(List.of(30L, 31L, 32L),
                request.ofertasDisciplinasIds());
    }

    @Test
    void devePropagarErroDoServiceAoSalvar() {
        Usuario estudante = autenticar("ESTUDANTE");
        ConfiguracaoAcademicaRequest request = request();

        doThrow(new IllegalArgumentException("Curso inválido."))
                .when(configuracaoService).salvar(estudante, request);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.salvar(authentication, request)
        );

        assertEquals("Curso inválido.", erro.getMessage());
    }

    private Usuario autenticar(String tipo) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getTipo()).thenReturn(tipo);
        when(usuario.getId()).thenReturn(1L);

        when(usuarioAutenticadoService.buscar(authentication))
                .thenReturn(Optional.of(usuario));

        return usuario;
    }

    private void semAutenticacao() {
        when(usuarioAutenticadoService.buscar(authentication))
                .thenReturn(Optional.empty());
    }

    private ConfiguracaoAcademica configuracao() {
        ConfiguracaoAcademica configuracao = new ConfiguracaoAcademica();
        Curso curso = mock(Curso.class);

        when(curso.getId()).thenReturn(2L);

        configuracao.setId(10L);
        configuracao.setCurso(curso);
        configuracao.setSemestre(3);

        return configuracao;
    }

    private MatriculaDisciplina matricula(Long ofertaId) {
        OfertaDisciplina oferta = mock(OfertaDisciplina.class);

        when(oferta.getId()).thenReturn(ofertaId);

        MatriculaDisciplina matricula = new MatriculaDisciplina();
        matricula.setOfertaDisciplina(oferta);

        return matricula;
    }

    private ConfiguracaoAcademicaRequest request() {
        return new ConfiguracaoAcademicaRequest(
                2L,
                3,
                List.of(20L, 21L)
        );
    }
}
