package com.wms.wms.repository;

import com.wms.wms.model.CaixaProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CaixaProdutoRepository extends JpaRepository<CaixaProduto, Long> {
    List<CaixaProduto> findByCaixaId(Long caixaId);
    Optional<CaixaProduto> findByCaixaIdAndProdutoId(Long caixaId, Long produtoId);
}