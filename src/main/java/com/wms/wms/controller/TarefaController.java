package com.wms.wms.controller;

import com.wms.wms.model.*;
import com.wms.wms.repository.UsuarioRepository;
import com.wms.wms.service.TarefaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tarefa")
public class TarefaController {

    private final TarefaService tarefaService;
    private final UsuarioRepository usuarioRepository;

    public TarefaController(TarefaService tarefaService, UsuarioRepository usuarioRepository) {
        this.tarefaService = tarefaService;
        this.usuarioRepository = usuarioRepository;
    }

    // ============================================================
    // TELA 1 - Nova tarefa / Bipar caixas
    // ============================================================
    @GetMapping("/nova")
    public String novaTarefa(Model model, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Tarefa tarefa = tarefaService.criarTarefa(usuario);
        model.addAttribute("tarefaId", tarefa.getId());
        model.addAttribute("codigosBipados", new ArrayList<String>());
        return "tarefa/bipar-caixas";
    }

    @PostMapping("/bipar-caixa")
    public String biparCaixa(@RequestParam Long tarefaId,
                             @RequestParam String codigoQr,
                             @RequestParam(required = false) List<String> codigosBipados,
                             Model model,
                             RedirectAttributes attributes) {
        if (codigosBipados == null) {
            codigosBipados = new ArrayList<>();
        }

        if (codigosBipados.size() >= 20) {
            attributes.addFlashAttribute("erro", "Limite de 20 caixas atingido!");
            return "redirect:/tarefa/nova";
        }

        codigosBipados.add(codigoQr);
        model.addAttribute("tarefaId", tarefaId);
        model.addAttribute("codigosBipados", codigosBipados);
        return "tarefa/bipar-caixas";
    }

    @PostMapping("/concluir-caixas")
    public String concluirCaixas(@RequestParam Long tarefaId,
                                 @RequestParam List<String> codigosBipados,
                                 RedirectAttributes attributes) {
        Tarefa tarefa = tarefaService.buscarTarefa(tarefaId);

        int ordem = 1;
        for (String codigo : codigosBipados) {
            try {
                tarefaService.adicionarCaixaNaTarefa(tarefa, codigo, ordem);
                ordem++;
            } catch (Exception e) {
                attributes.addFlashAttribute("erro", "Erro ao adicionar caixa: " + codigo);
            }
        }

        return "redirect:/tarefa/lista?tarefaId=" + tarefaId;
    }

    // ============================================================
    // TELA 2 - Lista de caixas da tarefa
    // ============================================================
    @GetMapping("/lista")
    public String listaCaixas(@RequestParam Long tarefaId, Model model) {
        Tarefa tarefa = tarefaService.buscarTarefa(tarefaId);
        model.addAttribute("tarefa", tarefa);
        return "tarefa/lista-caixas";
    }

    // ============================================================
    // TELA 3 - Abrir caixa e ver produtos
    // ============================================================
    @GetMapping("/caixa/{id}")
    public String abrirCaixa(@PathVariable Long id,
                             @RequestParam(required = false) Long tarefaId,
                             Model model) {
        Caixa caixa = tarefaService.buscarCaixa(id);
        List<CaixaProduto> produtos = tarefaService.buscarProdutosDaCaixa(id);

        boolean caixaVazia = tarefaService.isCaixaVazia(id);

        model.addAttribute("caixa", caixa);
        model.addAttribute("produtos", produtos);
        model.addAttribute("tarefaId", tarefaId);
        model.addAttribute("caixaVazia", caixaVazia);
        return "tarefa/caixa-produtos";
    }

    // ============================================================
    // AÇÃO - Bipar produto e ir para tela de posições
    // ============================================================
    @PostMapping("/bipar-produto")
    public String biparProduto(@RequestParam Long tarefaId,
                               @RequestParam Long caixaId,
                               @RequestParam String codigoProduto,
                               RedirectAttributes attributes) {
        try {
            Produto produto = tarefaService.buscarProdutoPorCodigo(codigoProduto);
            Integer quantidade = tarefaService.buscarQuantidadeNaCaixa(caixaId, produto.getId());

            if (quantidade == null) {
                quantidade = 0;
            }

            return "redirect:/tarefa/posicoes?tarefaId=" + tarefaId
                    + "&caixaId=" + caixaId
                    + "&produtoId=" + produto.getId()
                    + "&quantidade=" + quantidade;

        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Produto não encontrado: " + codigoProduto);
            return "redirect:/tarefa/caixa/" + caixaId + "?tarefaId=" + tarefaId;
        }
    }

    // ============================================================
    // TELA 4 - Mostrar posições disponíveis
    // ============================================================
    @GetMapping("/posicoes")
    public String mostrarPosicoes(@RequestParam Long tarefaId,
                                  @RequestParam Long caixaId,
                                  @RequestParam Long produtoId,
                                  @RequestParam Integer quantidade,
                                  Model model) {
        Caixa caixa = tarefaService.buscarCaixa(caixaId);
        Produto produto = tarefaService.buscarProduto(produtoId);
        List<Posicao> posicoes = tarefaService.listarPosicoes();

        model.addAttribute("tarefaId", tarefaId);
        model.addAttribute("caixa", caixa);
        model.addAttribute("produto", produto);
        model.addAttribute("quantidade", quantidade);
        model.addAttribute("posicoes", posicoes);
        return "tarefa/caixa-posicoes";
    }

    // ============================================================
    // AÇÃO - Armazenar produto na posição
    // ============================================================
    @PostMapping("/armazenar")
    public String armazenar(@RequestParam Long tarefaId,
                            @RequestParam Long caixaId,
                            @RequestParam Long produtoId,
                            @RequestParam Integer quantidade,
                            @RequestParam String codigoPosicao,
                            RedirectAttributes attributes) {
        try {
            tarefaService.armazenarProduto(tarefaId, caixaId, produtoId, quantidade, codigoPosicao);
            attributes.addFlashAttribute("sucesso", "Produto armazenado com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }

        return "redirect:/tarefa/caixa/" + caixaId + "?tarefaId=" + tarefaId;
    }

    // ============================================================
    // AÇÃO - Fechar caixa (só se estiver vazia)
    // ============================================================
    @PostMapping("/fechar-caixa")
    public String fecharCaixa(@RequestParam Long caixaId,
                              @RequestParam Long tarefaId,
                              RedirectAttributes attributes) {
        try {
            tarefaService.fecharCaixa(caixaId, tarefaId);
            attributes.addFlashAttribute("sucesso", "Caixa fechada com sucesso!");

            // Volta para a lista de caixas da tarefa
            return "redirect:/tarefa/lista?tarefaId=" + tarefaId;

        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
            return "redirect:/tarefa/caixa/" + caixaId + "?tarefaId=" + tarefaId;
        }
    }
}