
package com.sap.service;

import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.model.Disciplina;
import com.sap.model.MetaEstudo;
import com.sap.model.Notificacao;
import com.sap.model.PreferenciaNotificacao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.MetaEstudoRepository;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GeradorNotificacoesServiceTest {

    private MetaEstudoRepository metaEstudoRepository;
    private NotificacaoRepository notificacaoRepository;
    private PreferenciaNotificacaoService preferenciaService;
    private UsuarioRepository usuarioRepository;
    private ResultadoRepository resultadoRepository;
    private GeradorNotificacoesService service;

    private Usuario usuario;
    private PreferenciaNotificacao preferencias;

    @BeforeEach
    void configurar() {
        metaEstudoRepository = mock(MetaEstudoRepository.class);
        notificacaoRepository = mock(NotificacaoRepository.class);
        preferenciaService = mock(PreferenciaNotificacaoService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        resultadoRepository = mock(ResultadoRepository.class);

        service = new GeradorNotificacoesService(
                metaEstudoRepository,
                notificacaoRepository,
                preferenciaService,
                usuarioRepository,
                resultadoRepository
        );

        usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);

        preferencias = mock(PreferenciaNotificacao.class);

        when(preferenciaService.buscarOuCriar(1L))
                .thenReturn(preferencias);
    }

    @Test
    void naoDeveGerarLembretesQuandoNotificacoesEstiveremDesativadas() {
        when(preferencias.isNotificacoesAtivas()).thenReturn(false);

        service.gerarLembretesMetas(1L);

        verifyNoInteractions(metaEstudoRepository);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarLembretesQuandoCategoriasEstiveremDesativadas() {
        when(preferencias.isNotificacoesAtivas()).thenReturn(true);

        service.gerarLembretesMetas(1L);

        verifyNoInteractions(metaEstudoRepository);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarUsuarioInexistenteAoGerarLembretes() {
        configurarPreferenciasMetas(true, true, 3);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.gerarLembretesMetas(1L)
        );
    }

    @Test
    void deveGerarLembreteAntecipado() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar Java", LocalDate.now().plusDays(2));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        Notificacao notificacao = capturarNotificacao();

        assertEquals("Meta de estudo se aproximando", notificacao.getTitulo());
        assertEquals("PLANO_ESTUDOS", notificacao.getCategoria());
        assertEquals(10L, notificacao.getReferenciaId());
        assertTrue(notificacao.getReferenciaTipo().startsWith("META_LEMBRETE_ANTECIPADO_"));
        assertTrue(notificacao.getMensagem().contains("2 dia(s)"));
        assertFalse(notificacao.isLida());
    }

    @Test
    void deveGerarLembreteNoDiaDoVencimento() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar SQL", LocalDate.now());

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        Notificacao notificacao = capturarNotificacao();

        assertEquals("Meta de estudo para hoje", notificacao.getTitulo());
        assertTrue(notificacao.getMensagem().contains("Estudar SQL"));
        assertTrue(notificacao.getReferenciaTipo().startsWith("META_LEMBRETE_DIA_"));
    }

    @Test
    void naoDeveAnteciparLembreteForaDoPrazoConfigurado() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar Java", LocalDate.now().plusDays(5));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveAnteciparLembreteQuandoAntecedenciaForZero() {
        configurarPreferenciasMetas(true, false, 0);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar Java", LocalDate.now().plusDays(1));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveGerarNotificacaoDeMetaAtrasada() {
        configurarPreferenciasMetas(false, true, 0);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Finalizar projeto", LocalDate.now().minusDays(2));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        Notificacao notificacao = capturarNotificacao();

        assertEquals("Meta de estudo atrasada", notificacao.getTitulo());
        assertEquals("META_PENDENTE", notificacao.getCategoria());
        assertTrue(notificacao.getMensagem().contains("Finalizar projeto"));
        assertTrue(notificacao.getReferenciaTipo().startsWith("META_ATRASADA_"));
    }

    @Test
    void naoDeveGerarNotificacaoDeAtrasoQuandoPreferenciaEstiverDesativada() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar Java", LocalDate.now().minusDays(2));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveDuplicarLembreteExistente() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo meta = meta(10L, "Estudar Java", LocalDate.now());

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(meta));

        when(notificacaoRepository.existsByUsuarioIdAndReferenciaTipoAndReferenciaId(
                eq(1L), anyString(), eq(10L)
        )).thenReturn(true);

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveGerarNotificacoesParaDuasMetas() {
        configurarPreferenciasMetas(true, false, 3);
        configurarUsuario();

        MetaEstudo primeira = meta(10L, "Java", LocalDate.now());
        MetaEstudo segunda = meta(11L, "SQL", LocalDate.now().plusDays(1));

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(primeira, segunda));

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, times(2)).save(any(Notificacao.class));
    }

    @Test
    void naoDeveGerarNotificacoesSemMetas() {
        configurarPreferenciasMetas(true, true, 3);
        configurarUsuario();

        when(metaEstudoRepository.findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of());

        service.gerarLembretesMetas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarRevisoesQuandoNotificacoesEstiveremDesativadas() {
        when(preferencias.isNotificacoesAtivas()).thenReturn(false);

        service.gerarRecomendacoesRevisao(1L);

        verifyNoInteractions(resultadoRepository);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarRevisoesQuandoPreferenciaEstiverDesativada() {
        when(preferencias.isNotificacoesAtivas()).thenReturn(true);

        service.gerarRecomendacoesRevisao(1L);

        verifyNoInteractions(resultadoRepository);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarUsuarioInexistenteAoGerarRevisoes() {
        configurarPreferenciasRevisao();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.gerarRecomendacoesRevisao(1L)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "0, Revisão de alta prioridade",
            "49, Revisão de alta prioridade",
            "50, Revisão recomendada",
            "69, Revisão recomendada"
    })
    void deveGerarRevisaoConformePercentual(
            int percentual,
            String tituloEsperado
    ) {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(20L, 5L, percentual);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        service.gerarRecomendacoesRevisao(1L);

        Notificacao notificacao = capturarNotificacao();

        assertEquals(tituloEsperado, notificacao.getTitulo());
        assertEquals("RECOMENDACAO_REVISAO", notificacao.getCategoria());
        assertEquals("RESULTADO_REVISAO", notificacao.getReferenciaTipo());
        assertEquals(20L, notificacao.getReferenciaId());
        assertTrue(notificacao.getMensagem().contains(percentual + "%"));
    }

    @ParameterizedTest
    @CsvSource({"70", "85", "100"})
    void naoDeveGerarRevisaoComDesempenhoSatisfatorio(int percentual) {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(20L, 5L, percentual);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveIgnorarResultadoSemPercentual() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(20L, 5L, null);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveIgnorarResultadoSemAvaliacao() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = mock(Resultado.class);
        when(resultado.getAvaliacao()).thenReturn(null);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveConsiderarApenasResultadoMaisRecenteDaDisciplina() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado recente = resultado(20L, 5L, 80);
        Resultado antigo = resultado(21L, 5L, 30);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(recente, antigo));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveGerarRevisoesParaDisciplinasDiferentes() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado primeira = resultado(20L, 5L, 40);
        Resultado segunda = resultado(21L, 6L, 60);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(primeira, segunda));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, times(2)).save(any(Notificacao.class));
    }

    @Test
    void naoDeveDuplicarRecomendacaoExistente() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(20L, 5L, 40);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        when(notificacaoRepository.existsByUsuarioIdAndReferenciaTipoAndReferenciaId(
                1L, "RESULTADO_REVISAO", 20L
        )).thenReturn(true);

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveExcluirRecomendacaoAntigaSemResultadoValido() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Notificacao antiga = mock(Notificacao.class);
        when(antiga.getReferenciaId()).thenReturn(99L);

        when(notificacaoRepository.findByUsuarioIdAndCategoriaAndReferenciaTipo(
                1L, "RECOMENDACAO_REVISAO", "RESULTADO_REVISAO"
        )).thenReturn(List.of(antiga));

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of());

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository).delete(antiga);
    }

    @Test
    void deveManterRecomendacaoAindaValida() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(20L, 5L, 40);

        Notificacao existente = mock(Notificacao.class);
        when(existente.getReferenciaId()).thenReturn(20L);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        when(notificacaoRepository.findByUsuarioIdAndCategoriaAndReferenciaTipo(
                1L, "RECOMENDACAO_REVISAO", "RESULTADO_REVISAO"
        )).thenReturn(List.of(existente));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository, never()).delete(existente);
    }

    @Test
    void deveExcluirRecomendacaoQuandoDesempenhoMelhorar() {
        configurarPreferenciasRevisao();
        configurarUsuario();

        Resultado resultado = resultado(30L, 5L, 90);

        Notificacao antiga = mock(Notificacao.class);
        when(antiga.getReferenciaId()).thenReturn(20L);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        when(notificacaoRepository.findByUsuarioIdAndCategoriaAndReferenciaTipo(
                1L, "RECOMENDACAO_REVISAO", "RESULTADO_REVISAO"
        )).thenReturn(List.of(antiga));

        service.gerarRecomendacoesRevisao(1L);

        verify(notificacaoRepository).delete(antiga);
        verify(notificacaoRepository, never()).save(any());
    }

    private void configurarUsuario() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));
    }

    private void configurarPreferenciasMetas(
            boolean lembretes,
            boolean pendentes,
            int antecedencia
    ) {
        when(preferencias.isNotificacoesAtivas()).thenReturn(true);
        when(preferencias.isLembretesPlano()).thenReturn(lembretes);
        when(preferencias.isMetasPendentes()).thenReturn(pendentes);

        if (lembretes) {
            when(preferencias.getAntecedenciaLembretes())
                    .thenReturn(antecedencia);
        }
    }

    private void configurarPreferenciasRevisao() {
        when(preferencias.isNotificacoesAtivas()).thenReturn(true);
        when(preferencias.isRecomendacoesRevisao()).thenReturn(true);
    }

    private MetaEstudo meta(Long id, String titulo, LocalDate data) {
        MetaEstudo meta = mock(MetaEstudo.class);

        when(meta.getId()).thenReturn(id);
        when(meta.getTitulo()).thenReturn(titulo);
        when(meta.getDataPrevista()).thenReturn(data);

        return meta;
    }

    private Resultado resultado(Long id, Long disciplinaId, Integer percentual) {
        Disciplina disciplina = mock(Disciplina.class);
        Assunto assunto = mock(Assunto.class);
        Avaliacao avaliacao = mock(Avaliacao.class);
        Resultado resultado = mock(Resultado.class);

        when(disciplina.getId()).thenReturn(disciplinaId);
        when(assunto.getDisciplina()).thenReturn(disciplina);
        when(assunto.getNome()).thenReturn("Programação");
        when(avaliacao.getAssunto()).thenReturn(assunto);
        when(resultado.getAvaliacao()).thenReturn(avaliacao);

        if (percentual != null) {
            when(resultado.getPercentual())
                    .thenReturn(BigDecimal.valueOf(percentual));
            when(resultado.getId()).thenReturn(id);
        }

        return resultado;
    }

    private Notificacao capturarNotificacao() {
        ArgumentCaptor<Notificacao> captor =
                ArgumentCaptor.forClass(Notificacao.class);

        verify(notificacaoRepository).save(captor.capture());

        return captor.getValue();
    }
}
