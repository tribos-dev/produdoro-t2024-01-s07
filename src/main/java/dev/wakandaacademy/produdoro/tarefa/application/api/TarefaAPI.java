package dev.wakandaacademy.produdoro.tarefa.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization",required = true) String token, 
    		@PathVariable UUID idTarefa);

    @PostMapping("/muda-ordem/{idTarefa}")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    void mudaOrdemDaTarefa(@RequestHeader(name = "Authorization") String token, @PathVariable UUID idTarefa,
                           @RequestBody @Valid NovaPosicaoDaTarefaRequest novaPosicaoDaTarefaRequest);

    @GetMapping("/listar-tarefa/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaListResponse> listarTodasTarefas(@RequestHeader(name = "Authorization", required = true) String token,
                                                @PathVariable UUID idUsuario);

    @PatchMapping("/{idTarefa}/ativa")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void defineTarefaComoAtiva(@PathVariable UUID idTarefa, 
    		@RequestHeader(name = "Authorization", required = true) String token);
    
}
