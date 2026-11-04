
package com.sap.service;

import com.sap.model.MetaEstudo;
import com.sap.model.Notificacao;
import com.sap.model.PreferenciaNotificacao;
import com.sap.model.Usuario;
import com.sap.repository.MetaEstudoRepository;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sap.model.Resultado;
import com.sap.repository.ResultadoRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GeradorNotificacoesService {

    private final MetaEstudoRepository metaEstudoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final PreferenciaNotificacaoService preferenciaService;
    private final UsuarioRepository usuarioRepository;
    private final ResultadoRepository resultadoRepository;



        public GeradorNotificacoesService(
                MetaEstudoRepository metaEstudoRepository,
                NotificacaoRepository notificacaoRepository,
                PreferenciaNotificacaoService preferenciaService,
                UsuarioRepository usuarioRepository,
                ResultadoRepository resultadoRepository) {
        this.metaEstudoRepository = metaEstudoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.preferenciaService = preferenciaService;
        this.usuarioRepository = usuarioRepository;
        this.resultadoRepository = resultadoRepository;
        }


    @Transactional
    public void gerarLembretesMetas(Long usuarioId) {
        PreferenciaNotificacao preferencias = preferenciaService.buscarOuCriar(usuarioId);

        if (!preferencias.isNotificacoesAtivas()) {
            return;
        }

        if (!preferencias.isLembretesPlano() && !preferencias.isMetasPendentes()) {
            return;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        List<MetaEstudo> metas = metaEstudoRepository
                .findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(usuarioId);

        LocalDate hoje = LocalDate.now();

        for (MetaEstudo meta : metas) {
            long diasRestantes = ChronoUnit.DAYS.between(hoje, meta.getDataPrevista());

            if (preferencias.isLembretesPlano()) {

                boolean lembreteAntecipado =
                        preferencias.getAntecedenciaLembretes() > 0
                        && diasRestantes > 0
                        && diasRestantes <= preferencias.getAntecedenciaLembretes();

                boolean lembreteNoDia = diasRestantes == 0;


                if (lembreteAntecipado || lembreteNoDia) {
                    String referenciaTipo = lembreteAntecipado
                            ? "META_LEMBRETE_ANTECIPADO_" + meta.getDataPrevista()
                            : "META_LEMBRETE_DIA_" + meta.getDataPrevista();

                    String titulo = lembreteAntecipado
                            ? "Meta de estudo se aproximando"
                            : "Meta de estudo para hoje";

                    String mensagem = lembreteAntecipado
                            ? "A meta \"" + meta.getTitulo() + "\" vence em "
                                    + diasRestantes + " dia(s)."
                            : "Hoje é o prazo da meta \"" + meta.getTitulo() + "\".";

                    criarSeNaoExistir(
                            usuario,
                            titulo,
                            mensagem,
                            "PLANO_ESTUDOS",
                            referenciaTipo,
                            meta.getId()
                    );
                }
            }

            if (preferencias.isMetasPendentes() && diasRestantes < 0) {
                criarSeNaoExistir(
                        usuario,
                        "Meta de estudo atrasada",
                        "A meta \"" + meta.getTitulo()
                                + "\" passou do prazo em " + meta.getDataPrevista()
                                + " e ainda não foi concluída.",
                        "META_PENDENTE",
                        "META_ATRASADA_" + meta.getDataPrevista(),
                        meta.getId()
                );
            }
        }
    }



@Transactional
public void gerarRecomendacoesRevisao(Long usuarioId) {
    PreferenciaNotificacao preferencias =
            preferenciaService.buscarOuCriar(usuarioId);

    if (!preferencias.isNotificacoesAtivas()
            || !preferencias.isRecomendacoesRevisao()) {
        return;
    }

    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Usuário não encontrado.")
            );

    List<Resultado> resultados =
            resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(usuarioId);

    Map<Long, Resultado> ultimosResultados = new HashMap<>();

    for (Resultado resultado : resultados) {
        if (resultado.getAvaliacao() == null
                || resultado.getAvaliacao().getAssunto() == null
                || resultado.getAvaliacao().getAssunto().getDisciplina() == null) {
            continue;
        }

        Long disciplinaId = resultado.getAvaliacao()
                .getAssunto()
                .getDisciplina()
                .getId();

        ultimosResultados.putIfAbsent(disciplinaId, resultado);
    }

    Set<Long> resultadosValidos = new HashSet<>();

    for (Resultado resultado : ultimosResultados.values()) {
        if (resultado.getPercentual() != null
                && resultado.getPercentual().doubleValue() < 70) {
            resultadosValidos.add(resultado.getId());
        }
    }

    List<Notificacao> notificacoesAntigas =
            notificacaoRepository.findByUsuarioIdAndCategoriaAndReferenciaTipo(
                    usuarioId,
                    "RECOMENDACAO_REVISAO",
                    "RESULTADO_REVISAO"
            );

    for (Notificacao notificacao : notificacoesAntigas) {
        if (!resultadosValidos.contains(notificacao.getReferenciaId())) {
            notificacaoRepository.delete(notificacao);
        }
    }

    for (Resultado resultado : ultimosResultados.values()) {
        if (resultado.getPercentual() == null) {
            continue;
        }

        double percentual = resultado.getPercentual().doubleValue();

        if (percentual >= 70) {
            continue;
        }

        String assunto = resultado.getAvaliacao()
                .getAssunto()
                .getNome();

        String titulo;
        String mensagem;

        if (percentual < 50) {
            titulo = "Revisão de alta prioridade";
            mensagem = "Seu desempenho mais recente na disciplina, "
                    + "na avaliação sobre " + assunto
                    + ", foi de " + resultado.getPercentual()
                    + "%. Revise os conceitos fundamentais antes de realizar uma nova avaliação.";
        } else {
            titulo = "Revisão recomendada";
            mensagem = "Seu desempenho mais recente na disciplina, "
                    + "na avaliação sobre " + assunto
                    + ", foi de " + resultado.getPercentual()
                    + "%. Revise os pontos de dificuldade e continue praticando.";
        }

        criarSeNaoExistir(
                usuario,
                titulo,
                mensagem,
                "RECOMENDACAO_REVISAO",
                "RESULTADO_REVISAO",
                resultado.getId()
        );
    }
}


    private void criarSeNaoExistir(
            Usuario usuario,
            String titulo,
            String mensagem,
            String categoria,
            String referenciaTipo,
            Long referenciaId) {

        boolean existe = notificacaoRepository
                .existsByUsuarioIdAndReferenciaTipoAndReferenciaId(
                        usuario.getId(),
                        referenciaTipo,
                        referenciaId
                );

        if (existe) {
            return;
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setCategoria(categoria);
        notificacao.setReferenciaTipo(referenciaTipo);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setLida(false);

        notificacaoRepository.save(notificacao);
    }
}
