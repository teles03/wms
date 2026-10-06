package com.wms.wms.service;

import com.wms.wms.model.*;
import com.wms.wms.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final CaixaRepository caixaRepository;
    private final CaixaProdutoRepository caixaProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final PosicaoRepository posicaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public TarefaService(TarefaRepository tarefaRepository,
                         CaixaRepository caixaRepository,
                         CaixaProdutoRepository caixaProdutoRepository,
                         ProdutoRepository produtoRepository,
                         PosicaoRepository posicaoRepository,
                         MovimentacaoRepository movimentacaoRepository) {
        this.tarefaRepository = tarefaRepository;
        this.caixaRepository = caixaRepository;
        this.caixaProdutoRepository = caixaProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.posicaoRepository = posicaoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public Tarefa criarTarefa(Usuario usuario) {
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuario(usuario);
        tarefa.setInicio(LocalDateTime.now());
        tarefa.setStatus(Tarefa.StatusTarefa.EM_ANDAMENTO);
        return tarefaRepository.save(tarefa);
    }

    public void adicionarCaixaNaTarefa(Tarefa tarefa, String codigoQrCaixa, Integer ordem) {
        Caixa caixa = caixaRepository.findByCodigoQr(codigoQrCaixa)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrada: " + codigoQrCaixa));

        caixa.setStatus(Caixa.StatusCaixa.EM_TRABALHO);
        caixaRepository.save(caixa);

        TarefaCaixa tarefaCaixa = new TarefaCaixa();
        tarefaCaixa.setTarefa(tarefa);
        tarefaCaixa.setCaixa(caixa);
        tarefaCaixa.setOrdem(ordem);
        tarefaCaixa.setStatus(TarefaCaixa.StatusTarefaCaixa.PENDENTE);

        if (tarefa.getCaixasDaTarefa() == null) {
            tarefa.setCaixasDaTarefa(new ArrayList<>());
        }
        tarefa.getCaixasDaTarefa().add(tarefaCaixa);
        tarefaRepository.save(tarefa);
    }

    public Tarefa buscarTarefa(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
    }

    public Caixa buscarCaixa(Long id) {
        return caixaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrada"));
    }

    public List<CaixaProduto> buscarProdutosDaCaixa(Long caixaId) {
        return caixaProdutoRepository.findByCaixaId(caixaId);
    }

    public Produto buscarProdutoPorCodigo(String codigoBarras) {
        return produtoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Integer buscarQuantidadeNaCaixa(Long caixaId, Long produtoId) {
        return caixaProdutoRepository.findByCaixaIdAndProdutoId(caixaId, produtoId)
                .map(CaixaProduto::getQuantidade)
                .orElse(0);
    }

    public List<Posicao> listarPosicoes() {
        return posicaoRepository.findAll();
    }

    // ============================================================
    // ARMAZENAR PRODUTO - debita a quantidade da caixa
    // ============================================================
    public void armazenarProduto(Long tarefaId, Long caixaId, Long produtoId,
                                 Integer quantidade, String codigoPosicao) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        Caixa caixa = buscarCaixa(caixaId);
        Produto produto = buscarProduto(produtoId);
        Posicao posicao = posicaoRepository.findByCodigo(codigoPosicao)
                .orElseThrow(() -> new RuntimeException("Posição não encontrada: " + codigoPosicao));

        Movimentacao mov = new Movimentacao();
        mov.setTarefa(tarefa);
        mov.setCaixa(caixa);
        mov.setProduto(produto);
        mov.setPosicao(posicao);
        mov.setQuantidade(quantidade);
        mov.setDataHora(LocalDateTime.now());
        movimentacaoRepository.save(mov);

        CaixaProduto cp = caixaProdutoRepository
                .findByCaixaIdAndProdutoId(caixaId, produtoId)
                .orElseGet(() -> {
                    CaixaProduto novo = new CaixaProduto();
                    novo.setCaixa(caixa);
                    novo.setProduto(produto);
                    novo.setQuantidade(0);
                    return novo;
                });

        int novaQuantidade = cp.getQuantidade() - quantidade;
        cp.setQuantidade(novaQuantidade);
        caixaProdutoRepository.save(cp);

        verificarCaixaVazia(caixa);
    }

    // ============================================================
    // VERIFICA SE A CAIXA ESTÁ VAZIA (todos os produtos zerados)
    // ============================================================
    public boolean isCaixaVazia(Long caixaId) {
        List<CaixaProduto> produtos = caixaProdutoRepository.findByCaixaId(caixaId);
        if (produtos.isEmpty()) {
            return true;
        }
        return produtos.stream()
                .allMatch(cp -> cp.getQuantidade() == null || cp.getQuantidade() <= 0);
    }

    // Verifica e atualiza o status da caixa
    private void verificarCaixaVazia(Caixa caixa) {
        if (isCaixaVazia(caixa.getId())) {
            caixa.setStatus(Caixa.StatusCaixa.VAZIA);
            caixaRepository.save(caixa);
        }
    }

    // ============================================================
    // FECHAR CAIXA - só permite se estiver vazia
    // ============================================================
    public void fecharCaixa(Long caixaId, Long tarefaId) {
        Caixa caixa = buscarCaixa(caixaId);

        if (!isCaixaVazia(caixaId)) {
            throw new RuntimeException("A caixa ainda tem produtos. Não pode ser fechada.");
        }

        // Marca a caixa como VAZIA
        caixa.setStatus(Caixa.StatusCaixa.VAZIA);
        caixaRepository.save(caixa);

        // Marca a TarefaCaixa como CONCLUIDA
        Tarefa tarefa = buscarTarefa(tarefaId);
        if (tarefa.getCaixasDaTarefa() != null) {
            for (TarefaCaixa tc : tarefa.getCaixasDaTarefa()) {
                if (tc.getCaixa().getId().equals(caixaId)) {
                    tc.setStatus(TarefaCaixa.StatusTarefaCaixa.CONCLUIDA);
                    break;
                }
            }
            tarefaRepository.save(tarefa);
        }
    }

    // ============================================================
    // VERIFICA SE TODAS AS CAIXAS DA TAREFA ESTÃO CONCLUÍDAS
    // ============================================================
    public boolean isTarefaCompleta(Long tarefaId) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        if (tarefa.getCaixasDaTarefa() == null || tarefa.getCaixasDaTarefa().isEmpty()) {
            return false;
        }
        return tarefa.getCaixasDaTarefa().stream()
                .allMatch(tc -> tc.getStatus() == TarefaCaixa.StatusTarefaCaixa.CONCLUIDA);
    }

    public List<Tarefa> buscarTarefasEmAndamento(Usuario usuario) {
        return tarefaRepository.findByStatus(Tarefa.StatusTarefa.EM_ANDAMENTO)
                .stream()
                .filter(t -> t.getUsuario().getId().equals(usuario.getId()))
                .toList();
    }
        // ============================================================
    // ADMIN - Listar todas as tarefas ativas (para o painel)
    // ============================================================
    public List<Tarefa> listarTarefasAtivas() {
        return tarefaRepository.findByStatus(Tarefa.StatusTarefa.EM_ANDAMENTO);
    }

    // ============================================================
    // ADMIN - Buscar a última movimentação de uma tarefa
    // ============================================================
    public Movimentacao buscarUltimaMovimentacao(Long tarefaId) {
        return movimentacaoRepository.findFirstByTarefaIdOrderByDataHoraDesc(tarefaId)
                .orElse(null);
    }

    // ============================================================
    // ADMIN - Desvincular a tarefa do usuário
    // (a tarefa é cancelada, liberando o usuário)
    // ============================================================
    public void desvincularTarefa(Long tarefaId) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        tarefa.setStatus(Tarefa.StatusTarefa.CANCELADA);
        tarefa.setFim(LocalDateTime.now());
        tarefaRepository.save(tarefa);
    }

    // ============================================================
    // ADMIN - Buscar movimentações de uma tarefa
    // ============================================================
    public List<Movimentacao> listarMovimentacoes(Long tarefaId) {
        return movimentacaoRepository.findByTarefaIdOrderByDataHoraDesc(tarefaId);
    }
        // ============================================================
    // ADMIN - Listar TODAS as tarefas (para a tela de tarefas)
    // ============================================================
    public List<Tarefa> listarTodasTarefas() {
        return tarefaRepository.findAllByOrderByInicioDesc();
    }
        // ============================================================
    // ADMIN - Busca IDs de tarefas que contenham uma caixa com o código
    // ============================================================
    public List<Long> buscarIdsTarefasPorCaixa(String termo) {
        return tarefaRepository.findByCaixaCodigoContaining(termo)
                .stream()
                .map(Tarefa::getId)
                .toList();
    }
}