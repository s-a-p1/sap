
package com.sap.service;

import com.sap.model.AnaliseDesempenho;
import com.sap.repository.AnaliseDesempenhoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnaliseDesempenhoServiceTest {

    @Mock
    private AnaliseDesempenhoRepository repository;

    private AnaliseDesempenhoService service;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        service = new AnaliseDesempenhoService(repository);
        when(repository.save(any(AnaliseDesempenho.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 100, 0.0, ALTA",
            "49, 100, 49.0, ALTA",
            "50, 100, 50.0, MEDIA",
            "69, 100, 69.0, MEDIA",
            "70, 100, 70.0, BAIXA",
            "100, 100, 100.0, BAIXA"
    })
    void deveClassificarDesempenho(
            int acertos,
            int total,
            double percentual,
            String classificacao) {

        AnaliseDesempenho analise = service.analisar(acertos, total);

        assertEquals(acertos, analise.getAcertos());
        assertEquals(total, analise.getTotalQuestoes());
        assertEquals(percentual, analise.getPercentual(), 0.001);
        assertEquals(classificacao, analise.getDificuldade());
        assertEquals(classificacao, analise.getPrioridade());
        assertNotNull(analise.getRecomendacao());

        verify(repository).save(any(AnaliseDesempenho.class));
    }

    @Test
    void deveRejeitarAcertosNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(null, 10));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarTotalNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(5, null));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarTotalZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(0, 0));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarTotalNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(0, -1));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarAcertosNegativos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(-1, 10));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarAcertosMaioresQueTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> service.analisar(11, 10));

        verifyNoInteractions(repository);
    }

    @Test
    void deveCalcularPercentualFracionado() {
        AnaliseDesempenho analise = service.analisar(1, 3);

        assertEquals(33.333, analise.getPercentual(), 0.001);
        assertEquals("ALTA", analise.getDificuldade());
    }
}
