package com.wms.wms.controller;

import com.wms.wms.model.Movimentacao;
import com.wms.wms.model.Tarefa;
import com.wms.wms.service.TarefaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TarefaService tarefaService;

    public AdminController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    // ============================================================
    // PAINEL PRINCIPAL - Produtividade (tarefas em andamento)
    // Busca por: nome do usuário, ID da tarefa ou código de caixa
    // ============================================================
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String busca, Model model) {
        List<Tarefa> tarefas = tarefaService.listarTarefasAtivas();

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim().toLowerCase();

            // Busca por caixa (IDs de tarefas que contêm a caixa buscada)
            List<Long> idsPorCaixa = tarefaService.buscarIdsTarefasPorCaixa(termo);

            tarefas = tarefas.stream()
                    .filter(t -> (t.getUsuario() != null
                                    && t.getUsuario().getNome() != null
                                    && t.getUsuario().getNome().toLowerCase().contains(termo))
                            || String.valueOf(t.getId()).contains(termo)
                            || idsPorCaixa.contains(t.getId()))
                    .toList();
        }

        Map<Long, Movimentacao> ultimasMovs = new HashMap<>();
        List<String> locaisUltimaArmazenagem = new ArrayList<>();

        for (Tarefa t : tarefas) {
            Movimentacao mov = tarefaService.buscarUltimaMovimentacao(t.getId());
            ultimasMovs.put(t.getId(), mov);
            if (mov != null && mov.getPosicao() != null) {
                locaisUltimaArmazenagem.add(mov.getPosicao().getCodigo());
            } else {
                locaisUltimaArmazenagem.add("--");
            }
        }

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("locais", locaisUltimaArmazenagem);
        model.addAttribute("busca", busca);
        return "admin/dashboard";
    }

    // ============================================================
    // TELA DE TAREFAS - Lista TODAS com busca por usuário/ID/caixa
    // ============================================================
    @GetMapping("/tarefas")
    public String listarTarefas(@RequestParam(required = false) String busca, Model model) {
        List<Tarefa> tarefas = tarefaService.listarTodasTarefas();

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim().toLowerCase();

            List<Long> idsPorCaixa = tarefaService.buscarIdsTarefasPorCaixa(termo);

            tarefas = tarefas.stream()
                    .filter(t -> (t.getUsuario() != null
                                    && t.getUsuario().getNome() != null
                                    && t.getUsuario().getNome().toLowerCase().contains(termo))
                            || String.valueOf(t.getId()).contains(termo)
                            || idsPorCaixa.contains(t.getId()))
                    .toList();
        }

        Map<Long, Movimentacao> ultimasMovs = new HashMap<>();
        List<String> locaisUltimaArmazenagem = new ArrayList<>();

        for (Tarefa t : tarefas) {
            Movimentacao mov = tarefaService.buscarUltimaMovimentacao(t.getId());
            ultimasMovs.put(t.getId(), mov);
            if (mov != null && mov.getPosicao() != null) {
                locaisUltimaArmazenagem.add(mov.getPosicao().getCodigo());
            } else {
                locaisUltimaArmazenagem.add("--");
            }
        }

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("locais", locaisUltimaArmazenagem);
        model.addAttribute("busca", busca);
        return "admin/tarefas";
    }

    // ============================================================
    // VISUALIZAR TAREFA
    // ============================================================
    @GetMapping("/tarefa/{id}")
    public String visualizarTarefa(@PathVariable Long id, Model model) {
        Tarefa tarefa = tarefaService.buscarTarefa(id);
        List<Movimentacao> movimentacoes = tarefaService.listarMovimentacoes(id);

        model.addAttribute("tarefa", tarefa);
        model.addAttribute("movimentacoes", movimentacoes);
        return "admin/tarefa-detalhes";
    }

    // ============================================================
    // DESVINCULAR TAREFA
    // ============================================================
    @PostMapping("/desvincular")
    public String desvincular(@RequestParam Long tarefaId,
                              @RequestParam(required = false) String origem,
                              RedirectAttributes attributes) {
        try {
            tarefaService.desvincularTarefa(tarefaId);
            attributes.addFlashAttribute("sucesso", "Tarefa desvinculada com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }

        if ("tarefas".equals(origem)) {
            return "redirect:/admin/tarefas";
        }
        return "redirect:/admin/dashboard";
    }
}