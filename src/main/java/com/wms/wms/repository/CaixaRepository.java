package com.wms.wms.repository;

import com.wms.wms.model.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    Optional<Caixa> findByCodigoQr(String codigoQr);
}