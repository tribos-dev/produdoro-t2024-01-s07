package dev.wakandaacademy.produdoro.usuario.application.api;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@RestController
@Validated
@Log4j2
@RequiredArgsConstructor
public class UsuarioController implements UsuarioAPI {
	private final TokenService tokenService; 
	private final UsuarioService usuarioAppplicationService;

	@Override
	public UsuarioCriadoResponse postNovoUsuario(@Valid UsuarioNovoRequest usuarioNovo) {
		log.info("[inicia] UsuarioController - postNovoUsuario");
		UsuarioCriadoResponse usuarioCriado = usuarioAppplicationService.criaNovoUsuario(usuarioNovo);
		log.info("[finaliza] UsuarioController - postNovoUsuario");
		return usuarioCriado;
	}
	@Override
	public UsuarioCriadoResponse buscaUsuarioPorId(UUID idUsuario) {
		log.info("[inicia] UsuarioController - buscaUsuarioPorId");
		log.info("[idUsuario] {}", idUsuario);
		UsuarioCriadoResponse buscaUsuario = usuarioAppplicationService.buscaUsuarioPorId(idUsuario);
		log.info("[finaliza] UsuarioController - buscaUsuarioPorId");
		return buscaUsuario;
	}
	@Override
	public void mudaStatusFoco(String token, UUID idUsuario) {
		log.info("[inicia] UsuarioController - mudaStatusFoco");
		String usuario = validaTokenUsuario(token);
		usuarioAppplicationService.mudaStatusFoco(usuario, idUsuario);
		log.info("[finaliza] UsuarioController - mudaStatusFoco");		
	}
	
	private String validaTokenUsuario(String token) {
		String usuario = tokenService.getUsuarioByBearerToken(token).orElseThrow(
				()-> APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é válida"));
		return usuario;
	}
	
	public void mudaStatusParaPausaCurta(String token, UUID idUsuario) {
		log.info("[inicia] UsuarioController - mudaStatusParaPausaCurta");
		log.info("[idUsuario] {}", idUsuario);
		String email = getUsuarioByToken(token);
		usuarioAppplicationService.mudaStatusParaPausaCurta(email, idUsuario);
		log.info("[finaliza] UsuarioController - mudaStatusParaPausaCurta");		
	}

	@Override
	public void mudaStatusParaPausaLonga(String token, UUID idUsuario) {
		log.info("[inicia] UsuarioController - mudaStatusParaPausaLonga");
		log.info("[idUsuario] {}", idUsuario);
		String email = getUsuarioByToken(token);
		usuarioAppplicationService.mudaStatusParaPausaLonga(email, idUsuario);
		log.info("[finaliza] UsuarioController - mudaStatusParaPausaLonga");
	}

	private String getUsuarioByToken(String token) {
		log.debug("[token] {}", token);
		String usuario = tokenService.getUsuarioByBearerToken(token).orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
		log.info("[usuario] {}", usuario);
		return usuario;
	}	
}
