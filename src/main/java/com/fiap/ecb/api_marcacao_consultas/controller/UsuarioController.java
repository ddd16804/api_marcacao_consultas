package com.fiap.ecb.api_marcacao_consultas.controller;

import com.fiap.ecb.api_marcacao_consultas.model.Usuario;
import com.fiap.ecb.api_marcacao_consultas.service.UsuarioService;
import com.fiap.ecb.api_marcacao_consultas.security.JwtTokenProvider;
import com.fiap.ecb.api_marcacao_consultas.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final JwtTokenProvider jwtTokenProvider;

    public UsuarioController(UsuarioService usuarioService, JwtTokenProvider jwtTokenProvider) {
        this.usuarioService = usuarioService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.salvarUsuario(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getSenha());
            String token = jwtTokenProvider.gerarToken(usuario.getEmail());
            return ResponseEntity.ok().body(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Remove "Bearer " do header
            String token = authHeader.substring(7);

            // Extrai o email do token
            String email = jwtTokenProvider.obterEmailDoToken(token);

            // Busca o usuário pelo email
            Usuario usuario = usuarioService.buscarPorEmail(email);

            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
    }

    /**
     * Endpoint para admin alterar senha de qualquer usuário
     */
    @PutMapping("/{id}/senha")
    public ResponseEntity<?> alterarSenhaUsuario(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String novaSenha = request.get("novaSenha");
            if (novaSenha == null || novaSenha.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nova senha é obrigatória");
            }

            Usuario usuario = usuarioService.alterarSenha(id, novaSenha);
            return ResponseEntity.ok().body(Map.of("message", "Senha alterada com sucesso", "usuario", usuario.getNome()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
