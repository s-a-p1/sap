
package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.service.AssuntoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssuntoControllerTest {

    private AssuntoService assuntoService;
    private AssuntoController controller;

    @BeforeEach
    void configurar() {
        assuntoService = mock(AssuntoService.class);
        controller = new AssuntoController(assuntoService);
    }

    @Test
    void deveListarTodosOsAssuntos() {
        Assunto assunto1 = mock(Assunto.class);
        Assunto assunto2 = mock(Assunto.class);

        List<Assunto> assuntos = List.of(assunto1, assunto2);

        when(assuntoService.listarTodos()).thenReturn(assuntos);

        List<Assunto> resultado = controller.listarTodos();

        assertEquals(2, resultado.size());
        assertSame(assuntos, resultado);
        verify(assuntoService).listarTodos();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremAssuntos() {
        when(assuntoService.listarTodos()).thenReturn(List.of());

        List<Assunto> resultado = controller.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(assuntoService).listarTodos();
    }

    @Test
    void deveListarAssuntosPorDisciplina() {
        Long disciplinaId = 5L;

        Assunto assunto = mock(Assunto.class);

        List<Assunto> assuntos = List.of(assunto);

        when(assuntoService.listarPorDisciplina(disciplinaId))
                .thenReturn(assuntos);

        List<Assunto> resultado =
                controller.listarPorDisciplina(disciplinaId);

        assertEquals(1, resultado.size());
        assertSame(assunto, resultado.get(0));

        verify(assuntoService).listarPorDisciplina(disciplinaId);
    }

    @Test
    void deveRetornarListaVaziaParaDisciplinaSemAssuntos() {
        when(assuntoService.listarPorDisciplina(99L))
                .thenReturn(List.of());

        List<Assunto> resultado =
                controller.listarPorDisciplina(99L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(assuntoService).listarPorDisciplina(99L);
    }

    @Test
    void deveCadastrarAssuntoComSucesso() {
        Assunto assunto = mock(Assunto.class);
        Assunto assuntoSalvo = mock(Assunto.class);

        when(assuntoService.cadastrar(assunto))
                .thenReturn(assuntoSalvo);

        ResponseEntity<Assunto> resposta =
                controller.cadastrar(assunto);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(assuntoSalvo, resposta.getBody());

        verify(assuntoService).cadastrar(assunto);
    }

    @Test
    void deveExcluirAssuntoComSucesso() {
        ResponseEntity<Void> resposta =
                controller.excluir(10L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertNull(resposta.getBody());

        verify(assuntoService).excluir(10L);
    }

    @Test
    void deveTratarErroDeCadastro() {
        IllegalArgumentException erro =
                new IllegalArgumentException("Dados inválidos.");

        ResponseEntity<Map<String, String>> resposta =
                controller.tratarErroCadastro(erro);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Dados inválidos."),
                resposta.getBody()
        );
    }

    @Test
    void deveTratarAssuntoComVinculos() {
        IllegalStateException erro =
                new IllegalStateException(
                        "Assunto possui registros vinculados."
                );

        ResponseEntity<Map<String, String>> resposta =
                controller.tratarAssuntoComVinculos(erro);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", "Assunto possui registros vinculados."),
                resposta.getBody()
        );
    }

    @Test
    void devePropagarErroDoServiceAoCadastrar() {
        Assunto assunto = mock(Assunto.class);

        when(assuntoService.cadastrar(assunto))
                .thenThrow(new IllegalArgumentException(
                        "Assunto inválido."
                ));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.cadastrar(assunto)
        );

        assertEquals("Assunto inválido.", erro.getMessage());
    }

    @Test
    void devePropagarErroDoServiceAoExcluir() {
        doThrow(new IllegalStateException(
                "Assunto possui vínculos."
        )).when(assuntoService).excluir(10L);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.excluir(10L)
        );

        assertEquals("Assunto possui vínculos.", erro.getMessage());
    }
}
