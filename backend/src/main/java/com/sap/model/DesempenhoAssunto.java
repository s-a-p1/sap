
package com.sap.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "desempenhos_assunto")
public class DesempenhoAssunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resultado_id", nullable = false)
    private Resultado resultado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Column(name = "total_questoes", nullable = false)
    private Integer totalQuestoes;

    @Column(nullable = false)
    private Integer acertos;

    @Column(nullable = false)
    private Integer erros;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;

    public DesempenhoAssunto() {
    }

    public DesempenhoAssunto(
            Resultado resultado,
            Assunto assunto,
            Integer totalQuestoes,
            Integer acertos,
            Integer erros,
            BigDecimal percentual) {
        this.resultado = resultado;
        this.assunto = assunto;
        this.totalQuestoes = totalQuestoes;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
    }

    public Long getId() {
        return id;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public Assunto getAssunto() {
        return assunto;
    }

    public Integer getTotalQuestoes() {
        return totalQuestoes;
    }

    public Integer getAcertos() {
        return acertos;
    }

    public Integer getErros() {
        return erros;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }
}
