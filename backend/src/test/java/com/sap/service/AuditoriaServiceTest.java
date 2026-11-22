
package com.sap.service;

import com.sap.model.LogAuditoria;
import com.sap.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {

    private LogAuditoriaRepository repository;
    private AuditoriaService service;

    @BeforeEach
    void configurar() {
        repository = mock(LogAuditoriaRepository.class);
        service = new AuditoriaService(repository);
    }

    @Test
    void deveRegistrarAcaoDoUsuario() {
        service.registrar(
                1L,
                "usuario@teste.com",
                "LOGIN",
                "Usuário autenticado"
        );

        verify(repository).save(any(LogAuditoria.class));
    }

    @Test
    void deveRegistrarAcaoSemUsuarioIdentificado() {
        service.registrar(
                null,
                null,
                "ACESSO_NEGADO",
                "Tentativa de acesso"
        );

        verify(repository).save(any(LogAuditoria.class));
    }

    @Test
    void deveRegistrarUmaAcaoPorChamada() {
        service.registrar(1L, "a@teste.com", "LOGIN", "Entrada");
        service.registrar(1L, "a@teste.com", "LOGOUT", "Saída");

        verify(repository, times(2)).save(any(LogAuditoria.class));
    }

    @Test
    void deveCriarRegistroComData() {
        LocalDateTime antes = LocalDateTime.now();

        service.registrar(
                2L,
                "teste@teste.com",
                "ATUALIZACAO",
                "Perfil atualizado"
        );

        LocalDateTime depois = LocalDateTime.now();

        ArgumentCaptor<LogAuditoria> captor =
                ArgumentCaptor.forClass(LogAuditoria.class);

        verify(repository).save(captor.capture());

        LogAuditoria log = captor.getValue();

        assertNotNull(log);
        assertNotNull(log.getDataHora());
        assertFalse(log.getDataHora().isBefore(antes));
        assertFalse(log.getDataHora().isAfter(depois));
    }
}
