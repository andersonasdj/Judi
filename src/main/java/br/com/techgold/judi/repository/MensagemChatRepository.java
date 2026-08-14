package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.techgold.judi.model.MensagemChat;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

	@Query("SELECT m FROM MensagemChat m "
			+ "WHERE (m.remetente.id = :usuarioId AND m.destinatario.id = :outroId) "
			+ "OR (m.remetente.id = :outroId AND m.destinatario.id = :usuarioId) "
			+ "ORDER BY m.dataEnvio DESC")
	Page<MensagemChat> buscarConversa(@Param("usuarioId") Long usuarioId, @Param("outroId") Long outroId, Pageable page);

	List<MensagemChat> findByRemetenteIdAndDestinatarioIdAndLidaFalse(Long remetenteId, Long destinatarioId);

	long countByDestinatarioIdAndLidaFalse(Long destinatarioId);

	@Query("SELECT m.remetente.id, COUNT(m) FROM MensagemChat m "
			+ "WHERE m.destinatario.id = :usuarioId AND m.lida = false "
			+ "GROUP BY m.remetente.id")
	List<Object[]> contarNaoLidasPorRemetente(@Param("usuarioId") Long usuarioId);

}
