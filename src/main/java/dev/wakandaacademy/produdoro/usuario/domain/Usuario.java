package dev.wakandaacademy.produdoro.usuario.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import java.util.UUID;

import javax.validation.constraints.Email;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
	@Id
	private UUID idUsuario;
	@Email
	@Indexed(unique = true)
	private String email;
	private ConfiguracaoUsuario configuracao;
	@Builder.Default
	private StatusUsuario status = StatusUsuario.FOCO;
	@Builder.Default
	private Integer quantidadePomodorosPausaCurta = 0;

	public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
		this.idUsuario = UUID.randomUUID();
		this.email = usuarioNovo.getEmail();
		this.status = StatusUsuario.FOCO;
		this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
	}


	public void pertenceAoUsuario(Usuario usuarioPorEmail) {
		if (!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário(a) não autorizado(a) para a requisição solicitada!");
		}
	}


		public void alteraStatusParaFoco (UUID idUsuario) {
			pertenceAoUsuario(idUsuario);
			verificaStatusAtual();
		}

		private void verificaStatusAtual () {
			if (this.status.equals(StatusUsuario.FOCO)) {
				throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já esta em FOCO!");
			}
			mudaStatusParaFoco();
		}


		private void mudaStatusParaFoco () {
			this.status = StatusUsuario.FOCO;
		}

		public void validaUsuarioDaTarefa (UUID idUsuario){
			if (!this.idUsuario.equals(idUsuario)) {
				throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da Tarefa solicitada!");
			}
		}

		private void pertenceAoUsuario (UUID idUsuario){
			if (!this.idUsuario.equals(idUsuario)) {
				throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é válida!");
			}
		}

		public void mudaStatusPausaCurta (UUID idUsuario){
			pertenceAoUsuario(idUsuario);
			validaStatusPausaCurta();
			this.status = StatusUsuario.PAUSA_CURTA;
		}

	private void validaStatusPausaCurta() {
		if (this.status.equals(StatusUsuario.PAUSA_CURTA)) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em Pausa Curta!");
		}
	}

	public void mudaStatusPausaLonga(UUID idUsuario) {
		pertenceAoUsuario(idUsuario);
		validaStatusPausaLonga();
		this.status = StatusUsuario.PAUSA_LONGA;
	}


	private void validaStatusPausaLonga() {
		if (this.status.equals(StatusUsuario.PAUSA_LONGA)) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em Pausa Longa!");
		}
	}
}
