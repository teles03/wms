package com.wms.wms.repository;

import com.wms.wms.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    // Busca a última movimentação de uma tarefa (para o painel do admin)
    Optional<Movimentacao> findFirstByTarefaIdOrderByDataHoraDesc(Long tarefaId);
    
    // Lista todas as movimentações de uma tarefa
    List<Movimentacao> findByTarefaIdOrderByDataHoraDesc(Long tarefaId);
}