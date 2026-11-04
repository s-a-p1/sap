package com.sap.dto;

import java.util.List;

public record ConfiguracaoAcademicaRequest(
        Long cursoId,
        Integer semestre,
        List<Long> ofertasDisciplinasIds
) {
}