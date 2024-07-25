package dev.wakandaacademy.produdoro.usuario.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
	
	@InjectMocks
	private UsuarioApplicationService usuarioApplicationService;
	
	@Mock
	private UsuarioRepository usuarioRepository;
	
	@Test
	void deveMudarStatusParaPausaCurta () {
		//Dado
		Usuario usuario = DataHelper.createUsuario();
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), usuario.getIdUsuario());
		//Então
		assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
		verify(usuarioRepository, times(1)).salva(usuario);
	}
	
	@Test
	void naoDeveMudarStatusParaPausaCurta_QuandoPassarIdInvalido() {
		//Dado
		Usuario usuario = DataHelper.createUsuario();
		UUID idUsuarioInvalido = UUID.fromString("fa69e937-a983-435a-acc4-61892718c97b");
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		APIException e = assertThrows(APIException.class, 
				() -> usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), idUsuarioInvalido));
		//Entao
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());	
	}

	@Test
	void deveMudarStatusParaPausaLonga () {
		//Dado
		Usuario usuario = DataHelper.createUsuario2();
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
		usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
		//Então
		assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
		verify(usuarioRepository, times(1)).salva(usuario);
	}

	@Test
	void naoDeveMudarStatusParaPausaLonga_QuandoPassarIdInvalido() {
		//Dado
		Usuario usuario = DataHelper.createUsuario2();
		UUID idUsuarioInvalido = UUID.fromString("fa69e937-a983-435a-acc4-61892718c97b");
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		APIException e = assertThrows(APIException.class,
				() -> usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), idUsuarioInvalido));
		//Entao
		assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());
	}

}