package br.com.techgold.judi.dto;

import java.math.BigDecimal;

/** {@code personalizado} indica se {@code valor} veio da configuração do cliente ou do padrão do funcionário. */
public record DtoValorHoraEfetivo(BigDecimal valor, boolean personalizado) {
}
