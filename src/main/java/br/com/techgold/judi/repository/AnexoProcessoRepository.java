package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techgold.judi.model.AnexoProcesso;

public interface AnexoProcessoRepository extends JpaRepository<AnexoProcesso, Long> {

	public List<AnexoProcesso> findByProcessoIdOrderByDataUploadDesc(Long processoId);

}
