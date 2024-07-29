package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoDaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	@Override
	public void mudaOrdemDaTarefa(String emailDoUsuario, UUID idTarefa,
								  NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest) {
		log.info("[inicia] TarefaApplicationService - mudaOrdemDatarefa");
		Tarefa tarefa = detalhaTarefa(emailDoUsuario, idTarefa);
		List<Tarefa> tarefas = tarefaRepository.buscarTodasTarefasPorIdUsuario(tarefa.getIdUsuario());
		tarefaRepository.defineNovaPosicaoDaTarefa(tarefa, tarefas, novaPosicaoDaTarefaRequest);
		log.info("[finaliza] TarefaApplicationService - mudaOrdemDatarefa");
	}

	@Override
	public List<TarefaListResponse> buscarTodasTarefas(String usuario, UUID idUsuario) {
		log.info("[inicia] TarefaApplicationService - buscarTodasTarefas");
		Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
		usuarioRepository.buscaUsuarioPorId(idUsuario);
		usuarioPorEmail.validaUsuario(idUsuario);
		List<Tarefa> tarefas = tarefaRepository.buscarTodasTarefasPorIdUsuario(idUsuario);
		log.info("[finaliza] TarefaApplicationService - buscarTodasTarefas");
		return TarefaListResponse.converter(tarefas);
	}

	private Tarefa validarTarefa (UUID idTarefa, Usuario usuarioPorEmail) {
		Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Id da Tarefa inválido!"));
		tarefa.pertenceAoUsuario(usuarioPorEmail);		
		return tarefa;	
	}
	
}
