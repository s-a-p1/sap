package com.sap.validation;

public final class ValidadorCredenciais {

    public static final String EMAIL_REGEX =
            "^[^\\s@]+@(?:[a-zA-Z0-9-]+[.])+[a-zA-Z]{2,}$";

    public static final String SENHA_REGEX =
            "(?s)^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*\\p{Punct}).*$";

    public static final String MENSAGEM_EMAIL =
            "Informe um e-mail válido.";

    public static final String MENSAGEM_SENHA =
            "A senha deve conter uma letra maiúscula, uma letra minúscula, um número e um caractere especial.";

    private ValidadorCredenciais() {
    }

    public static boolean emailValido(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static boolean senhaValida(String senha) {
        return senha != null && senha.matches(SENHA_REGEX);
    }
}
