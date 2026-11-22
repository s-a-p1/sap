
package com.sap.service;

import com.sap.dto.ConfiguracaoAcademicaRequest;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Curso;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.OfertaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.CursoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfiguracaoAcademicaServiceTest {

    private ConfiguracaoAcademicaRepository configuracaoRepository;
    private CursoRepository cursoRepository;
    private OfertaDisciplinaRepository ofertaRepository;
    private MatriculaDisciplinaRepository matriculaRepository;
    private ConfiguracaoAcademicaService service;

    @BeforeEach
    void configurar() {
        configuracaoRepository = mock(ConfiguracaoAcademicaRepository.class);
        cursoRepository = mock(CursoRepository.class);
        ofertaRepository = mock(OfertaDisciplinaRepository.class);
        matriculaRepository = mock(MatriculaDisciplinaRepository.class);

        service = new ConfiguracaoAcademicaService(
                configuracaoRepository,
                cursoRepository,
                ofertaRepository,
                matriculaRepository
        );
    }

    @Test
    void deveRejeitarUsuarioNulo() {
        verificarErro(HttpStatus.FORBIDDEN, () ->
                service.salvar(null, request(List.of(10L))));
    }

    @Test
    void deveRejeitarUsuarioNaoEstudante() {
        Usuario usuario = estudante();
        usuario.setTipo("PROFESSOR");

        verificarErro(HttpStatus.FORBIDDEN, () ->
                service.salvar(usuario, request(List.of(10L))));
    }

    @Test
    void deveRejeitarRequestNulo() {
        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), null));
    }

    @Test
    void deveRejeitarCursoNulo() {
        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(),
                        new ConfiguracaoAcademicaRequest(null, 1, List.of(10L))));
    }

    @Test
    void deveRejeitarSemestreNulo() {
        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(),
                        new ConfiguracaoAcademicaRequest(1L, null, List.of(10L))));
    }

    @Test
    void deveRejeitarListaNula() {
        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(),
                        new ConfiguracaoAcademicaRequest(1L, 1, null)));
    }

    @Test
    void deveRejeitarCursoInexistente() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), request(List.of(10L))));
    }

    @Test
    void deveRejeitarSemestreInvalido() {
        prepararCurso();

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(),
                        new ConfiguracaoAcademicaRequest(1L, 0, List.of(10L))));
    }

    @Test
    void deveRejeitarSemDisciplinasDisponiveis() {
        prepararCurso();

        when(ofertaRepository.findByCursoIdAndSemestre(1L, 1))
                .thenReturn(List.of());

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), request(List.of(10L))));
    }

    @Test
    void deveRejeitarListaVazia() {
        prepararOfertas();

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), request(List.of())));
    }

    @Test
    void deveRejeitarDisciplinaNula() {
        prepararOfertas();

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(),
                        request(java.util.Arrays.asList(10L, null))));
    }

    @Test
    void deveRejeitarDisciplinasDuplicadas() {
        prepararOfertas();

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), request(List.of(10L, 10L))));
    }

    @Test
    void deveRejeitarDisciplinaDeOutroCurso() {
        prepararOfertas();

        verificarErro(HttpStatus.BAD_REQUEST, () ->
                service.salvar(estudante(), request(List.of(99L))));
    }

    @Test
    void deveSalvarNovaConfiguracao() {
        prepararOfertas();

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        when(configuracaoRepository.save(any(ConfiguracaoAcademica.class)))
                .thenAnswer(invocacao -> {
                    ConfiguracaoAcademica configuracao = invocacao.getArgument(0);
                    configuracao.setId(5L);
                    return configuracao;
                });

        when(matriculaRepository.findByConfiguracaoAcademicaId(5L))
                .thenReturn(List.of());

        when(ofertaRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(oferta(10L)));

        service.salvar(estudante(), request(List.of(10L)));

        verify(configuracaoRepository).save(any(ConfiguracaoAcademica.class));
        verify(matriculaRepository).save(any(MatriculaDisciplina.class));
        verify(matriculaRepository).flush();
    }

    @Test
    void deveSubstituirMatriculasAnteriores() {
        prepararOfertas();

        ConfiguracaoAcademica existente = new ConfiguracaoAcademica();
        existente.setId(5L);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        when(configuracaoRepository.save(any(ConfiguracaoAcademica.class)))
                .thenReturn(existente);

        MatriculaDisciplina antiga = new MatriculaDisciplina();

        when(matriculaRepository.findByConfiguracaoAcademicaId(5L))
                .thenReturn(List.of(antiga));

        when(ofertaRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(oferta(10L)));

        service.salvar(estudante(), request(List.of(10L)));

        verify(matriculaRepository).deleteAll(List.of(antiga));
        verify(matriculaRepository).flush();
        verify(matriculaRepository).save(any(MatriculaDisciplina.class));
    }

    private Usuario estudante() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTipo("ESTUDANTE");
        return usuario;
    }

    private ConfiguracaoAcademicaRequest request(List<Long> ids) {
    return new ConfiguracaoAcademicaRequest(
            1L,
            1,
            new java.util.ArrayList<>(ids)
    );
}

    private void prepararCurso() {
        Curso curso = new Curso();
        curso.setId(1L);

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));
    }

    private OfertaDisciplina oferta(Long id) {
        OfertaDisciplina oferta = new OfertaDisciplina();
        oferta.setId(id);
        return oferta;
    }

    private void prepararOfertas() {
        prepararCurso();

        when(ofertaRepository.findByCursoIdAndSemestre(1L, 1))
                .thenReturn(List.of(oferta(10L), oferta(20L)));
    }

    private void verificarErro(
            HttpStatus status,
            org.junit.jupiter.api.function.Executable acao) {

        ResponseStatusException erro =
                assertThrows(ResponseStatusException.class, acao);

        assertEquals(status, erro.getStatusCode());
    }
}
