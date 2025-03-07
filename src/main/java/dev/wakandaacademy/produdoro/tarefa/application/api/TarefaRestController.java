package dev.wakandaacademy.produdoro.tarefa.application.api;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.service.TarefaService;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
public class TarefaRestController implements TarefaAPI {
	private final TarefaService tarefaService;
	private final TokenService tokenService;

	@Override
	public TarefaIdResponse postNovaTarefa(TarefaRequest tarefaRequest) {
		log.info("[inicia]  TarefaRestController - postNovaTarefa  ");
		TarefaIdResponse tarefaCriada = tarefaService.criaNovaTarefa(tarefaRequest);
		log.info("[finaliza]  TarefaRestController - postNovaTarefa");
		return tarefaCriada;
	}

	@Override
	public TarefaDetalhadoResponse detalhaTarefa(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - detalhaTarefa");
		String usuario = getUsuarioByToken(token);
		Tarefa tarefa = tarefaService.detalhaTarefa(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - detalhaTarefa");
		return new TarefaDetalhadoResponse(tarefa);
	}

	@Override
	public void incrementaPomodoro(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - incrementaPomodoro");
		String usuario = getUsuarioByToken(token);
		tarefaService.incrementaPomodoro(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - incrementaPomodoro");
	}

	@Override
	public void concluiTarefa(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - concluiTarefa");
		String usuario = getUsuarioByToken(token);
		tarefaService.concluiTarefa(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - concluiTarefa");
	}

	private String getUsuarioByToken(String token) {
		log.debug("[token] {}", token);
		String usuario = tokenService.getUsuarioByBearerToken(token)
				.orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
		log.info("[usuario] {}", usuario);
		return usuario;
	}

	@Override
	public void deletaTarefasConcluidas(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - deletaTarefasConcluidas");
		String email = getUsuarioByToken(token);
		tarefaService.deletaTarefasConcluidas(email, idUsuario);
		log.info("[finaliza] TarefaRestController - deletaTarefasConcluidas");
	}
		
		
	@Override	
	public void alteraTarefa(String token, EditaTarefaRequest tarefaRequest, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - alteraTarefa");
		String usuario = getUsuarioByToken(token);
		tarefaService.alteraTarefa(usuario, tarefaRequest, idTarefa);
		log.info("[finaliza] TarefaRestController - alteraTarefa");
	}

	@Override
	public List<TarefaDetalhadoResponse> visualizaTodasAsTarefas(String token, UUID idUsuario) {
		log.info("[inicia] TarefaRestController - visualizaTodasAsTarefas");
		String usuario = getUsuarioByToken(token);
		List<TarefaDetalhadoResponse> tarefas = tarefaService.visualizaTodasAsTarefas(usuario, idUsuario);
		log.info("[finaliza] TarefaRestController - visualizaTodasAsTarefas");
		return tarefas;
	}

	@Override
	public void defineTarefaComoAtiva(UUID idTarefa, String token) {
		log.info("[inicia] TarefaRestController - defineTarefaComoAtiva");
		String usuarioEmail = getUsuarioByToken(token);
		tarefaService.defineTarefaComoAtiva(idTarefa, usuarioEmail);
		log.info("[finaliza] TarefaRestController - defineTarefaComoAtiva");
	}

	@Override
	public void deletaTarefa(UUID idTarefa, String token) {
		log.info("[inicia] TarefaRestController - deletaTarefa");
		String usuarioEmail = getUsuarioByToken(token);
		tarefaService.deletaTarefa(idTarefa, usuarioEmail);
		log.info("[finaliza] TarefaRestController - deletaTarefa");
	}

}
