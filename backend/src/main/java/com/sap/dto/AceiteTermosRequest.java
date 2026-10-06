package com.sap.dto;

import jakarta.validation.constraints.AssertTrue;

public class AceiteTermosRequest {

    @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade.")
    private boolean aceitouTermos;

    public AceiteTermosRequest() {
    }

    public boolean isAceitouTermos() {
        return aceitouTermos;
    }

    public void setAceitouTermos(boolean aceitouTermos) {
        this.aceitouTermos = aceitouTermos;
    }
}
