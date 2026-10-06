package com.wms.wms.repository;

import com.wms.wms.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByStatus(Tarefa.StatusTarefa status);
    List<Tarefa> findAllByOrderByInicioDesc();

    // Busca tarefas que contenham uma caixa com o código informado
    @Query("SELECT DISTINCT t FROM Tarefa t " +
           "LEFT JOIN t.caixasDaTarefa tc " +
           "LEFT JOIN tc.caixa c " +
           "WHERE LOWER(c.codigoQr) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Tarefa> findByCaixaCodigoContaining(@Param("termo") String termo);
}