
package com.sap.dto;

import com.sap.model.PreferenciaNotificacao;

public record PreferenciaNotificacaoDTO(
        boolean notificacoesAtivas,
        boolean lembretesPlano,
        boolean metasPendentes,
        boolean recomendacoesRevisao,
        boolean avaliacoesProximas,
        boolean evolucaoDesempenho,
        boolean resumoSemanal,
        int antecedenciaLembretes
) {

    public static PreferenciaNotificacaoDTO fromEntity(
            PreferenciaNotificacao preferencia) {

        return new PreferenciaNotificacaoDTO(
                preferencia.isNotificacoesAtivas(),
                preferencia.isLembretesPlano(),
                preferencia.isMetasPendentes(),
                preferencia.isRecomendacoesRevisao(),
                preferencia.isAvaliacoesProximas(),
                preferencia.isEvolucaoDesempenho(),
                preferencia.isResumoSemanal(),
                preferencia.getAntecedenciaLembretes()
        );
    }

    public PreferenciaNotificacao toEntity() {
        PreferenciaNotificacao preferencia = new PreferenciaNotificacao();

        preferencia.setNotificacoesAtivas(notificacoesAtivas);
        preferencia.setLembretesPlano(lembretesPlano);
        preferencia.setMetasPendentes(metasPendentes);
        preferencia.setRecomendacoesRevisao(recomendacoesRevisao);
        preferencia.setAvaliacoesProximas(avaliacoesProximas);
        preferencia.setEvolucaoDesempenho(evolucaoDesempenho);
        preferencia.setResumoSemanal(resumoSemanal);
        preferencia.setAntecedenciaLembretes(antecedenciaLembretes);

        return preferencia;
    }
}
