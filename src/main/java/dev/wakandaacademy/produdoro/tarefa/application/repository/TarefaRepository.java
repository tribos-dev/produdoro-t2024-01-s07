package dev.wakandaacademy.produdoro.tarefa.application.repository;

import java.util.List;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository {
    Tarefa salva(Tarefa tarefa);
    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
    void processaStatusEContadorPomodoro(Usuario usuario);
    List<Tarefa> visualizaTodasAsTarefa(UUID idUsuario);
	Optional<Tarefa> buscaTarefaJaAtiva(UUID idUsuario);
	List<Tarefa> buscaTarefasConcluidas(UUID idUsuario);
	void deletaVariasTarefas(List<Tarefa> tarefasconcluidas);
	void atualizaPosicoesDaTarefa(List<Tarefa> tarefasDoUsuario);
    Integer contarTarefas(UUID idUsuario);
}
