
package com.sap.controller;

import com.sap.model.Curso;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.CursoRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import com.sap.repository.SemestreCursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CursoControllerTest {

    private CursoRepository cursoRepository;
    private SemestreCursoRepository semestreCursoRepository;
    private OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private CursoController controller;

    @BeforeEach
    void configurar() {
        cursoRepository = mock(CursoRepository.class);
        semestreCursoRepository = mock(SemestreCursoRepository.class);
        ofertaDisciplinaRepository = mock(OfertaDisciplinaRepository.class);
        configuracaoAcademicaRepository =
                mock(ConfiguracaoAcademicaRepository.class);

        controller = new CursoController(
                cursoRepository,
                semestreCursoRepository,
                ofertaDisciplinaRepository,
                configuracaoAcademicaRepository
        );
    }

    @Test
    void deveListarCursos() {
        Curso primeiro = curso(1L, "Sistemas de Informação", "Tecnologia");
        Curso segundo = curso(2L, "Administração", "Gestão");

        when(cursoRepository.findAll())
                .thenReturn(List.of(primeiro, segundo));

        List<Map<String, Object>> resposta = controller.listar();

        assertEquals(2, resposta.size());
        assertEquals(1L, resposta.get(0).get("id"));
        assertEquals("Sistemas de Informação", resposta.get(0).get("nome"));
        assertEquals(2L, resposta.get(1).get("id"));
        assertEquals("Administração", resposta.get(1).get("nome"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemCursos() {
        when(cursoRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> resposta = controller.listar();

        assertTrue(resposta.isEmpty());
    }

    @Test
    void deveRejeitarCadastroSemNome() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("descricao", "Descrição"));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o nome do curso.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarCadastroComNomeVazio() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("nome", ""));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o nome do curso.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarCadastroComNomeApenasEspacos() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("nome", "    "));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o nome do curso.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarNomeAcimaDoLimite() {
        ResponseEntity<?> resposta =
                controller.cadastrar(Map.of("nome", "A".repeat(151)));

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Nome ou descrição excede o limite permitido.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarDescricaoAcimaDoLimite() {
        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of(
                        "nome", "Sistemas de Informação",
                        "descricao", "A".repeat(256)
                )
        );

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Nome ou descrição excede o limite permitido.");

        verifyNoInteractions(cursoRepository);
    }

    @Test
    void deveRejeitarCursoDuplicado() {
        Curso existente = curso(
                1L, "Sistemas de Informação", "Tecnologia"
        );

        when(cursoRepository.findAll())
                .thenReturn(List.of(existente));

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of("nome", "Sistemas de Informação")
        );

        assertErro(resposta, HttpStatus.CONFLICT,
                "Já existe um curso cadastrado com este nome.");

        verify(cursoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarCursoDuplicadoIgnorandoMaiusculas() {
        Curso existente = curso(
                1L, "SISTEMAS DE INFORMAÇÃO", "Tecnologia"
        );

        when(cursoRepository.findAll())
                .thenReturn(List.of(existente));

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of("nome", "sistemas de informação")
        );

        assertErro(resposta, HttpStatus.CONFLICT,
                "Já existe um curso cadastrado com este nome.");
    }

    @Test
    void deveCadastrarCursoComSucesso() {
        configurarSalvamento();

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of(
                        "nome", "Sistemas de Informação",
                        "descricao", "Tecnologia"
                )
        );

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(10L, dados.get("id"));
        assertEquals("Sistemas de Informação", dados.get("nome"));
        assertEquals("Tecnologia", dados.get("descricao"));

        verify(cursoRepository).saveAndFlush(any(Curso.class));
    }

    @Test
    void deveCadastrarCursoSemDescricao() {
        configurarSalvamento();

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of("nome", "Administração")
        );

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals("", dados.get("descricao"));
    }

    @Test
    void deveRemoverEspacosDoNomeEDescricao() {
        configurarSalvamento();

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of(
                        "nome", "  Administração  ",
                        "descricao", "  Gestão  "
                )
        );

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals("Administração", dados.get("nome"));
        assertEquals("Gestão", dados.get("descricao"));
    }

    @Test
    void deveAceitarNomeEDescricaoNosLimites() {
        configurarSalvamento();

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of(
                        "nome", "A".repeat(150),
                        "descricao", "B".repeat(255)
                )
        );

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(150, ((String) dados.get("nome")).length());
        assertEquals(255, ((String) dados.get("descricao")).length());
    }

    @Test
    void deveTratarErroDeIntegridadeNoCadastro() {
        when(cursoRepository.saveAndFlush(any(Curso.class)))
                .thenThrow(new DataIntegrityViolationException("Erro"));

        ResponseEntity<?> resposta = controller.cadastrar(
                Map.of("nome", "Sistemas de Informação")
        );

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não foi possível cadastrar o curso. Verifique se o nome já existe.");
    }

    @Test
    void deveRetornarNaoEncontradoAoExcluirCursoInexistente() {
        ResponseEntity<?> resposta = controller.excluir(99L);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Curso não encontrado.");
    }

    @Test
    void deveImpedirExclusaoComSemestres() {
        prepararCursoExistente();

        when(semestreCursoRepository.existsByCurso_Id(1L))
                .thenReturn(true);

        ResponseEntity<?> resposta = controller.excluir(1L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este curso porque ele possui semestres cadastrados.");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComDisciplinasOfertadas() {
        prepararCursoExistente();

        when(ofertaDisciplinaRepository.existsByCurso_Id(1L))
                .thenReturn(true);

        ResponseEntity<?> resposta = controller.excluir(1L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este curso porque ele possui disciplinas ofertadas.");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void deveImpedirExclusaoComEstudantesVinculados() {
        prepararCursoExistente();

        when(configuracaoAcademicaRepository.existsByCurso_Id(1L))
                .thenReturn(true);

        ResponseEntity<?> resposta = controller.excluir(1L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este curso porque existem estudantes vinculados.");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void deveExcluirCursoComSucesso() {
        Curso existente = prepararCursoExistente();

        ResponseEntity<?> resposta = controller.excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertNull(resposta.getBody());

        verify(cursoRepository).delete(existente);
        verify(cursoRepository).flush();
    }

    @Test
    void deveTratarErroDeIntegridadeNaExclusao() {
        prepararCursoExistente();

        doThrow(new DataIntegrityViolationException("Erro"))
                .when(cursoRepository).flush();

        ResponseEntity<?> resposta = controller.excluir(1L);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir este curso porque existem registros vinculados.");
    }

    private Curso curso(Long id, String nome, String descricao) {
        Curso curso = mock(Curso.class);

        when(curso.getId()).thenReturn(id);
        when(curso.getNome()).thenReturn(nome);
        when(curso.getDescricao()).thenReturn(descricao);

        return curso;
    }

    private Curso prepararCursoExistente() {
        Curso existente = mock(Curso.class);

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        return existente;
    }

    private void configurarSalvamento() {
        when(cursoRepository.saveAndFlush(any(Curso.class)))
                .thenAnswer(invocacao -> {
                    Curso curso = invocacao.getArgument(0);
                    Curso salvo = mock(Curso.class);

                    when(salvo.getId()).thenReturn(10L);
                    when(salvo.getNome()).thenReturn(curso.getNome());
                    when(salvo.getDescricao()).thenReturn(curso.getDescricao());

                    return salvo;
                });
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
