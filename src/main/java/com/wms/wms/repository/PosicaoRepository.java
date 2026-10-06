package com.wms.wms.repository;

import com.wms.wms.model.Posicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PosicaoRepository extends JpaRepository<Posicao, Long> {
    Optional<Posicao> findByCodigo(String codigo);
}