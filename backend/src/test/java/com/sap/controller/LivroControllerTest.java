
package com.sap.controller;

import com.sap.dto.LivroResponse;
import com.sap.service.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivroControllerTest {

    private LivroService livroService;
    private LivroController controller;

    @BeforeEach
    void configurar() {
        livroService = mock(LivroService.class);
        controller = new LivroController(livroService);
    }

    @Test
    void deveBuscarLivrosComSucesso() {
        LivroResponse primeiro = mock(LivroResponse.class);
        LivroResponse segundo = mock(LivroResponse.class);

        List<LivroResponse> livros = List.of(primeiro, segundo);

        when(livroService.buscarLivros("Java"))
                .thenReturn(livros);

        List<LivroResponse> resposta = controller.buscar("Java");

        assertNotNull(resposta);
        assertEquals(2, resposta.size());
        assertSame(primeiro, resposta.get(0));
        assertSame(segundo, resposta.get(1));

        verify(livroService).buscarLivros("Java");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemLivros() {
        when(livroService.buscarLivros("Assunto inexistente"))
                .thenReturn(List.of());

        List<LivroResponse> resposta =
                controller.buscar("Assunto inexistente");

        assertNotNull(resposta);
        assertTrue(resposta.isEmpty());

        verify(livroService).buscarLivros("Assunto inexistente");
    }

    @Test
    void deveEncaminharTermoDeBuscaCorretamente() {
        controller.buscar("Spring Boot");

        verify(livroService, times(1))
                .buscarLivros("Spring Boot");
    }

    @Test
    void deveEncaminharBuscaVaziaAoService() {
        controller.buscar("");

        verify(livroService).buscarLivros("");
    }

    @Test
    void devePropagarErroDoService() {
        when(livroService.buscarLivros("Java"))
                .thenThrow(new IllegalArgumentException(
                        "Erro ao buscar livros."
                ));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.buscar("Java")
        );

        assertEquals("Erro ao buscar livros.", erro.getMessage());
    }
}
