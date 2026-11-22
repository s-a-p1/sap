
package com.sap.controller;

import com.sap.model.AnaliseDesempenho;
import com.sap.service.AnaliseDesempenhoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnaliseDesempenhoControllerTest {

    private AnaliseDesempenhoService service;
    private AnaliseDesempenhoController controller;

    @BeforeEach
    void configurar() {
        service = mock(AnaliseDesempenhoService.class);
        controller = new AnaliseDesempenhoController(service);
    }

    @Test
    void deveRealizarAnaliseComSucesso() {
        AnaliseDesempenho analise = mock(AnaliseDesempenho.class);

        when(service.analisar(8, 10)).thenReturn(analise);

        AnaliseDesempenho resultado = controller.analisar(8, 10);

        assertSame(analise, resultado);
        verify(service).analisar(8, 10);
    }

    @Test
    void devePermitirAnaliseComZeroAcertos() {
        AnaliseDesempenho analise = mock(AnaliseDesempenho.class);

        when(service.analisar(0, 10)).thenReturn(analise);

        AnaliseDesempenho resultado = controller.analisar(0, 10);

        assertSame(analise, resultado);
        verify(service).analisar(0, 10);
    }

    @Test
    void deveRetornarBadRequestQuandoDadosInvalidos() {
        when(service.analisar(12, 10))
                .thenThrow(new IllegalArgumentException(
                        "Quantidade de acertos inválida."
                ));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.analisar(12, 10)
        );

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        assertEquals(
                "Quantidade de acertos inválida.",
                erro.getReason()
        );
    }

    @Test
    void devePropagarErrosInesperados() {
        when(service.analisar(5, 10))
                .thenThrow(new IllegalStateException(
                        "Falha inesperada."
                ));

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.analisar(5, 10)
        );

        assertEquals("Falha inesperada.", erro.getMessage());
    }
}
