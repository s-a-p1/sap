
package com.sap.service;

import com.sap.dto.CadastroUsuarioRequest;
import com.sap.validation.ValidadorCredenciais;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.AnaliseDesempenhoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.LogAuditoriaRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.MetaEstudoRepository;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.PreferenciaNotificacaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.repository.TokenRecuperacaoSenhaRepository;
import com.sap.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UsuarioService {

    private static final String VERSAO_ATUAL_TERMOS = "1.0";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;
    private final MatriculaDisciplinaRepository matriculaDisciplinaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final PreferenciaNotificacaoRepository preferenciaNotificacaoRepository;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final MetaEstudoRepository metaEstudoRepository;
    private final ResultadoRepository resultadoRepository;
    private final AnaliseDesempenhoRepository analiseDesempenhoRepository;
    private final DesempenhoAssuntoRepository desempenhoAssuntoRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ConfiguracaoAcademicaRepository configuracaoAcademicaRepository,
            MatriculaDisciplinaRepository matriculaDisciplinaRepository,
            NotificacaoRepository notificacaoRepository,
            PreferenciaNotificacaoRepository preferenciaNotificacaoRepository,
            TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            MetaEstudoRepository metaEstudoRepository,
            ResultadoRepository resultadoRepository,
            AnaliseDesempenhoRepository analiseDesempenhoRepository,
            DesempenhoAssuntoRepository desempenhoAssuntoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.configuracaoAcademicaRepository = configuracaoAcademicaRepository;
        this.matriculaDisciplinaRepository = matriculaDisciplinaRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.preferenciaNotificacaoRepository = preferenciaNotificacaoRepository;
        this.tokenRecuperacaoSenhaRepository = tokenRecuperacaoSenhaRepository;
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.metaEstudoRepository = metaEstudoRepository;
        this.resultadoRepository = resultadoRepository;
        this.analiseDesempenhoRepository = analiseDesempenhoRepository;
        this.desempenhoAssuntoRepository = desempenhoAssuntoRepository;
    }

    public Usuario cadastrar(CadastroUsuarioRequest request) {

        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        if (!request.isAceitouTermos()) {
            throw new IllegalArgumentException(
                    "É necessário aceitar os Termos de Uso e a Política de Privacidade."
            );
        }

        String senhaCriptografada = passwordEncoder.encode(request.getSenha());

        Usuario usuario = new Usuario();

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(senhaCriptografada);
        usuario.setTipo("ESTUDANTE");
        usuario.setProvedor(ProvedorAutenticacao.LOCAL);
        usuario.setProvedorId(null);
        usuario.setAtivo(true);
        usuario.setAceitouTermos(true);
        usuario.setDataAceiteTermos(LocalDateTime.now());
        usuario.setVersaoTermos(VERSAO_ATUAL_TERMOS);

        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarPerfil(Long usuarioId, String nome, String email) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe seu nome.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe seu e-mail.");
        }

        String nomeNormalizado = nome.trim();
        String emailNormalizado = email.trim().toLowerCase();

        if (nomeNormalizado.length() > 100) {
            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 100 caracteres."
            );
        }

        if (emailNormalizado.length() > 150) {
            throw new IllegalArgumentException(
                    "O e-mail deve ter no máximo 150 caracteres."
            );
        }

        if (!usuario.getEmail().equalsIgnoreCase(emailNormalizado)
                && !ValidadorCredenciais.emailValido(emailNormalizado)) {
            throw new IllegalArgumentException(
                    ValidadorCredenciais.MENSAGEM_EMAIL
            );
        }

        if (!usuario.getEmail().equals(emailNormalizado)
                && usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }

        usuario.setNome(nomeNormalizado);
        usuario.setEmail(emailNormalizado);

        return usuarioRepository.save(usuario);
    }

    public void alterarSenha(
            Long usuarioId,
            String senhaAtual,
            String novaSenha) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        if (senhaAtual == null || senhaAtual.isBlank()) {
            throw new IllegalArgumentException("Informe sua senha atual.");
        }

        if (novaSenha == null || novaSenha.length() < 8) {
            throw new IllegalArgumentException(
                    "A nova senha deve ter pelo menos 8 caracteres."
            );
        }

        if (!ValidadorCredenciais.senhaValida(novaSenha)) {
            throw new IllegalArgumentException(
                    ValidadorCredenciais.MENSAGEM_SENHA
            );
        }

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new IllegalArgumentException(
                    "A nova senha deve ser diferente da senha atual."
            );
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    public void validarSenhaExclusao(Long usuarioId, String senhaAtual) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        if (senhaAtual == null || senhaAtual.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe sua senha para confirmar a exclusão."
            );
        }

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta.");
        }
    }

    @Transactional
    public void excluirConta(Long usuarioId, String senhaAtual) {

        validarSenhaExclusao(usuarioId, senhaAtual);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        metaEstudoRepository.deleteAll(
                metaEstudoRepository.findByUsuarioIdOrderByDataPrevistaAsc(usuarioId)
        );

        metaEstudoRepository.flush();

        var resultados = resultadoRepository
                .findByUsuarioIdOrderByDataRealizacaoDesc(usuarioId);

        for (var resultado : resultados) {

            analiseDesempenhoRepository.findByResultado_Id(resultado.getId())
                    .ifPresent(analiseDesempenhoRepository::delete);

            desempenhoAssuntoRepository.deleteAll(
                    desempenhoAssuntoRepository.findByResultado_Id(resultado.getId())
            );
        }

        analiseDesempenhoRepository.flush();
        desempenhoAssuntoRepository.flush();

        resultadoRepository.deleteAll(resultados);
        resultadoRepository.flush();

        configuracaoAcademicaRepository.findByUsuarioId(usuarioId)
                .ifPresent(configuracao -> {

                    matriculaDisciplinaRepository.deleteAll(
                            matriculaDisciplinaRepository
                                    .findByConfiguracaoAcademicaId(configuracao.getId())
                    );

                    matriculaDisciplinaRepository.flush();

                    configuracaoAcademicaRepository.delete(configuracao);
                    configuracaoAcademicaRepository.flush();
                });

        notificacaoRepository.deleteAll(
                notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
        );

        preferenciaNotificacaoRepository.findByUsuarioId(usuarioId)
                .ifPresent(preferenciaNotificacaoRepository::delete);

        tokenRecuperacaoSenhaRepository.deleteAll(
                tokenRecuperacaoSenhaRepository.findByUsuarioId(usuarioId)
        );

        logAuditoriaRepository.deleteAll(
                logAuditoriaRepository.findByUsuarioId(usuarioId)
        );

        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }
}
