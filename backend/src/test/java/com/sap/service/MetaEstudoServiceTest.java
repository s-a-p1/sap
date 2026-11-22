
package com.sap.service;

import com.sap.model.Assunto;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Curso;
import com.sap.model.DesempenhoAssunto;
import com.sap.model.Disciplina;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.MetaEstudo;
import com.sap.model.OfertaDisciplina;
import com.sap.model.Resultado;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.MetaEstudoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MetaEstudoServiceTest {

    private MetaEstudoRepository metaEstudoRepository;
    private DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private MatriculaDisciplinaRepository matriculaDisciplinaRepository;
    private MetaEstudoService service;

    @BeforeEach
    void configurar() {
        metaEstudoRepository = mock(MetaEstudoRepository.class);
        desempenhoAssuntoRepository = mock(DesempenhoAssuntoRepository.class);
        configuracaoAcademicaRepository = mock(ConfiguracaoAcademicaRepository.class);
        matriculaDisciplinaRepository = mock(MatriculaDisciplinaRepository.class);

        service = new MetaEstudoService(
                metaEstudoRepository,
                desempenhoAssuntoRepository,
                configuracaoAcademicaRepository,
                matriculaDisciplinaRepository
        );
    }

    @Test
    void deveRejeitarUsuarioNuloAoGerarMetas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.gerarMetasAutomaticamente(null)
        );

        verifyNoInteractions(metaEstudoRepository);
    }

    @Test
    void naoDeveGerarMetasSemConfiguracaoAcademica() {
        when(configuracaoAcademicaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        service.gerarMetasAutomaticamente(1L);

        verifyNoInteractions(desempenhoAssuntoRepository);
        verify(metaEstudoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarMetasSemDisciplinasMatriculadas() {
        configurarMatricula();

        when(matriculaDisciplinaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of());

        service.gerarMetasAutomaticamente(1L);

        verifyNoInteractions(desempenhoAssuntoRepository);
        verify(metaEstudoRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "0, ALTA, 2, Revisar fundamentos de",
            "49, ALTA, 2, Revisar fundamentos de",
            "50, MEDIA, 4, Reforçar conhecimentos em",
            "69, MEDIA, 4, Reforçar conhecimentos em",
            "70, BAIXA, 7, Aprofundar conhecimentos em",
            "100, BAIXA, 7, Aprofundar conhecimentos em"
    })
    void deveGerarMetaConformeDesempenho(
            int percentual,
            String prioridade,
            int dias,
            String prefixo
    ) {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 5L, percentual, 20L);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        when(metaEstudoRepository
                .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.empty());

        service.gerarMetasAutomaticamente(1L);

        MetaEstudo meta = capturarMeta();

        assertEquals(1L, meta.getUsuarioId());
        assertEquals(prioridade, meta.getPrioridade());
        assertEquals(prefixo + " Programação", meta.getTitulo());
        assertEquals(LocalDate.now().plusDays(dias), meta.getDataPrevista());
        assertEquals(false, meta.getConcluida());
        assertEquals("AUTOMATICA", meta.getOrigem());
        assertNotNull(meta.getResultadoOrigem());
    }

    @Test
    void naoDeveGerarMetaParaDisciplinaNaoMatriculada() {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 99L, 30, 20L);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarMetaParaAssuntoNulo() {
        configurarMatricula();

        DesempenhoAssunto desempenho = mock(DesempenhoAssunto.class);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository, never()).save(any());
    }

    @Test
    void naoDeveDuplicarAssuntoProcessado() {
        configurarMatricula();

        DesempenhoAssunto recente = desempenho(100L, 5L, 40, 20L);
        DesempenhoAssunto antigo = desempenho(100L, 5L, 60, 21L);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(recente, antigo));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository, times(1)).save(any(MetaEstudo.class));
    }

    @Test
    void naoDeveAtualizarMetaComMesmoResultado() {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 5L, 40, 20L);
        Resultado resultado = desempenho.getResultado();

        MetaEstudo existente = new MetaEstudo();
        existente.setResultadoOrigem(resultado);
        existente.setConcluida(false);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        when(metaEstudoRepository
                .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(existente));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarMetaPendenteComNovoResultado() {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 5L, 55, 20L);

        MetaEstudo existente = new MetaEstudo();
        existente.setConcluida(false);
        existente.setTitulo("Meta antiga");

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        when(metaEstudoRepository
                .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(existente));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository).save(existente);

        assertEquals("MEDIA", existente.getPrioridade());
        assertEquals("Reforçar conhecimentos em Programação", existente.getTitulo());
        assertEquals(LocalDate.now().plusDays(4), existente.getDataPrevista());
    }

    @Test
    void naoDeveReabrirMetaConcluidaSemResultadoDeOrigem() {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 5L, 40, 20L);

        MetaEstudo existente = new MetaEstudo();
        existente.setConcluida(true);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        when(metaEstudoRepository
                .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(existente));

        service.gerarMetasAutomaticamente(1L);

        verify(metaEstudoRepository, never()).save(any());
    }

    @Test
    void deveCriarNovaMetaQuandoResultadoForMaisRecente() {
        configurarMatricula();

        DesempenhoAssunto desempenho = desempenho(100L, 5L, 40, 20L);

        Resultado anterior = mock(Resultado.class);
        when(anterior.getId()).thenReturn(19L);
        when(anterior.getDataRealizacao())
                .thenReturn(LocalDateTime.now().minusDays(5));

        MetaEstudo existente = new MetaEstudo();
        existente.setConcluida(true);
        existente.setResultadoOrigem(anterior);

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        when(metaEstudoRepository
                .findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(1L, 100L))
                .thenReturn(Optional.of(existente));

        service.gerarMetasAutomaticamente(1L);

        MetaEstudo nova = capturarMeta();

        assertFalse(nova.getConcluida());
        assertEquals("ALTA", nova.getPrioridade());
    }

    @Test
    void deveRejeitarUsuarioNuloAoListarMetas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.listarMetas(null)
        );
    }

    @Test
    void deveRetornarListaVaziaSemMatricula() {
        when(configuracaoAcademicaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        assertTrue(service.listarMetas(1L).isEmpty());

        verify(metaEstudoRepository, never())
                .findByUsuarioIdOrderByDataPrevistaAsc(anyLong());
    }

    @Test
    void deveListarApenasMetasDeDisciplinasMatriculadas() {
        configurarMatricula();

        MetaEstudo valida = meta(1L, 5L);
        MetaEstudo invalida = meta(1L, 99L);

        when(metaEstudoRepository.findByUsuarioIdOrderByDataPrevistaAsc(1L))
                .thenReturn(List.of(valida, invalida));

        List<MetaEstudo> resultado = service.listarMetas(1L);

        assertEquals(1, resultado.size());
        assertSame(valida, resultado.get(0));
    }

    @Test
    void deveRejeitarDadosNulosAoAtualizarConclusao() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(null, 1L, true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(1L, null, true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(1L, 1L, null)
        );
    }

    @Test
    void deveRejeitarMetaInexistente() {
        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(10L, 1L, true)
        );
    }

    @Test
    void deveRejeitarMetaDeOutroUsuario() {
        MetaEstudo meta = meta(2L, 5L);

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(10L, 1L, true)
        );
    }

    @Test
    void deveRejeitarMetaForaDaMatriculaAtual() {
        configurarMatricula();

        MetaEstudo meta = meta(1L, 99L);

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarConclusao(10L, 1L, true)
        );
    }

    @Test
    void deveConcluirMetaManualSemCriarProximaEtapa() {
        configurarMatricula();

        MetaEstudo meta = meta(1L, 5L);
        meta.setOrigem("MANUAL");
        meta.setConcluida(false);

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        when(metaEstudoRepository.save(any(MetaEstudo.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        MetaEstudo resultado = service.atualizarConclusao(10L, 1L, true);

        assertTrue(resultado.getConcluida());
        verify(metaEstudoRepository, times(1)).save(any());
    }

    @Test
    void deveReabrirMetaConcluida() {
        configurarMatricula();

        MetaEstudo meta = meta(1L, 5L);
        meta.setConcluida(true);

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        when(metaEstudoRepository.save(any(MetaEstudo.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        MetaEstudo resultado = service.atualizarConclusao(10L, 1L, false);

        assertFalse(resultado.getConcluida());
        verify(metaEstudoRepository, times(1)).save(any());
    }

    @Test
    void deveCriarEtapaDePraticaAoConcluirRevisao() {
        configurarMatricula();

        MetaEstudo meta = meta(1L, 5L);
        meta.setTitulo("Revisar fundamentos de Programação");
        meta.setOrigem("AUTOMATICA");
        meta.setConcluida(false);
        meta.setResultadoOrigem(mock(Resultado.class));
        meta.setPrioridade("ALTA");

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        when(metaEstudoRepository.save(any(MetaEstudo.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        service.atualizarConclusao(10L, 1L, true);

        ArgumentCaptor<MetaEstudo> captor =
                ArgumentCaptor.forClass(MetaEstudo.class);

        verify(metaEstudoRepository, times(2)).save(captor.capture());

        MetaEstudo proxima = captor.getAllValues().get(1);

        assertEquals("Praticar exercícios de Programação", proxima.getTitulo());
        assertEquals(LocalDate.now().plusDays(3), proxima.getDataPrevista());
        assertEquals("ALTA", proxima.getPrioridade());
        assertFalse(proxima.getConcluida());
    }

    @Test
    void deveCriarEtapaDeNovaAvaliacaoAposPratica() {
        configurarMatricula();

        MetaEstudo meta = meta(1L, 5L);
        meta.setTitulo("Praticar exercícios de Programação");
        meta.setOrigem("AUTOMATICA");
        meta.setConcluida(false);
        meta.setResultadoOrigem(mock(Resultado.class));
        meta.setPrioridade("MEDIA");

        when(metaEstudoRepository.findById(10L))
                .thenReturn(Optional.of(meta));

        when(metaEstudoRepository.save(any(MetaEstudo.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        service.atualizarConclusao(10L, 1L, true);

        ArgumentCaptor<MetaEstudo> captor =
                ArgumentCaptor.forClass(MetaEstudo.class);

        verify(metaEstudoRepository, times(2)).save(captor.capture());

        MetaEstudo proxima = captor.getAllValues().get(1);

        assertEquals("Realizar nova avaliação de Programação", proxima.getTitulo());
        assertEquals(LocalDate.now().plusDays(5), proxima.getDataPrevista());
        assertEquals("MEDIA", proxima.getPrioridade());
    }

    private void configurarMatricula() {
        ConfiguracaoAcademica configuracao = mock(ConfiguracaoAcademica.class);
        Curso curso = mock(Curso.class);
        OfertaDisciplina oferta = mock(OfertaDisciplina.class);
        Disciplina disciplina = mock(Disciplina.class);
        MatriculaDisciplina matricula = mock(MatriculaDisciplina.class);

        when(configuracao.getId()).thenReturn(10L);
        when(configuracao.getCurso()).thenReturn(curso);
        when(configuracao.getSemestre()).thenReturn(1);
        when(curso.getId()).thenReturn(2L);

        when(oferta.getCurso()).thenReturn(curso);
        when(oferta.getSemestre()).thenReturn(1);
        when(oferta.getDisciplina()).thenReturn(disciplina);
        when(disciplina.getId()).thenReturn(5L);
        when(matricula.getOfertaDisciplina()).thenReturn(oferta);

        when(configuracaoAcademicaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaDisciplinaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of(matricula));
    }

    private DesempenhoAssunto desempenho(
            Long assuntoId,
            Long disciplinaId,
            int percentual,
            Long resultadoId
    ) {
        Disciplina disciplina = mock(Disciplina.class);
        Assunto assunto = mock(Assunto.class);
        Resultado resultado = mock(Resultado.class);
        DesempenhoAssunto desempenho = mock(DesempenhoAssunto.class);

        when(disciplina.getId()).thenReturn(disciplinaId);
        when(assunto.getId()).thenReturn(assuntoId);
        when(assunto.getNome()).thenReturn("Programação");
        when(assunto.getDisciplina()).thenReturn(disciplina);

        when(resultado.getId()).thenReturn(resultadoId);
        when(resultado.getDataRealizacao()).thenReturn(LocalDateTime.now());

        when(desempenho.getAssunto()).thenReturn(assunto);
        when(desempenho.getResultado()).thenReturn(resultado);
        when(desempenho.getPercentual())
                .thenReturn(BigDecimal.valueOf(percentual));

        return desempenho;
    }

    private MetaEstudo meta(Long usuarioId, Long disciplinaId) {
        MetaEstudo meta = new MetaEstudo();
        meta.setUsuarioId(usuarioId);

        Disciplina disciplina = mock(Disciplina.class);
        Assunto assunto = mock(Assunto.class);

        when(disciplina.getId()).thenReturn(disciplinaId);
        when(assunto.getDisciplina()).thenReturn(disciplina);
        when(assunto.getNome()).thenReturn("Programação");

        meta.setAssunto(assunto);
        meta.setTitulo("Estudar Programação");
        meta.setConcluida(false);

        return meta;
    }

    private MetaEstudo capturarMeta() {
        ArgumentCaptor<MetaEstudo> captor =
                ArgumentCaptor.forClass(MetaEstudo.class);

        verify(metaEstudoRepository).save(captor.capture());

        return captor.getValue();
    }
}
