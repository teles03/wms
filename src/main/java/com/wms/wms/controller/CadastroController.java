package com.wms.wms.controller;

import com.wms.wms.model.Usuario;
import com.wms.wms.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Abre a tela de cadastro: GET /cadastro
    @GetMapping("")
    public String abrirTelaCadastro(Usuario usuario) {
        return "cadastro";
    }

    // Recebe os dados do formulário: POST /cadastro/salvar
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes attributes) {
        try {
            usuarioService.cadastrar(usuario);
            attributes.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso! Faça o login.");
            return "redirect:/login";
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao cadastrar. O login já pode estar em uso.");
            return "redirect:/cadastro";
        }
    }
}