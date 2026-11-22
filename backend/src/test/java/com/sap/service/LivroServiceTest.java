
package com.sap.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivroServiceTest {

    private LivroService service;

    @BeforeEach
    void configurar() {
        service = new LivroService();
    }

    @Test
    void deveRejeitarBuscaNula() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarLivros(null)
        );

        assertEquals(
                "Informe um termo para buscar livros.",
                erro.getMessage()
        );
    }

    @Test
    void deveRejeitarBuscaVazia() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarLivros("")
        );

        assertEquals(
                "Informe um termo para buscar livros.",
                erro.getMessage()
        );
    }

    @Test
    void deveRejeitarBuscaComEspacos() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarLivros("    ")
        );

        assertEquals(
                "Informe um termo para buscar livros.",
                erro.getMessage()
        );
    }

    @Test
    void deveRejeitarBuscaComTabulacoes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarLivros("\t\t")
        );
    }

    @Test
    void deveRejeitarBuscaComQuebrasDeLinha() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarLivros("\n\n")
        );
    }
}
