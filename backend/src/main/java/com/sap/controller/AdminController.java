package com.sap.controller;

import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import com.sap.dto.CadastroProfessorRequest;
import com.sap.model.ProvedorAutenticacao;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
private final PasswordEncoder passwordEncoder;

public AdminController(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder) {

    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
}
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Long>> obterResumo() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        long totalAlunos = usuarios.stream()
                .filter(usuario -> "ESTUDANTE".equals(usuario.getTipo()))
                .count();

        long totalProfessores = usuarios.stream()
                .filter(usuario -> "PROFESSOR".equals(usuario.getTipo()))
                .count();

        return ResponseEntity.ok(Map.of(
                "totalAlunos", totalAlunos,
                "totalProfessores", totalProfessores
        ));
    }

    @GetMapping("/usuarios")
public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {

    List<Map<String, Object>> usuarios = usuarioRepository.findAll()
            .stream()
            .map(usuario -> {
                Map<String, Object> dados = new java.util.LinkedHashMap<>();

                dados.put("id", usuario.getId());
                dados.put("nome", usuario.getNome());
                dados.put("email", usuario.getEmail());
                dados.put("tipo", usuario.getTipo());
                dados.put("ativo", usuario.isAtivo());

                return dados;
            })
            .toList();

    return ResponseEntity.ok(usuarios);
}
@PatchMapping("/usuarios/{id}/acesso")
public ResponseEntity<?> alterarAcesso(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> request) {

    Usuario usuario = usuarioRepository.findById(id).orElse(null);

    if (usuario == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado."));
    }

    if ("ADMIN".equals(usuario.getTipo())) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Não é permitido alterar o acesso de administradores."));
    }

    Boolean ativo = request.get("ativo");

    if (ativo == null) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Informe o status de acesso."));
    }

    usuario.setAtivo(ativo);
    usuarioRepository.save(usuario);

    return ResponseEntity.ok(Map.of(
            "id", usuario.getId(),
            "nome", usuario.getNome(),
            "ativo", usuario.isAtivo()
    ));
}
@PostMapping("/professores")
public ResponseEntity<?> cadastrarProfessor(
        @Valid @RequestBody CadastroProfessorRequest request) {

    String emailNormalizado = request.getEmail().trim().toLowerCase();

    if (usuarioRepository.existsByEmail(emailNormalizado)) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Já existe um usuário cadastrado com este e-mail."));
    }

    Usuario professor = new Usuario();

    professor.setNome(request.getNome().trim());
    professor.setEmail(emailNormalizado);
    professor.setSenha(passwordEncoder.encode(request.getSenha()));
    professor.setTipo("PROFESSOR");
    professor.setProvedor(ProvedorAutenticacao.LOCAL);
    professor.setProvedorId(null);
    professor.setAtivo(true);
    professor.setAceitouTermos(false);
    professor.setDataAceiteTermos(null);
    professor.setVersaoTermos(null);

    Usuario professorSalvo = usuarioRepository.save(professor);

    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", professorSalvo.getId(),
            "nome", professorSalvo.getNome(),
            "email", professorSalvo.getEmail(),
            "tipo", professorSalvo.getTipo(),
            "ativo", professorSalvo.isAtivo()
    ));
}
}