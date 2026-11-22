
package com.sap.service;

import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UsuarioRepository usuarioRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        service = new CustomUserDetailsService(usuarioRepository);
    }

    @Test
    void deveCarregarEstudanteAtivo() {
        Usuario usuario = usuario("estudante@teste.com", "ESTUDANTE", true);

        when(usuarioRepository.findByEmail("estudante@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("estudante@teste.com");

        assertEquals("estudante@teste.com", detalhes.getUsername());
        assertEquals("senha123", detalhes.getPassword());
        assertTrue(detalhes.isEnabled());
        assertTrue(detalhes.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDANTE")));
    }

    @Test
    void deveCarregarProfessorAtivo() {
        Usuario usuario = usuario("professor@teste.com", "PROFESSOR", true);

        when(usuarioRepository.findByEmail("professor@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("professor@teste.com");

        assertTrue(detalhes.isEnabled());
        assertTrue(detalhes.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR")));
    }

    @Test
    void deveCarregarAdministradorAtivo() {
        Usuario usuario = usuario("admin@teste.com", "ADMIN", true);

        when(usuarioRepository.findByEmail("admin@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("admin@teste.com");

        assertTrue(detalhes.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void deveNormalizarEmailComMaiusculas() {
        Usuario usuario = usuario("teste@teste.com", "ESTUDANTE", true);

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("TESTE@TESTE.COM");

        assertEquals("teste@teste.com", detalhes.getUsername());

        verify(usuarioRepository).findByEmail("teste@teste.com");
    }

    @Test
    void deveRemoverEspacosDoEmail() {
        Usuario usuario = usuario("teste@teste.com", "ESTUDANTE", true);

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("  teste@teste.com  ");

        assertEquals("teste@teste.com", detalhes.getUsername());

        verify(usuarioRepository).findByEmail("teste@teste.com");
    }

    @Test
    void deveNormalizarEmailComEspacosEMaiusculas() {
        Usuario usuario = usuario("teste@teste.com", "ESTUDANTE", true);

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        service.loadUserByUsername("  TESTE@TESTE.COM  ");

        verify(usuarioRepository).findByEmail("teste@teste.com");
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        when(usuarioRepository.findByEmail("inexistente@teste.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("inexistente@teste.com")
        );
    }

    @Test
    void deveDesabilitarUsuarioInativo() {
        Usuario usuario = usuario("inativo@teste.com", "ESTUDANTE", false);

        when(usuarioRepository.findByEmail("inativo@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("inativo@teste.com");

        assertFalse(detalhes.isEnabled());
    }

    @Test
    void devePreservarSenhaArmazenada() {
        Usuario usuario = usuario("teste@teste.com", "ESTUDANTE", true);
        usuario.setSenha("$2a$10$hashExemplo");

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails detalhes =
                service.loadUserByUsername("teste@teste.com");

        assertEquals("$2a$10$hashExemplo", detalhes.getPassword());
    }

    @Test
    void deveConsultarRepositorioUmaVez() {
        Usuario usuario = usuario("teste@teste.com", "ESTUDANTE", true);

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        service.loadUserByUsername("teste@teste.com");

        verify(usuarioRepository, times(1))
                .findByEmail("teste@teste.com");
    }

    private Usuario usuario(String email, String tipo, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);
        usuario.setSenha("senha123");
        usuario.setTipo(tipo);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
