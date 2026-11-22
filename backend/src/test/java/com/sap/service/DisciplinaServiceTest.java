
package com.sap.service;

import com.sap.model.Disciplina;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.DisciplinaRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisciplinaServiceTest {

    private DisciplinaRepository disciplinaRepository;
    private AssuntoRepository assuntoRepository;
    private OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private DisciplinaService service;

    @BeforeEach
    void configurar() {
        disciplinaRepository = mock(DisciplinaRepository.class);
        assuntoRepository = mock(AssuntoRepository.class);
        ofertaDisciplinaRepository = mock(OfertaDisciplinaRepository.class);

        service = new DisciplinaService(
                disciplinaRepository,
                assuntoRepository,
                ofertaDisciplinaRepository
        );
    }

    @Test
    void deveListarTodasAsDisciplinas() {
        Disciplina primeira = disciplina(1L, "Matemática");
        Disciplina segunda = disciplina(2L, "Programação");

        when(disciplinaRepository.findAll())
                .thenReturn(List.of(primeira, segunda));

        List<Disciplina> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        assertEquals("Matemática", resultado.get(0).getNome());
        assertEquals("Programação", resultado.get(1).getNome());

        verify(disciplinaRepository).findAll();
    }

    @Test
    void deveRetornarListaVazia() {
        when(disciplinaRepository.findAll()).thenReturn(List.of());

        List<Disciplina> resultado = service.listarTodas();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveCadastrarDisciplinaValida() {
        Disciplina disciplina = disciplina(99L, "Programação");

        when(disciplinaRepository.save(any(Disciplina.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Disciplina resultado = service.cadastrar(disciplina);

        assertEquals("Programação", resultado.getNome());
        assertNull(resultado.getId());

        verify(disciplinaRepository).save(disciplina);
    }

    @Test
    void deveRemoverEspacosDoNome() {
        Disciplina disciplina = disciplina(null, "  Banco de Dados  ");

        when(disciplinaRepository.save(any(Disciplina.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Disciplina resultado = service.cadastrar(disciplina);

        assertEquals("Banco de Dados", resultado.getNome());
    }

    @Test
    void deveLimparIdAntesDoCadastro() {
        Disciplina disciplina = disciplina(50L, "Redes");

        when(disciplinaRepository.save(any(Disciplina.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        service.cadastrar(disciplina);

        ArgumentCaptor<Disciplina> captor =
                ArgumentCaptor.forClass(Disciplina.class);

        verify(disciplinaRepository).save(captor.capture());

        assertNull(captor.getValue().getId());
    }

    @Test
    void deveRejeitarNomeNulo() {
        Disciplina disciplina = disciplina(null, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(disciplina)
        );

        verify(disciplinaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNomeVazio() {
        Disciplina disciplina = disciplina(null, "");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(disciplina)
        );

        verify(disciplinaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarNomeComApenasEspacos() {
        Disciplina disciplina = disciplina(null, "    ");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(disciplina)
        );

        verify(disciplinaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarExclusaoDeDisciplinaInexistente() {
        when(disciplinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.excluir(99L)
        );

        verify(disciplinaRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComAssuntosCadastrados() {
        Disciplina disciplina = disciplina(1L, "Matemática");

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(disciplina));

        when(assuntoRepository.existsByDisciplina_Id(1L))
                .thenReturn(true);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> service.excluir(1L)
        );

        assertTrue(erro.getMessage().contains("assuntos cadastrados"));

        verify(disciplinaRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComVinculoAoCurso() {
        Disciplina disciplina = disciplina(1L, "Matemática");

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(disciplina));

        when(assuntoRepository.existsByDisciplina_Id(1L))
                .thenReturn(false);

        when(ofertaDisciplinaRepository.existsByDisciplina_Id(1L))
                .thenReturn(true);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> service.excluir(1L)
        );

        assertTrue(erro.getMessage().contains("vinculada a um curso"));

        verify(disciplinaRepository, never()).delete(any());
    }

    @Test
    void deveExcluirDisciplinaSemVinculos() {
        Disciplina disciplina = disciplina(1L, "Matemática");

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(disciplina));

        when(assuntoRepository.existsByDisciplina_Id(1L))
                .thenReturn(false);

        when(ofertaDisciplinaRepository.existsByDisciplina_Id(1L))
                .thenReturn(false);

        service.excluir(1L);

        verify(disciplinaRepository).delete(disciplina);
    }

    @Test
    void deveConsultarAssuntosAntesDasOfertas() {
        Disciplina disciplina = disciplina(1L, "Matemática");

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(disciplina));

        service.excluir(1L);

        var ordem = inOrder(assuntoRepository, ofertaDisciplinaRepository);

        ordem.verify(assuntoRepository).existsByDisciplina_Id(1L);
        ordem.verify(ofertaDisciplinaRepository).existsByDisciplina_Id(1L);
    }

    @Test
    void naoDeveConsultarOfertasSeExistiremAssuntos() {
        Disciplina disciplina = disciplina(1L, "Matemática");

        when(disciplinaRepository.findById(1L))
                .thenReturn(Optional.of(disciplina));

        when(assuntoRepository.existsByDisciplina_Id(1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.excluir(1L)
        );

        verifyNoInteractions(ofertaDisciplinaRepository);
    }

    private Disciplina disciplina(Long id, String nome) {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(id);
        disciplina.setNome(nome);
        return disciplina;
    }
}
