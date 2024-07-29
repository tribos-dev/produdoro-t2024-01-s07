package dev.wakandaacademy.produdoro.tarefa.application.service;


import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaDetalhadoResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;


    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }
    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void incrementaPomodoro(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - incrementaPomodoro");
        Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        tarefa.incrementaPomodoro(usuarioPorEmail);
        tarefaRepository.salva(tarefa);
        tarefaRepository.processaStatusEContadorPomodoro(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - incrementaPomodoro");
    }

    @Override
    public List<TarefaDetalhadoResponse> visualizaTodasAsTarefas(String usuario, UUID idUsuario) {
        log.info("[inicial] - TarefaApplicationService - visualizaTodasAsTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.validaUsuarioDaTarefa(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.visualizaTodasAsTarefa(idUsuario);
        log.info("[finaliza] - TarefaApplicationService - visualizaTodasAsTarefas");
        return TarefaDetalhadoResponse.converte(tarefas);
    }
    private Tarefa detalhaTarefaBadRequest(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void concluiTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - concluiTarefa");
        Tarefa tarefa = detalhaTarefaBadRequest(usuario, idTarefa);
        tarefa.concluiTarefa();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - concluiTarefa");
    }
    
	@Override
	public void defineTarefaComoAtiva(UUID idTarefa, String usuarioEmail) {
		log.info("[inicia] TarefaApplicationService - defineTarefaComoAtiva");
		//Busco se o usuario ja existe, se a tarefa existe e se o usuario e o dono da tarefa.
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
		Tarefa tarefa = validarTarefa(idTarefa, usuarioPorEmail);
		//Busco se ja existe uma tarefa ativa para o usuario, se sim, inativo essa tarefa.
		Optional<Tarefa> tarefaJaAtiva =
				tarefaRepository.buscaTarefaJaAtiva(usuarioPorEmail.getIdUsuario());
		tarefaJaAtiva.ifPresent(tarefaAtiva -> {
			tarefaAtiva.defineTarefaComoInativa();
			tarefaRepository.salva(tarefaAtiva);
		});
		//Por fim, ativo e em seguida salvo a tarefa.	
		tarefa.defineTarefaComoAtiva();
		tarefaRepository.salva(tarefa);		
		log.info("[finaliza] TarefaApplicationService - defineTarefaComoAtiva");
	}
	
	private Tarefa validarTarefa (UUID idTarefa, Usuario usuarioPorEmail) {
		Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Id da Tarefa inválido!"));
		tarefa.pertenceAoUsuario(usuarioPorEmail);		
		return tarefa;	
	}

}
