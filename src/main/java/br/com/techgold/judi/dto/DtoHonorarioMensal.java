package br.com.techgold.judi.dto;

import java.math.BigDecimal;

public record DtoHonorarioMensal(String mes, BigDecimal horas, BigDecimal total) {
}
