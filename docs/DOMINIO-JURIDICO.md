# Domínio jurídico — Processos, Movimentações e Alertas

Este documento cobre o núcleo funcional do Judi: o acompanhamento automatizado de processos judiciais. Para o esqueleto de autenticação/autorização (Funcionário/Cliente), ver os pacotes `model`, `security` e `services` na raiz do backend.

## Modelo de dados

### `Processo` (tabela `processos`)

Representa um processo judicial monitorado pelo escritório.

| Campo | Tipo | Observação |
|---|---|---|
| `numeroProcesso` | String, único | Número CNJ, ex. `0001234-56.2024.8.26.0100` |
| `cliente` | FK → `Cliente` | Parte representada pelo escritório |
| `funcionarioResponsavel` | FK → `Funcionario`, opcional | Advogado/colaborador responsável |
| `tribunal` | String | Sigla do tribunal (ex. `TJSP`, `TRF3`) — usada para resolver o alias da API do DataJud, ver `TribunalDataJud` |
| `classeProcessual`, `assunto`, `orgaoJulgador`, `grau` | String | Metadados do processo |
| `status` | `StatusProcesso`: `ATIVO, SUSPENSO, ARQUIVADO, EXTINTO` | Situação processual controlada pelo escritório |
| `dataDistribuicao`, `valorCausa` | | |
| `monitorado` | Boolean | Se `true`, entra na sincronização diária com o DataJud |
| `dataUltimaConsulta`, `statusUltimaConsulta`, `mensagemErroUltimaConsulta` | | Resultado da última tentativa de sincronização (`StatusConsultaDataJud`: `NUNCA_CONSULTADO, SUCESSO, ERRO`) |
| `dataUltimaMovimentacao` | | Atualizado sempre que uma movimentação mais recente é registrada (manual ou DataJud) |
| `ativo` | Boolean | Soft delete — igual ao padrão já usado em `Cliente`/`Funcionario` |

### `MovimentacaoProcesso` (tabela `movimentacoes_processo`)

Histórico de andamentos de um processo. `origem` distingue registros manuais (`MANUAL`) dos importados automaticamente (`DATAJUD`). A deduplicação na sincronização usa o par (`codigoMovimento`, `dataMovimentacao`) por processo.

### `AlertaProcesso` (tabela `alertas_processo`)

Notificações geradas para a equipe. `tipo`: `NOVA_MOVIMENTACAO, ERRO_CONSULTA, PRAZO, OUTRO`. `severidade`: `INFO, ATENCAO, URGENTE`. Hoje são gerados automaticamente em dois casos: uma sincronização traz movimentações novas, ou uma consulta ao DataJud passa a falhar (a cada transição de sucesso→erro, para não gerar alerta repetido todo dia enquanto o erro persiste).

## Endpoints REST

Todos sob o context-path `/judi`, protegidos pela regra padrão do `SecurityConfiguration` (`anyRequest().authenticated()` — qualquer funcionário autenticado tem acesso, exceto onde anotado).

### `/processos`

| Método | Path | Papel exigido |
|---|---|---|
| GET | `/processos` | autenticado |
| GET | `/processos/nome/{conteudo}` | busca por número do processo ou nome do cliente |
| GET | `/processos/cliente/{clienteId}` | |
| GET | `/processos/funcionario/{funcionarioId}` | |
| GET | `/processos/{id}` | detalhe completo (inclui movimentações e alertas) |
| POST | `/processos` | cadastro |
| PUT | `/processos` | `ROLE_ADMIN` |
| DELETE | `/processos/delete/{id}` | `ROLE_ADMIN` — soft delete (`ativo=false`) |
| GET | `/processos/{id}/movimentacoes` | |
| POST | `/processos/{id}/movimentacoes` | registro manual de movimentação |
| POST | `/processos/{id}/sincronizar` | força consulta ao DataJud para este processo |
| POST | `/processos/sincronizar-todos` | `ROLE_ADMIN` — dispara a sincronização em lote fora do horário agendado |
| POST | `/processos/importar/{clienteId}` | multipart (`arquivo`) — importação em lote via CSV, ver abaixo |

### Importação em lote (`ProcessoService.importarCsv`)

Tela em `/judi/processo/importar`. Recebe um CSV (`multipart/form-data`, campo `arquivo`) para um cliente específico. Delimitador (`,` ou `;`) é detectado automaticamente pela primeira linha. Colunas aceitas no cabeçalho — só `numeroProcesso` é obrigatória:

```
numeroProcesso, tribunal, classeProcessual, assunto, orgaoJulgador, grau,
dataDistribuicao (yyyy-MM-dd), valorCausa, observacoes, monitorado (true/false)
```

Regras: número fora do formato CNJ (`NNNNNNN-DD.AAAA.J.TR.OOOO`) ou já cadastrado não interrompe a importação — a linha é registrada em `erros`/contada como duplicada e o processamento segue para a próxima. Ao final, cada processo novo tem sua sincronização com o DataJud disparada em background (`DataJudSyncService.sincronizarAsync`, no `asyncExecutor` já usado pelo restante do projeto), sem bloquear a resposta HTTP. O retorno (`DtoResultadoImportacaoProcessos`) traz `totalLinhas`, `importados`, `duplicados`, `invalidos` e a lista detalhada de erros por linha.

### `/alertas`

| Método | Path | |
|---|---|---|
| GET | `/alertas?apenasNaoLidos=` | paginado |
| GET | `/alertas/contagem-nao-lidos` | |
| PUT | `/alertas/{id}/lido` | |

### `/dashboard`

| Método | Path | |
|---|---|---|
| GET | `/dashboard/resumo` | contagens agregadas: processos ativos, por status, alertas não lidos, processos com erro de consulta, movimentações nos últimos 7 dias |
| GET | `/dashboard/painel` | mesmo resumo + últimas 10 movimentações e últimos 10 alertas não lidos de qualquer processo — usado pela tela `/judi/painel` |
| GET | `/dashboard/recentes` | atividade recente para a home: 5 últimas tarefas atualizadas, 5 últimos processos criados, 5 últimos casos criados e 5 próximas tarefas agendadas (status `AGENDADA`, ordenadas pela `dataAgendamento` mais próxima) |

### Busca universal (`/busca`)

| Método | Path | |
|---|---|---|
| GET | `/busca?q=` | agrega, numa única chamada, a busca por palavra já existente em Caso/Processo/Tarefa/Alerta (`buscarPorPalavra`), limitando a 5 resultados por categoria; usada pelo campo de busca da home (`/judi/home`). `q` com menos de 2 caracteres retorna listas vazias sem consultar o banco |

`BuscaUniversalService` apenas delega para `CasoService`/`ProcessoService`/`TarefaService`/`AlertaProcessoService`, sem lógica de busca própria — a query de cada categoria continua em seu respectivo repositório (`buscarPorPalavra`, com `LIKE` sobre título/número/mensagem + nome do cliente).

### Painel de TV (`/judi/painel`)

Tela full-screen pensada para ficar aberta continuamente numa TV do escritório (sem sidebar/navbar, tema escuro fixo, sem interação). Continua exigindo login normal (`ROLE_USER`) — o navegador da TV precisa logar uma vez; se a sessão expirar, a tela detecta o 401 na atualização automática e redireciona para `/judi/login`. Atualiza os dados a cada 30s via `fetch` (sem recarregar a página) e tem um `<meta http-equiv="refresh">` de 30min como rede de segurança contra vazamento de memória em sessões muito longas.

## Integração com o DataJud (CNJ)

Pacote `br.com.techgold.judi.datajud`.

1. **`DataJudProperties`** — lê `datajud.enabled`, `datajud.api-key` e `datajud.base-url` (variáveis `DATAJUD_ENABLED`, `DATAJUD_API_KEY`, `DATAJUD_BASE_URL` no `.env`). Enquanto `DATAJUD_API_KEY` estiver vazia ou `DATAJUD_ENABLED=false`, a sincronização **não faz nenhuma chamada HTTP**: cada processo monitorado é marcado com `statusUltimaConsulta=ERRO` e a mensagem "Integração com o DataJud ainda não configurada", sem gerar alerta repetido nem quebrar a aplicação.
2. **`TribunalDataJud`** — mapeia a sigla cadastrada em `Processo.tribunal` (ex. `TJSP`) para o alias do endpoint público (`api_publica_tjsp`). A lista cobre os tribunais superiores, TRFs e TJs; **revise-a ao ativar a integração em produção**, pois foi montada por referência e ainda não validada contra uma chamada real (não há chave de API disponível neste momento).
3. **`DataJudClient`** — monta o `POST {base-url}/api_publica_{alias}/_search` com header `Authorization: APIKey {key}` e um corpo de busca por `numeroProcesso` (Elasticsearch DSL). O parsing da resposta é feito via `JsonNode` de forma defensiva (não depende de um schema rígido), pois o formato de retorno pode variar entre tribunais.
4. **`DataJudSyncService`** — para cada processo `ativo && monitorado`, busca movimentos novos, grava-os via `MovimentacaoProcessoService.registrarSeNovo`, atualiza o status de consulta do processo e gera alertas quando aplicável. Falhas em um processo não interrompem a sincronização dos demais.
5. **`DataJudSyncScheduler`** — roda `sincronizarTodos()` diariamente via `@Scheduled(cron = "${datajud.sync-cron:0 0 6 * * *}")` (06:00 por padrão, configurável por `DATAJUD_SYNC_CRON`), em thread assíncrona (`asyncExecutor`, já usado no restante do projeto).

### Ativando a integração

1. Solicitar acesso à API Pública do DataJud junto ao CNJ (ver [datajud-wiki.cnj.jus.br/api-publica](https://datajud-wiki.cnj.jus.br/api-publica/)) e obter a API key.
2. Definir no `.env`: `DATAJUD_ENABLED=true` e `DATAJUD_API_KEY=<chave>`.
3. Conferir/ajustar `TribunalDataJud` para os tribunais realmente usados pelo escritório.
4. Acompanhar os primeiros ciclos via `statusUltimaConsulta`/`mensagemErroUltimaConsulta` de cada processo e os alertas do tipo `ERRO_CONSULTA`.

## Caso

`Caso` é o agrupador acima de Processo: o problema/assunto jurídico do cliente (ex: "Disputa contratual com Fornecedor XYZ"), que pode reunir vários processos (ação de cobrança, ação de indenização, processo administrativo — todos a mesma questão) e/ou tarefas que nunca viram processo formal (ex: "Revisão de contrato" num caso Consultivo).

```
Cliente → Caso → Processos
                → Tarefas
```

### `Caso` (tabela `casos`)

`titulo`, `descricao`, `cliente` (FK obrigatória), `funcionarioResponsavel` (FK opcional), `natureza` (`NaturezaCaso`: `CONSULTIVO, PREVENTIVO, CONTENCIOSO`), `status` (`StatusCaso`: `ABERTO, EM_ANDAMENTO, CONCLUIDO, ARQUIVADO` — só informativo, sem a trava de imutabilidade que `Tarefa` tem), `ativo` (soft delete).

### Vínculo com Processo e Tarefa

`Processo.caso` e `Tarefa.caso` são **opcionais** (nem todo processo/tarefa precisa de um caso — mantém compatibilidade com os registros já existentes, que nascem sem caso). Quando um `casoId` é informado no cadastro/edição de Processo ou Tarefa, o backend valida que `caso.cliente` é o mesmo `clienteId` informado — caso contrário, `IllegalStateException` ("O caso selecionado pertence a outro cliente."). Isso significa que o **cliente do processo/tarefa continua sendo um campo próprio** (não é derivado do caso) — os dois precisam bater, mas a fonte da verdade do cliente não migrou para dentro do caso, evitando refatorar cadastro/edição/importação de processo já existentes.

### Endpoints

| Método | Path | Observação |
|---|---|---|
| GET | `/casos`, `/casos/nome/{conteudo}`, `/casos/cliente/{id}`, `/casos/funcionario/{id}`, `/casos/{id}` | listagens/detalhe (paginado); detalhe inclui `processos` e `tarefas` vinculados |
| POST | `/casos` | cadastro |
| PUT | `/casos` | `ROLE_ADMIN` |
| PUT | `/casos/{id}/status/{status}` | qualquer autenticado |
| DELETE | `/casos/delete/{id}` | `ROLE_ADMIN` — soft delete |
| GET | `/processos/caso/{casoId}`, `/tarefas/caso/{casoId}` | processos/tarefas de um caso específico |

`DtoCadastroProcesso`/`DtoAtualizarProcesso` e `DtoCadastroTarefa`/`DtoAtualizarTarefa` ganharam o campo opcional `casoId`. `DtoProcessoList`/`DtoProcessoDetalhe`/`DtoTarefaList`/`DtoTarefaDetalhe` ganharam `casoId`/`tituloCaso` para exibição.

## Anexos de Processo

`AnexoProcesso` (tabela `anexos_processo`): `processo` (FK), `funcionario` (FK — quem enviou), `nomeOriginal`, `nomeArmazenado` (UUID gerado pelo servidor, nunca o nome enviado pelo usuário — evita colisão e travessia de diretório), `mimeType`, `tamanho`, `dataUpload`.

**Armazenamento**: em disco, sob `upload.dir` (mesma property já usada por `MediaResourceConfiguration`), na pasta `upload.dir/processos/{processoId}/{nomeArmazenado}`. Diferente do `/media/**` (que serve arquivos estáticos sem autenticação), o download passa por um endpoint dedicado que exige login — a regra padrão `anyRequest().authenticated()` do `SecurityConfiguration` já cobre isso, sem precisar de configuração extra.

| Método | Path | Observação |
|---|---|---|
| GET | `/processos/{processoId}/anexos` | lista os anexos do processo |
| POST | `/processos/{processoId}/anexos` | multipart, campo `arquivo` |
| GET | `/anexos/{id}/download` | baixa o arquivo (`Content-Disposition: attachment`) |
| DELETE | `/anexos/{id}` | quem enviou ou `ROLE_ADMIN` |

## Tarefas, Timesheets e Despesas

Módulo de controle de trabalho: tarefas ligadas a um cliente (e opcionalmente a um processo), executadas por um funcionário por vez, gerando timesheet e, opcionalmente, despesas para reembolso.

### `Tarefa` (tabela `tarefas`)

`titulo`, `descricao`, `cliente` (FK obrigatória), `caso` (FK opcional), `processo` (FK opcional), `funcionarioResponsavel` (FK opcional — quem está com a tarefa agora), `status` (`StatusTarefa`: `ABERTA, AGENDADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA`), `dataAgendamento` (preenchida só quando `status=AGENDADA`), `ativo` (soft delete). `status`/`funcionarioResponsavel` são atualizados automaticamente pelo `TimesheetService` ao iniciar/finalizar um timesheet — não precisam ser gerenciados manualmente no dia a dia (mas podem ser ajustados via `PUT /tarefas` ou `PUT /tarefas/{id}/status/{status}`, por exemplo para marcar `CONCLUIDA`/`CANCELADA`).

**Estados finais (`StatusTarefa.isFinal()` — `CONCLUIDA`/`CANCELADA`)**: uma vez lá, a tarefa trava o fluxo de trabalho — `TimesheetService` (`iniciar`, `registrarManual`, `atualizar`, `excluir`) e `DespesaTarefaService.cadastrar` recusam qualquer nova operação com `IllegalStateException`. Metadados da tarefa (título/descrição/cliente/processo) continuam editáveis via `PUT /tarefas` (`ROLE_ADMIN`), e `DespesaTarefaService.marcarReembolsada` continua funcionando normalmente (fluxo financeiro é independente do fluxo de trabalho). `TarefaService.alterarStatus`:
- Não deixa sair de um estado final (reabrir) a não ser que quem chamou seja `ROLE_ADMIN` — outros usuários recebem erro.
- Não deixa entrar em `CONCLUIDA`/`CANCELADA` se ainda houver um timesheet aberto (`dataFim IS NULL`) na tarefa — precisa finalizar o trabalho em andamento antes.
- Recusa transição direta para `AGENDADA` (não tem como carregar a data por essa rota) — use `PUT /tarefas/{id}/agendar` (`TarefaService.agendar`), que grava `status=AGENDADA` + `dataAgendamento` atomicamente. `PUT /tarefas` (edição completa) também aceita `status=AGENDADA` desde que `dataAgendamento` venha preenchido no mesmo payload.
- `dataAgendamento` é só o registro da data escolhida — **ainda não existe** um job que gera alertas a partir dela; isso é trabalho futuro. Hoje ela só serve para exibição (`GET /tarefas/{id}` e listagens já retornam o campo).

### `Timesheet` (tabela `timesheets`)

É o próprio "trabalho realizado": `tarefa`, `funcionario`, `dataInicio`, `dataFim` (nula enquanto em andamento), `origem` (`TIMER` ou `MANUAL`), `observacoes`. Dois jeitos de registrar, via `TimesheetService`:

- **Timer** — `iniciar(tarefaId, funcionario)` grava `dataInicio=agora`; `finalizar(timesheetId, funcionario)` grava `dataFim=agora` (só quem iniciou pode finalizar).
- **Manual** — `registrarManual(...)` recebe início e fim já preenchidos, para lançar trabalho retroativo.

**Regra de "um funcionário por vez"**: `iniciar` recusa (`IllegalStateException`) se já existir um timesheet aberto (`dataFim IS NULL`) para a tarefa; `registrarManual` recusa se o intervalo informado se sobrepuser a qualquer timesheet já existente da tarefa (`TimesheetRepository.existeSobreposicao`), aberto ou fechado. Isso vale tanto para lançamentos do mesmo funcionário quanto de funcionários diferentes — a tarefa nunca tem dois trabalhos simultâneos, mas pode ter vários funcionários diferentes ao longo do tempo (histórico de timesheets).

### `DespesaTarefa` (tabela `despesas_tarefa`)

Despesa de um funcionário numa tarefa, para reembolso: `tarefa`, `funcionario`, `descricao`, `valor`, `data`, `reembolsada` (Boolean, sem fluxo de aprovação — só marca paga/não paga via `PUT /despesas/{id}/reembolsar`, `ROLE_ADMIN`), `dataReembolso`.

**Comprovante (opcional, um por despesa)**: PDF ou imagem (jpg/jpeg/png/gif/webp), armazenado em disco sob `upload.dir/despesas/{despesaId}/{nomeArmazenado}` — mesmo padrão de `AnexoProcesso` (nome em disco é sempre um UUID gerado pelo servidor, nunca o nome enviado pelo usuário). Metadados na própria linha da despesa (`comprovanteNomeOriginal`, `comprovanteNomeArmazenado`, `comprovanteMimeType`, `comprovanteTamanho`). Só o funcionário que registrou a despesa ou um `ROLE_ADMIN` pode anexar/trocar/remover o comprovante; download é liberado a qualquer autenticado (mesma regra do anexo de processo).

### Endpoints

| Método | Path | Observação |
|---|---|---|
| GET | `/tarefas`, `/tarefas/nome/{conteudo}`, `/tarefas/cliente/{id}`, `/tarefas/processo/{id}`, `/tarefas/funcionario/{id}`, `/tarefas/{id}` | listagens/detalhe (paginado) |
| POST | `/tarefas` | cadastro |
| PUT | `/tarefas` | `ROLE_ADMIN` |
| PUT | `/tarefas/{id}/status/{status}` | qualquer autenticado; recusa `status=AGENDADA` (use `/agendar`) |
| PUT | `/tarefas/{id}/agendar` | body `{ dataAgendamento }` — marca `AGENDADA` com data/hora |
| DELETE | `/tarefas/delete/{id}` | `ROLE_ADMIN` — soft delete |
| GET | `/timesheets/tarefa/{tarefaId}` | histórico da tarefa |
| GET | `/timesheets/meus`, `/timesheets/aberto` | do funcionário autenticado |
| GET | `/timesheets/funcionario/{id}` | `ROLE_ADMIN` |
| POST | `/timesheets/iniciar/{tarefaId}` | inicia o timer (funcionário autenticado) |
| PUT | `/timesheets/{id}/finalizar` | finaliza o timer |
| POST | `/timesheets/manual` | lançamento manual |
| PUT | `/timesheets/{id}` | corrige início/fim/observações de um lançamento já existente (não altera se estava aberto/fechado — dono do lançamento ou `ROLE_ADMIN`) |
| DELETE | `/timesheets/{id}` | exclui um lançamento (dono ou `ROLE_ADMIN`); se era o timesheet aberto da tarefa, ela volta para `ABERTA` |
| GET | `/despesas/tarefa/{tarefaId}`, `/despesas/minhas` | |
| GET | `/despesas/funcionario/{id}`, `/despesas/pendentes` | `ROLE_ADMIN` |
| POST | `/despesas` | cadastro (funcionário autenticado) |
| PUT | `/despesas/{id}/reembolsar`, DELETE `/despesas/{id}` | `ROLE_ADMIN` |
| POST | `/despesas/{id}/comprovante` | multipart (`arquivo`) — anexa/substitui o comprovante; dono da despesa ou `ROLE_ADMIN` |
| GET | `/despesas/{id}/comprovante` | download/preview (`Content-Disposition: inline`); qualquer autenticado |
| DELETE | `/despesas/{id}/comprovante` | remove só o comprovante (mantém a despesa); dono ou `ROLE_ADMIN` |

## Decisões de escopo (registradas para não serem re-discutidas sem necessidade)

- **Cliente não possui login** — só `Funcionario` autentica no sistema hoje; `Cliente` continua sendo um cadastro gerenciado pela equipe (CRM), sem portal próprio.
- **Sem Flyway/Liquibase** — o schema continua sendo gerenciado por `hibernate.ddl-auto=update`, na mesma linha do restante do projeto.
- Havia queries nativas resíduais em `FuncionarioRepository` referenciando uma tabela `solicitacoes` (sem entidade correspondente, resquício de um template de outro negócio) — foram removidas; `Processo` é o modelo que efetivamente substitui esse conceito.
