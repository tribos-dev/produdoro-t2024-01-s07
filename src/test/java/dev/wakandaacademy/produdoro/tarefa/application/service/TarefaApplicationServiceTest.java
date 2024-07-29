package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.EditaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;
    
	@Mock
	UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
    
    @Test
    void deveEditarTarefa() {
    	//Dado
    	Usuario usuario = DataHelper.createUsuario();
    	Tarefa tarefa = DataHelper.createTarefa();
    	EditaTarefaRequest request = DataHelper.getEditaTarefaRequest();
    	//Quando
    	when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
    	when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
    	tarefaApplicationService.alteraTarefa(usuario.getEmail(), request, tarefa.getIdTarefa());
    	//Entao
    	verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    	verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
    	verify(tarefaRepository, times(1)).salva(tarefa);
    	assertEquals("descricao2", tarefa.getDescricao());
    }
    
    @Test
    void naoDeveEditarTarefa() {
    	UUID idTarefaInvalido = UUID.randomUUID();
    	String usuario = "thalita";
    	EditaTarefaRequest request = DataHelper.getEditaTarefaRequest();
    	when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido)).thenReturn(Optional.empty());
    	assertThrows(APIException.class, 
    			()-> tarefaApplicationService.alteraTarefa(usuario, request, idTarefaInvalido));
    	verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefaInvalido);    	
    }
    
	@Test
	@DisplayName("Deve definir a tarefa do usuário como ativa.")
	void deveDefinirTarefaComoAtiva() {
		//Dado
		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();
		Tarefa tarefaAtiva = getTarefaAtiva(usuario);
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
		when(tarefaRepository.buscaTarefaJaAtiva(usuario.getIdUsuario())).thenReturn(Optional.ofNullable(tarefaAtiva));
		tarefaApplicationService.defineTarefaComoAtiva(tarefa.getIdTarefa(), String.valueOf(usuario.getEmail()));
		//Entao
		verify(tarefaRepository, times(1)).salva(tarefa);
		verify(tarefaRepository, times(1)).buscaTarefaJaAtiva(usuario.getIdUsuario());
		verify(tarefaRepository, times(1)).salva(tarefa);
	}

	@Test
	@DisplayName("Não deve definir a tarefa do usuário como ativa.")
	void nãoDeveDefinirTarefaComoAtiva() {
		//Dado
		Usuario usuario = DataHelper.createUsuario();
		UUID idTarefaInvalido = UUID.randomUUID();
		//Quando
		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido))
			.thenThrow(APIException.build(HttpStatus.NOT_FOUND, "Id da Tarefa inválido!"));
		APIException e = assertThrows(APIException.class, () -> {
			tarefaApplicationService.defineTarefaComoAtiva(idTarefaInvalido, String.valueOf(usuario.getEmail()));
		});
		//Entao
		assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
		verify(tarefaRepository, never()).buscaTarefaJaAtiva(usuario.getIdUsuario());
		verify(tarefaRepository, never()).salva(any(Tarefa.class));
	}

	@Test
	void deveIcrementarPomodoroAUmaTarefa() {

		Usuario usuario = DataHelper.createUsuario();
		Tarefa tarefa = DataHelper.createTarefa();

		when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
		when(tarefaRepository.buscaTarefaPorId(any(UUID.class))).thenReturn(Optional.of(tarefa));

		tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), tarefa.getIdTarefa());

		verify(tarefaRepository, times(1)).salva(any(Tarefa.class));
		verify(tarefaRepository, times(1)).processaStatusEContadorPomodoro(usuario);
	}

	private static Tarefa getTarefaAtiva(Usuario usuario) {
		return Tarefa.builder().contagemPomodoro(1).idTarefa(UUID.fromString("4c70c27a-446c-4506-b666-1067085d8d85"))
				.idUsuario(usuario.getIdUsuario()).descricao("Descricao da tarefa")
				.statusAtivacao(StatusAtivacaoTarefa.ATIVA).build();
	}    
    
}
