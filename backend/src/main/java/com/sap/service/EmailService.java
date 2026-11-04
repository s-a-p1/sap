
package com.sap.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String nomeRemetente;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String remetente,
            @Value("${app.mail.from-name}") String nomeRemetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.nomeRemetente = nomeRemetente;
    }

    public void enviarRecuperacaoSenha(String destinatario, String linkRecuperacao) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");

            helper.setFrom(remetente, nomeRemetente);
            helper.setTo(destinatario);
            helper.setSubject("SAP - Recuperação de senha");

            String conteudo = """
                    Olá!

                    Recebemos uma solicitação para redefinir a senha da sua conta no SAP.

                    Para criar uma nova senha, acesse o link abaixo:

                    %s

                    Este link é válido por 15 minutos e pode ser utilizado apenas uma vez.

                    Se você não solicitou a recuperação de senha, ignore este e-mail.

                    Atenciosamente,
                    Equipe SAP - Sistema de Aprendizagem
                    """.formatted(linkRecuperacao);

            helper.setText(conteudo, false);

            mailSender.send(mensagem);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Não foi possível preparar o e-mail de recuperação.", e);
        }
    }
}
