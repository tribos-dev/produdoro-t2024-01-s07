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

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
	@InjectMocks
	UsuarioApplicationService usuarioApplicationService;
	
	@Mock
	UsuarioRepository usuarioRepository;
	
	@Test
	void deveMudarParaFoco() {
		Usuario usuario = DataHelper.createUsuario();
		//UUID idUsuario = UUID.fromString("a713162f-20a9-4db9-a85b-90cd51ab18f4");
		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		//when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenReturn(usuario);
		usuarioApplicationService.mudaStatusFoco(usuario.getEmail(), usuario.getIdUsuario());
		verify(usuarioRepository, times(1)).salva(any());
	}
}