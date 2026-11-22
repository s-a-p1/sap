
package com.sap.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeradorTokenRecuperacaoTest {

    private GeradorTokenRecuperacao service;

    @BeforeEach
    void configurar() {
        service = new GeradorTokenRecuperacao();
    }

    @Test
    void deveGerarTokenNaoNulo() {
        String token = service.gerarToken();

        assertNotNull(token);
    }

    @Test
    void deveGerarTokenNaoVazio() {
        String token = service.gerarToken();

        assertFalse(token.isBlank());
    }

    @Test
    void deveGerarTokenComQuarentaETresCaracteres() {
        String token = service.gerarToken();

        assertEquals(43, token.length());
    }

    @Test
    void deveGerarTokenComTrintaEDoisBytes() {
        String token = service.gerarToken();

        byte[] bytes = Base64.getUrlDecoder().decode(token);

        assertEquals(32, bytes.length);
    }

    @Test
    void deveGerarTokenSemPreenchimentoBase64() {
        String token = service.gerarToken();

        assertFalse(token.contains("="));
    }

    @Test
    void deveGerarTokenCompativelComUrl() {
        String token = service.gerarToken();

        assertTrue(token.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void deveGerarTokensDiferentes() {
        String primeiro = service.gerarToken();
        String segundo = service.gerarToken();

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveGerarCemTokensSemRepeticao() {
        Set<String> tokens = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            tokens.add(service.gerarToken());
        }

        assertEquals(100, tokens.size());
    }

    @Test
    void deveGerarHashNaoNulo() {
        String hash = service.gerarHash("token-teste");

        assertNotNull(hash);
    }

    @Test
    void deveGerarHashComSessentaEQuatroCaracteres() {
        String hash = service.gerarHash("token-teste");

        assertEquals(64, hash.length());
    }

    @Test
    void deveGerarHashHexadecimal() {
        String hash = service.gerarHash("token-teste");

        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void deveGerarMesmoHashParaMesmoToken() {
        String primeiro = service.gerarHash("token123");
        String segundo = service.gerarHash("token123");

        assertEquals(primeiro, segundo);
    }

    @Test
    void deveGerarHashesDiferentesParaTokensDiferentes() {
        String primeiro = service.gerarHash("token123");
        String segundo = service.gerarHash("token456");

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveDiferenciarLetrasMaiusculasEMinusculas() {
        String primeiro = service.gerarHash("Token123");
        String segundo = service.gerarHash("token123");

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveDiferenciarTokensComEspacos() {
        String primeiro = service.gerarHash("token123");
        String segundo = service.gerarHash(" token123 ");

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveGerarHashParaStringVazia() {
        String hash = service.gerarHash("");

        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                hash
        );
    }

    @Test
    void deveGerarHashCompativelComSha256()
            throws NoSuchAlgorithmException {

        String token = "SAP-recuperacao-123";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        String esperado = HexFormat.of().formatHex(
                digest.digest(token.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(esperado, service.gerarHash(token));
    }

    @Test
    void deveGerarHashCorretoParaCaracteresUnicode()
            throws NoSuchAlgorithmException {

        String token = "recuperação-á-é-ç-🔐";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        String esperado = HexFormat.of().formatHex(
                digest.digest(token.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(esperado, service.gerarHash(token));
    }

    @Test
    void deveGerarHashDoTokenProduzido() {
        String token = service.gerarToken();

        String hash = service.gerarHash(token);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertNotEquals(token, hash);
    }
}
