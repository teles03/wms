package com.wms.wms.service;

import com.wms.wms.model.Usuario;
import com.wms.wms.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(Usuario usuario) {
        // Criptografa a senha antes de salvar
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        
        // Define o perfil padrão como USUARIO
        usuario.setPerfil(Usuario.Perfil.USUARIO);
        
        // Salva no banco de dados
        return usuarioRepository.save(usuario);
    }
}