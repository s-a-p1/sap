
package com.sap.service;

import com.sap.model.Avaliacao;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Disciplina;
import com.sap.model.MatriculaDisciplina;
import com.sap.model.OfertaDisciplina;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvaliacaoServiceTest {

    private AvaliacaoRepository avaliacaoRepository;
    private ConfiguracaoAcademicaRepository configuracaoRepository;
    private MatriculaDisciplinaRepository matriculaRepository;
    private AvaliacaoService service;

    @BeforeEach
    void configurar() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        configuracaoRepository = mock(ConfiguracaoAcademicaRepository.class);
        matriculaRepository = mock(MatriculaDisciplinaRepository.class);

        service = new AvaliacaoService(
                avaliacaoRepository,
                configuracaoRepository,
                matriculaRepository
        );
    }

    @Test
    void deveListarTodasAsAvaliacoes() {
        when(avaliacaoRepository.findAll())
                .thenReturn(List.of(new Avaliacao(), new Avaliacao()));

        assertEquals(2, service.listarTodas().size());

        verify(avaliacaoRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaSemConfiguracaoAcademica() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        List<Avaliacao> resultado = service.listarDoEstudante(usuario);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(matriculaRepository);
        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void deveRetornarListaVaziaSemMatriculas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        ConfiguracaoAcademica configuracao = new ConfiguracaoAcademica();
        configuracao.setId(10L);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of());

        assertTrue(service.listarDoEstudante(usuario).isEmpty());

        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void deveListarAvaliacoesDasDisciplinasMatriculadas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        ConfiguracaoAcademica configuracao = new ConfiguracaoAcademica();
        configuracao.setId(10L);

        Disciplina disciplina = new Disciplina();
        disciplina.setId(5L);

        OfertaDisciplina oferta = new OfertaDisciplina();
        oferta.setDisciplina(disciplina);

        MatriculaDisciplina matricula = new MatriculaDisciplina();
        matricula.setOfertaDisciplina(oferta);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of(matricula));

        when(avaliacaoRepository.findByAssuntoDisciplinaId(5L))
                .thenReturn(List.of(new Avaliacao(), new Avaliacao()));

        List<Avaliacao> resultado = service.listarDoEstudante(usuario);

        assertEquals(2, resultado.size());

        verify(avaliacaoRepository).findByAssuntoDisciplinaId(5L);
    }

    @Test
    void deveReunirAvaliacoesDeMultiplasDisciplinas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        ConfiguracaoAcademica configuracao = new ConfiguracaoAcademica();
        configuracao.setId(10L);

        MatriculaDisciplina primeira = criarMatricula(5L);
        MatriculaDisciplina segunda = criarMatricula(6L);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository.findByConfiguracaoAcademicaId(10L))
                .thenReturn(List.of(primeira, segunda));

        when(avaliacaoRepository.findByAssuntoDisciplinaId(5L))
                .thenReturn(List.of(new Avaliacao()));

        when(avaliacaoRepository.findByAssuntoDisciplinaId(6L))
                .thenReturn(List.of(new Avaliacao(), new Avaliacao()));

        List<Avaliacao> resultado = service.listarDoEstudante(usuario);

        assertEquals(3, resultado.size());

        verify(avaliacaoRepository).findByAssuntoDisciplinaId(5L);
        verify(avaliacaoRepository).findByAssuntoDisciplinaId(6L);
    }

    private MatriculaDisciplina criarMatricula(Long disciplinaId) {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(disciplinaId);

        OfertaDisciplina oferta = new OfertaDisciplina();
        oferta.setDisciplina(disciplina);

        MatriculaDisciplina matricula = new MatriculaDisciplina();
        matricula.setOfertaDisciplina(oferta);

        return matricula;
    }
}
