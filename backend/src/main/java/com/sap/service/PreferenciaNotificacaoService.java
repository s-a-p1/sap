
package com.sap.service;

import com.sap.model.PreferenciaNotificacao;
import com.sap.model.Usuario;
import com.sap.repository.PreferenciaNotificacaoRepository;
import com.sap.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PreferenciaNotificacaoService {

    private final PreferenciaNotificacaoRepository preferenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public PreferenciaNotificacaoService(
            PreferenciaNotificacaoRepository preferenciaRepository,
            UsuarioRepository usuarioRepository) {
        this.preferenciaRepository = preferenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PreferenciaNotificacao buscarOuCriar(Long usuarioId) {
        Optional<PreferenciaNotificacao> existente =
                preferenciaRepository.findByUsuarioId(usuarioId);

        if (existente.isPresent()) {
            return existente.get();
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        PreferenciaNotificacao preferencia = new PreferenciaNotificacao();
        preferencia.setUsuario(usuario);

        return preferenciaRepository.save(preferencia);
    }

    @Transactional
    public PreferenciaNotificacao salvar(
            Long usuarioId,
            PreferenciaNotificacao dados) {

        PreferenciaNotificacao preferencia = buscarOuCriar(usuarioId);

        if (dados.getAntecedenciaLembretes() < 0
                || dados.getAntecedenciaLembretes() > 2) {
            throw new IllegalArgumentException(
                    "A antecedência deve ser de 0, 1 ou 2 dias."
            );
        }

        preferencia.setNotificacoesAtivas(dados.isNotificacoesAtivas());
        preferencia.setLembretesPlano(dados.isLembretesPlano());
        preferencia.setMetasPendentes(dados.isMetasPendentes());
        preferencia.setRecomendacoesRevisao(dados.isRecomendacoesRevisao());
        preferencia.setAvaliacoesProximas(dados.isAvaliacoesProximas());
        preferencia.setEvolucaoDesempenho(dados.isEvolucaoDesempenho());
        preferencia.setResumoSemanal(dados.isResumoSemanal());
        preferencia.setAntecedenciaLembretes(dados.getAntecedenciaLembretes());

        return preferenciaRepository.save(preferencia);
    }
}
