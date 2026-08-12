package br.com.techgold.judi.datajud;

import java.time.LocalDateTime;

/**
 * Representação simplificada de um movimento retornado pela API do DataJud,
 * já convertida a partir do JSON bruto da resposta (ver {@link DataJudClient}).
 */
public record MovimentoDataJud(String codigo, String descricao, LocalDateTime dataHora, String complemento) {
}
