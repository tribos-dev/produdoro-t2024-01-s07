package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;
    private final MongoTemplate mongoTemplate;
    private Integer contagemPomodoroPausaCurta = 0;

    @Override
    public Tarefa salva(Tarefa tarefa) {
        log.info("[inicia] TarefaInfraRepository - salva");
        try {
            tarefaSpringMongoDBRepository.save(tarefa);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
        }
        log.info("[finaliza] TarefaInfraRepository - salva");
        return tarefa;
    }
    
    @Override
    public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
        Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
        return tarefaPorId;
    }

    @Override
    public void processaStatusEContadorPomodoro(Usuario usuario) {
        log.info("[inicia] TarefaInfraRepository - processaStatusEContadorPomodoro");
        if (usuario.getStatus().equals(StatusUsuario.FOCO)) {
            if (this.contagemPomodoroPausaCurta < 3) {
                usuario.mudaStatusPausaCurta(usuario.getIdUsuario());
            } else {
                usuario.mudaStatusPausaLonga(usuario.getIdUsuario());
                this.contagemPomodoroPausaCurta = 0;
            }
        } else {
            usuario.alteraStatusParaFoco(usuario.getIdUsuario());
            this.contagemPomodoroPausaCurta++;
        }
        salvarStatusUsuario(usuario);
        log.info("[finaliza] TarefaInfraRepository - processaStatusEContadorPomodoro");
    }

    private void salvarStatusUsuario(Usuario usuario) {
        Query query = Query.query(Criteria.where("idUsuario").is(usuario.getIdUsuario()));
        Update updateUsuario = Update.update("status", usuario.getStatus());
        mongoTemplate.updateMulti(query, updateUsuario, Usuario.class);
    }
    
    @Override
    public List<Tarefa> visualizaTodasAsTarefa(UUID idUsuario) {
        log.info("[inicial] - TarefaInfraRepository - visualizaTodasAsTarefa");
        List<Tarefa> tarefas = tarefaSpringMongoDBRepository.findAllByIdUsuario(idUsuario);
        log.info("[finaliza] - TarefaInfraRepository - visualizaTodasAsTarefa");
        return tarefas;
    }
    
	@Override
	public Optional<Tarefa> buscaTarefaJaAtiva(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefaJaAtiva");
		Optional<Tarefa> tarefaJaAtiva = 
				tarefaSpringMongoDBRepository.buscaTarefaJaAtiva(StatusAtivacaoTarefa.ATIVA, idUsuario);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefaJaAtiva");
		return tarefaJaAtiva;
	}

	@Override
	public List<Tarefa> buscaTarefasConcluidas(UUID idUsuario) {
		log.info("[inicial] - TarefaInfraRepository - buscaTarefasConcluidas");
		Query query = new Query();
		query.addCriteria(Criteria.where("idUsuario").is(idUsuario).and("status").is(StatusTarefa.CONCLUIDA));
		List<Tarefa> tarefasConcluidas = mongoTemplate.find(query, Tarefa.class);
        log.info("[finaliza] - TarefaInfraRepository - buscaTarefasConcluidas");
		return tarefasConcluidas;
	}

	@Override
	public void deletaVariasTarefas(List<Tarefa> tarefasconcluidas) {
		log.info("[inicia] TarefaInfraRepository - deletaVariasTarefas");
		tarefaSpringMongoDBRepository.deleteAll(tarefasconcluidas);
		log.info("[finaliza] TarefaInfraRepository - deletaVariasTarefas");
		
	}

	@Override
	public void atualizaPosicoesDaTarefa(List<Tarefa> tarefasDoUsuario) {
		log.info("[inicia] TarefaInfraRepository - atualizaPosicoesDaTarefa");
		int tamanhoDaLista = tarefasDoUsuario.size();
		List<Tarefa> tarefasAtualizadas = IntStream.range(0, tamanhoDaLista)
				.mapToObj(i-> atualizaTarefasComNovaPosicao(tarefasDoUsuario.get(i),i)).collect(Collectors.toList());
				salvaVariasTarefas(tarefasAtualizadas);
		log.info("[finaliza] TarefaInfraRepository - atualizaPosicoesDaTarefa");
		
	}

	private void salvaVariasTarefas(List<Tarefa> tarefasAtualizadas) {
		tarefaSpringMongoDBRepository.saveAll(tarefasAtualizadas);
		
	}

	private Object atualizaTarefasComNovaPosicao(Tarefa tarefa, int novaPosicao) {
		tarefa.atualizaPosicao(novaPosicao);
		return tarefa;
	}
}
