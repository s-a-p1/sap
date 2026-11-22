
package com.sap.controller;

import com.sap.model.Curso;
import com.sap.model.SemestreCurso;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.CursoRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import com.sap.repository.SemestreCursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SemestreCursoControllerTest {

    private SemestreCursoRepository semestreCursoRepository;
    private CursoRepository cursoRepository;
    private OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private SemestreCursoController controller;

    @BeforeEach
    void configurar() {
        semestreCursoRepository = mock(SemestreCursoRepository.class);
        cursoRepository = mock(CursoRepository.class);
        ofertaDisciplinaRepository = mock(OfertaDisciplinaRepository.class);
        configuracaoAcademicaRepository =
                mock(ConfiguracaoAcademicaRepository.class);

        controller = new SemestreCursoController(
                semestreCursoRepository,
                cursoRepository,
                ofertaDisciplinaRepository,
                configuracaoAcademicaRepository
        );
    }

    @Test
    void deveListarSemestresDeUmCurso() {
        SemestreCurso primeiro = semestre(10L, 5L, 1);
        SemestreCurso segundo = semestre(11L, 5L, 2);

        when(semestreCursoRepository.findByCurso_IdOrderByNumeroAsc(5L))
                .thenReturn(List.of(primeiro, segundo));

        List<Map<String, Object>> resposta =
                controller.listarPorCurso(5L);

        assertEquals(2, resposta.size());

        assertEquals(10L, resposta.get(0).get("id"));
        assertEquals(5L, resposta.get(0).get("cursoId"));
        assertEquals(1, resposta.get(0).get("numero"));

        assertEquals(11L, resposta.get(1).get("id"));
        assertEquals(5L, resposta.get(1).get("cursoId"));
        assertEquals(2, resposta.get(1).get("numero"));

        verify(semestreCursoRepository)
                .findByCurso_IdOrderByNumeroAsc(5L);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemSemestres() {
        when(semestreCursoRepository.findByCurso_IdOrderByNumeroAsc(5L))
                .thenReturn(List.of());

        List<Map<String, Object>> resposta =
                controller.listarPorCurso(5L);

        assertNotNull(resposta);
        assertTrue(resposta.isEmpty());
    }

    @Test
    void deveConsultarSemestresDoCursoCorreto() {
        controller.listarPorCurso(25L);

        verify(semestreCursoRepository)
                .findByCurso_IdOrderByNumeroAsc(25L);
    }

    @Test
    void deveRejeitarCadastroSemCurso() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("numero", 1L));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o curso e o número do semestre.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarCadastroSemNumero() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("cursoId", 5L));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o curso e o número do semestre.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarCadastroSemDados() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of());

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o curso e o número do semestre.");
    }

    @Test
    void deveRejeitarSemestreZero() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 5L,
                        "numero", 0L
                ));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "O semestre deve estar entre 1 e 20.");
    }

    @Test
    void deveRejeitarSemestreNegativo() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 5L,
                        "numero", -1L
                ));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "O semestre deve estar entre 1 e 20.");
    }

    @Test
    void deveRejeitarSemestreMaiorQueVinte() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 5L,
                        "numero", 21L
                ));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "O semestre deve estar entre 1 e 20.");
    }

    @Test
    void deveRejeitarCursoInexistente() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 99L,
                        "numero", 1L
                ));

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Curso não encontrado.");

        verify(cursoRepository).findById(99L);
    }

    @Test
    void deveRejeitarSemestreDuplicado() {
        Curso curso = curso(5L);

        when(cursoRepository.findById(5L))
                .thenReturn(Optional.of(curso));

        when(semestreCursoRepository.existsByCurso_IdAndNumero(5L, 1))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 5L,
                        "numero", 1L
                ));

        assertErro(resposta, HttpStatus.CONFLICT,
                "Este semestre já está cadastrado no curso.");

        verify(semestreCursoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarPrimeiroSemestre() {
        verificarCadastroValido(1L);
    }

    @Test
    void deveCadastrarVigesimoSemestre() {
        verificarCadastroValido(20L);
    }

    @Test
    void deveCadastrarSemestreIntermediario() {
        verificarCadastroValido(8L);
    }

    @Test
    void devePreservarCursoENumeroAoCadastrar() {
        Curso curso = curso(5L);

        when(cursoRepository.findById(5L))
                .thenReturn(Optional.of(curso));

        SemestreCurso salvo = semestre(10L, 5L, 3);

        when(semestreCursoRepository.save(any(SemestreCurso.class)))
                .thenReturn(salvo);

        controller.cadastrar(Map.of(
                "cursoId", 5L,
                "numero", 3L
        ));

        ArgumentCaptor<SemestreCurso> captor =
                ArgumentCaptor.forClass(SemestreCurso.class);

        verify(semestreCursoRepository).save(captor.capture());

        SemestreCurso enviado = captor.getValue();

        assertSame(curso, enviado.getCurso());
        assertEquals(3, enviado.getNumero());
    }

    @Test
    void deveRejeitarExclusaoDeSemestreInexistente() {
        ResponseEntity<?> resposta = controller.excluir(99L);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Semestre não encontrado.");

        verify(semestreCursoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComDisciplinasOfertadas() {
        SemestreCurso semestre = semestre(10L, 5L, 2);

        when(semestreCursoRepository.findById(10L))
                .thenReturn(Optional.of(semestre));

        when(ofertaDisciplinaRepository.existsByCurso_IdAndSemestre(5L, 2))
                .thenReturn(true);

        ResponseEntity<?> resposta = controller.excluir(10L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este semestre porque ele possui disciplinas ofertadas.");

        verify(semestreCursoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComEstudantesVinculados() {
        SemestreCurso semestre = semestre(10L, 5L, 2);

        when(semestreCursoRepository.findById(10L))
                .thenReturn(Optional.of(semestre));

        when(configuracaoAcademicaRepository
                .existsByCurso_IdAndSemestre(5L, 2))
                .thenReturn(true);

        ResponseEntity<?> resposta = controller.excluir(10L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este semestre porque existem estudantes vinculados.");

        verify(semestreCursoRepository, never()).delete(any());
    }

    @Test
    void deveExcluirSemestreSemVinculos() {
        SemestreCurso semestre = semestre(10L, 5L, 2);

        when(semestreCursoRepository.findById(10L))
                .thenReturn(Optional.of(semestre));

        ResponseEntity<?> resposta = controller.excluir(10L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertNull(resposta.getBody());

        verify(semestreCursoRepository).delete(semestre);
    }

    @Test
    void deveVerificarOfertasAntesDeEstudantes() {
        SemestreCurso semestre = semestre(10L, 5L, 2);

        when(semestreCursoRepository.findById(10L))
                .thenReturn(Optional.of(semestre));

        controller.excluir(10L);

        var ordem = inOrder(
                ofertaDisciplinaRepository,
                configuracaoAcademicaRepository
        );

        ordem.verify(ofertaDisciplinaRepository)
                .existsByCurso_IdAndSemestre(5L, 2);

        ordem.verify(configuracaoAcademicaRepository)
                .existsByCurso_IdAndSemestre(5L, 2);
    }

    @Test
    void naoDeveVerificarEstudantesQuandoExistemOfertas() {
        SemestreCurso semestre = semestre(10L, 5L, 2);

        when(semestreCursoRepository.findById(10L))
                .thenReturn(Optional.of(semestre));

        when(ofertaDisciplinaRepository.existsByCurso_IdAndSemestre(5L, 2))
                .thenReturn(true);

        controller.excluir(10L);

        verifyNoInteractions(configuracaoAcademicaRepository);
    }

    private void verificarCadastroValido(Long numero) {
        Curso curso = curso(5L);

        when(cursoRepository.findById(5L))
                .thenReturn(Optional.of(curso));

        SemestreCurso salvo = semestre(
                10L, 5L, numero.intValue()
        );

        when(semestreCursoRepository.save(any(SemestreCurso.class)))
                .thenReturn(salvo);

        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of(
                        "cursoId", 5L,
                        "numero", numero
                ));

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        assertEquals(
                Map.of(
                        "id", 10L,
                        "cursoId", 5L,
                        "numero", numero.intValue()
                ),
                resposta.getBody()
        );

        verify(semestreCursoRepository)
                .existsByCurso_IdAndNumero(5L, numero.intValue());

        verify(semestreCursoRepository).save(any(SemestreCurso.class));
    }

    private Curso curso(Long id) {
        Curso curso = mock(Curso.class);

        when(curso.getId()).thenReturn(id);

        return curso;
    }

    private SemestreCurso semestre(
            Long id,
            Long cursoId,
            Integer numero) {

        SemestreCurso semestre = mock(SemestreCurso.class);
        Curso curso = mock(Curso.class);

        when(semestre.getId()).thenReturn(id);
        when(semestre.getNumero()).thenReturn(numero);
        when(semestre.getCurso()).thenReturn(curso);
        when(curso.getId()).thenReturn(cursoId);

        return semestre;
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
