
package com.sap.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CsrfControllerTest {

    private CsrfController controller;
    private HttpServletRequest request;
    private CsrfToken csrfToken;

    @BeforeEach
    void configurar() {
        controller = new CsrfController();
        request = mock(HttpServletRequest.class);
        csrfToken = mock(CsrfToken.class);
    }

    @Test
    void deveRetornarTokenCsrfComSucesso() {
        prepararToken("token-123", "X-CSRF-TOKEN");

        Map<String, String> resposta = controller.csrf(request);

        assertNotNull(resposta);
        assertEquals("token-123", resposta.get("token"));
        assertEquals("X-CSRF-TOKEN", resposta.get("headerName"));
        assertEquals(2, resposta.size());
    }

    @Test
    void deveUtilizarAtributoCorretoDaRequisicao() {
        prepararToken("token-456", "X-XSRF-TOKEN");

        controller.csrf(request);

        verify(request).getAttribute(CsrfToken.class.getName());
    }

    @Test
    void deveRetornarCabecalhoPersonalizado() {
        prepararToken("abc123", "X-CUSTOM-CSRF");

        Map<String, String> resposta = controller.csrf(request);

        assertEquals("X-CUSTOM-CSRF", resposta.get("headerName"));
        assertEquals("abc123", resposta.get("token"));
    }

    @Test
    void deveLancarExcecaoQuandoTokenNaoExiste() {
        when(request.getAttribute(CsrfToken.class.getName()))
                .thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> controller.csrf(request)
        );
    }

    private void prepararToken(String token, String headerName) {
        when(request.getAttribute(CsrfToken.class.getName()))
                .thenReturn(csrfToken);

        when(csrfToken.getToken()).thenReturn(token);
        when(csrfToken.getHeaderName()).thenReturn(headerName);
    }
}
