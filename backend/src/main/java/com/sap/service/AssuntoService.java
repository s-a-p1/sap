
package com.sap.service;

import com.sap.model.Assunto;
import com.sap.model.Disciplina;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.DisciplinaRepository;
import com.sap.repository.QuestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssuntoService {

    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;

    public AssuntoService(
            AssuntoRepository assuntoRepository,
            DisciplinaRepository disciplinaRepository,
            AvaliacaoRepository avaliacaoRepository,
            QuestaoRepository questaoRepository) {
        this.assuntoRepository = assuntoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoRepository = questaoRepository;
    }

    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }

    public List<Assunto> listarPorDisciplina(Long disciplinaId) {
        return assuntoRepository.listarAssuntosEspecificos(disciplinaId);
    }

    public Assunto cadastrar(Assunto assunto) {
        if (assunto.getNome() == null || assunto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do assunto é obrigatório.");
        }

        if (assunto.getDisciplina() == null ||
                assunto.getDisciplina().getId() == null) {
            throw new IllegalArgumentException("Selecione uma disciplina.");
        }

        Disciplina disciplina = disciplinaRepository.findById(
                assunto.getDisciplina().getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "A disciplina selecionada não foi encontrada."
        ));

        assunto.setId(null);
        assunto.setNome(assunto.getNome().trim());
        assunto.setDisciplina(disciplina);
        assunto.setVisivelCatalogo(true);

        if (assunto.getDescricao() != null) {
            assunto.setDescricao(assunto.getDescricao().trim());
        }

        return assuntoRepository.save(assunto);
    }

    @Transactional
    public void excluir(Long id) {
        Assunto assunto = assuntoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assunto não encontrado."
                ));

        if (avaliacaoRepository.existsByAssunto_Id(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir este assunto porque ele possui avaliações vinculadas."
            );
        }

        if (questaoRepository.existsByAssunto_Id(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir este assunto porque ele possui questões vinculadas."
            );
        }

        assuntoRepository.delete(assunto);
    }
}
