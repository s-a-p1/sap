
package com.sap.service;

import com.sap.model.Disciplina;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.DisciplinaRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final OfertaDisciplinaRepository ofertaDisciplinaRepository;

    public DisciplinaService(
            DisciplinaRepository disciplinaRepository,
            AssuntoRepository assuntoRepository,
            OfertaDisciplinaRepository ofertaDisciplinaRepository) {

        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.ofertaDisciplinaRepository = ofertaDisciplinaRepository;
    }

    public List<Disciplina> listarTodas() {
        return disciplinaRepository.findAll();
    }

    public Disciplina cadastrar(Disciplina disciplina) {
        if (disciplina.getNome() == null || disciplina.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da disciplina é obrigatório.");
        }

        disciplina.setId(null);
        disciplina.setNome(disciplina.getNome().trim());

        return disciplinaRepository.save(disciplina);
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Disciplina não encontrada."
                ));

        if (assuntoRepository.existsByDisciplina_Id(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir esta disciplina porque ela possui assuntos cadastrados."
            );
        }

        if (ofertaDisciplinaRepository.existsByDisciplina_Id(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir esta disciplina porque ela está vinculada a um curso."
            );
        }

        disciplinaRepository.delete(disciplina);
    }
}
