package br.com.techgold.judi.dto;

import java.math.BigDecimal;

/** {@code valorHora} nulo remove o valor personalizado, voltando a usar o padrão do funcionário. */
public record DtoSalvarValorHora(BigDecimal valorHora) {
}
