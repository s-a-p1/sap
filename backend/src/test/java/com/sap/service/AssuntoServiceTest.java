
package com.sap.service;

import com.sap.model.Assunto;
import com.sap.model.Disciplina;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.DisciplinaRepository;
import com.sap.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssuntoServiceTest {

    @Mock
    private AssuntoRepository assuntoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    private AssuntoService service;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);

        service = new AssuntoService(
                assuntoRepository,
                disciplinaRepository,
                avaliacaoRepository,
                questaoRepository
        );

        when(assuntoRepository.save(any(Assunto.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void deveListarTodosOsAssuntos() {
        List<Assunto> assuntos = List.of(new Assunto(), new Assunto());

        when(assuntoRepository.findAll()).thenReturn(assuntos);

        assertEquals(2, service.listarTodos().size());

        verify(assuntoRepository).findAll();
    }

    @Test
    void deveListarAssuntosPorDisciplina() {
        when(assuntoRepository.listarAssuntosEspecificos(1L))
                .thenReturn(List.of(new Assunto()));

        assertEquals(1, service.listarPorDisciplina(1L).size());

        verify(assuntoRepository).listarAssuntosEspecificos(1L);
    }

    @Test
    void deveCadastrarAssunto() {
        Assunto assunto = assuntoValido();

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(assunto.getDisciplina()));

        Assunto salvo = service.cadastrar(assunto);

        assertEquals("Programação", salvo.getNome());
        assertNotNull(salvo.getDisciplina());

        verify(assuntoRepository).save(assunto);
    }

    @Test
    void deveRejeitarNomeNulo() {
        Assunto assunto = assuntoValido();
        assunto.setNome(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(assunto));

        verify(assuntoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        Assunto assunto = assuntoValido();
        assunto.setNome("   ");

        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(assunto));

        verify(assuntoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarDisciplinaNula() {
        Assunto assunto = assuntoValido();
        assunto.setDisciplina(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(assunto));
    }

    @Test
    void deveRejeitarDisciplinaSemId() {
        Assunto assunto = assuntoValido();
        assunto.setDisciplina(new Disciplina());

        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(assunto));
    }

    @Test
    void deveRejeitarDisciplinaInexistente() {
        Assunto assunto = assuntoValido();

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(assunto));
    }

    @Test
    void deveExcluirAssuntoSemVinculos() {
        Assunto assunto = new Assunto();

        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(assunto));

        service.excluir(1L);

        verify(assuntoRepository).delete(assunto);
    }

    @Test
    void deveRejeitarExclusaoDeAssuntoInexistente() {
        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.excluir(1L));

        verify(assuntoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComAvaliacoes() {
        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(new Assunto()));

        when(avaliacaoRepository.existsByAssunto_Id(1L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.excluir(1L));

        verify(assuntoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComQuestoes() {
        when(assuntoRepository.findById(1L))
                .thenReturn(Optional.of(new Assunto()));

        when(questaoRepository.existsByAssunto_Id(1L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.excluir(1L));

        verify(assuntoRepository, never()).delete(any());
    }

    private Assunto assuntoValido() {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(1L);

        Assunto assunto = new Assunto();
        assunto.setNome("Programação");
        assunto.setDisciplina(disciplina);

        return assunto;
    }
}
