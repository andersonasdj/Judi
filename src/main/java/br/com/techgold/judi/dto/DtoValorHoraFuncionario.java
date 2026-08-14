package br.com.techgold.judi.dto;

import java.math.BigDecimal;

/**
 * Uma linha da tabela de valor/hora de um cliente: o valor padrão do
 * funcionário, o valor personalizado para este cliente (se houver) e o valor
 * efetivamente usado no cálculo dos honorários (personalizado, com fallback
 * para o padrão).
 */
public record DtoValorHoraFuncionario(
		Long funcionarioId,
		String nomeFuncionario,
		BigDecimal valorHoraPadrao,
		BigDecimal valorHoraPersonalizado,
		BigDecimal valorHoraEfetivo) {
}
