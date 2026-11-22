
package com.sap.service;

import com.sap.model.PreferenciaNotificacao;
import com.sap.model.Usuario;
import com.sap.repository.PreferenciaNotificacaoRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PreferenciaNotificacaoServiceTest {

    private PreferenciaNotificacaoRepository preferenciaRepository;
    private UsuarioRepository usuarioRepository;
    private PreferenciaNotificacaoService service;

    @BeforeEach
    void configurar() {
        preferenciaRepository = mock(PreferenciaNotificacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        service = new PreferenciaNotificacaoService(
                preferenciaRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRetornarPreferenciaExistente() {
        PreferenciaNotificacao existente = new PreferenciaNotificacao();

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        PreferenciaNotificacao resultado = service.buscarOuCriar(1L);

        assertSame(existente, resultado);
        verify(preferenciaRepository, never()).save(any());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveCriarPreferenciaQuandoNaoExistir() {
        Usuario usuario = mock(Usuario.class);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.buscarOuCriar(1L);

        assertNotNull(resultado);
        assertSame(usuario, resultado.getUsuario());

        verify(preferenciaRepository).save(resultado);
    }

    @Test
    void deveRejeitarUsuarioInexistenteAoCriarPreferencia() {
        when(preferenciaRepository.findByUsuarioId(99L))
                .thenReturn(Optional.empty());

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarOuCriar(99L)
        );

        assertEquals("Usuário não encontrado.", erro.getMessage());
        verify(preferenciaRepository, never()).save(any());
    }

    @Test
    void deveSalvarTodasAsPreferencias() {
        PreferenciaNotificacao existente = new PreferenciaNotificacao();
        PreferenciaNotificacao dados = new PreferenciaNotificacao();

        dados.setNotificacoesAtivas(true);
        dados.setLembretesPlano(true);
        dados.setMetasPendentes(false);
        dados.setRecomendacoesRevisao(true);
        dados.setAvaliacoesProximas(false);
        dados.setEvolucaoDesempenho(true);
        dados.setResumoSemanal(false);
        dados.setAntecedenciaLembretes(2);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.salvar(1L, dados);

        assertTrue(resultado.isNotificacoesAtivas());
        assertTrue(resultado.isLembretesPlano());
        assertFalse(resultado.isMetasPendentes());
        assertTrue(resultado.isRecomendacoesRevisao());
        assertFalse(resultado.isAvaliacoesProximas());
        assertTrue(resultado.isEvolucaoDesempenho());
        assertFalse(resultado.isResumoSemanal());
        assertEquals(2, resultado.getAntecedenciaLembretes());
        assertSame(existente, resultado);
    }

    @Test
    void devePermitirDesativarTodasAsNotificacoes() {
        PreferenciaNotificacao existente = new PreferenciaNotificacao();
        PreferenciaNotificacao dados = new PreferenciaNotificacao();

        dados.setNotificacoesAtivas(false);
        dados.setLembretesPlano(false);
        dados.setMetasPendentes(false);
        dados.setRecomendacoesRevisao(false);
        dados.setAvaliacoesProximas(false);
        dados.setEvolucaoDesempenho(false);
        dados.setResumoSemanal(false);
        dados.setAntecedenciaLembretes(0);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.salvar(1L, dados);

        assertFalse(resultado.isNotificacoesAtivas());
        assertFalse(resultado.isLembretesPlano());
        assertFalse(resultado.isMetasPendentes());
        assertFalse(resultado.isRecomendacoesRevisao());
        assertFalse(resultado.isAvaliacoesProximas());
        assertFalse(resultado.isEvolucaoDesempenho());
        assertFalse(resultado.isResumoSemanal());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void deveAceitarAntecedenciaValida(int antecedencia) {
        PreferenciaNotificacao existente = new PreferenciaNotificacao();
        PreferenciaNotificacao dados = new PreferenciaNotificacao();
        dados.setAntecedenciaLembretes(antecedencia);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.salvar(1L, dados);

        assertEquals(antecedencia, resultado.getAntecedenciaLembretes());
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 3, 5, 100})
    void deveRejeitarAntecedenciaInvalida(int antecedencia) {
        PreferenciaNotificacao existente = new PreferenciaNotificacao();
        PreferenciaNotificacao dados = new PreferenciaNotificacao();
        dados.setAntecedenciaLembretes(antecedencia);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.salvar(1L, dados)
        );

        assertEquals(
                "A antecedência deve ser de 0, 1 ou 2 dias.",
                erro.getMessage()
        );

        verify(preferenciaRepository, never()).save(any());
    }

    @Test
    void devePreservarUsuarioAssociadoAoSalvar() {
        Usuario usuario = mock(Usuario.class);
        PreferenciaNotificacao existente = new PreferenciaNotificacao();
        existente.setUsuario(usuario);

        PreferenciaNotificacao dados = new PreferenciaNotificacao();
        dados.setAntecedenciaLembretes(1);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(existente));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.salvar(1L, dados);

        assertSame(usuario, resultado.getUsuario());
    }

    @Test
    void deveRejeitarSalvarPreferenciasDeUsuarioInexistente() {
        PreferenciaNotificacao dados = new PreferenciaNotificacao();

        when(preferenciaRepository.findByUsuarioId(99L))
                .thenReturn(Optional.empty());

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.salvar(99L, dados)
        );

        verify(preferenciaRepository, never()).save(any());
    }

    @Test
    void deveCriarPreferenciaAntesDeSalvarQuandoNecessario() {
        Usuario usuario = mock(Usuario.class);
        PreferenciaNotificacao dados = new PreferenciaNotificacao();
        dados.setAntecedenciaLembretes(1);

        when(preferenciaRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(preferenciaRepository.save(any(PreferenciaNotificacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PreferenciaNotificacao resultado = service.salvar(1L, dados);

        assertSame(usuario, resultado.getUsuario());
        assertEquals(1, resultado.getAntecedenciaLembretes());

        verify(preferenciaRepository, times(2))
                .save(any(PreferenciaNotificacao.class));
    }
}
