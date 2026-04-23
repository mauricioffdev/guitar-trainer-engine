# Guitar Trainer Engine (LibGDX)

Protótipo de jogo de ritmo estilo Guitar Hero/Guitar Trainer usando Java + LibGDX.

## Estrutura do projeto

- `core`: lógica principal do jogo
- `desktop`: launcher para execução em desktop (JVM)

## Mecânicas atuais

- Input nas lanes com `A`, `S`, `D`, `F`
- Input de guitarra no desktop (microfone/interface), mapeado para lanes:
  - `E2 -> A`
  - `A2 -> S`
  - `D3 -> D`
  - `G3 -> F`
- Julgamento de timing por janela de acerto:
  - `PERFECT`
  - `OK`
  - `MISS`
- Sincronia de hit da guitarra com compensacao de latencia:
  - eventos de guitarra carregam timestamp de captura
  - julgamento usa tempo estimado do evento (nao apenas o frame atual)
  - offset configuravel em `GameConfig.GUITAR_INPUT_OFFSET_SECONDS`
- Janela de acerto da guitarra levemente mais tolerante que teclado (melhor robustez no mundo real)
- Sistema de score e combo
- Mapa de notas em loop durante a run
- Tempo de partida fixo em `30s`
- Tuner em tempo real para guitarra (input de microfone/interface no desktop)
- Tela de resultado ao final da partida com:
  - score final
  - combo máximo
  - tempo da run

## Controles

- `A / S / D / F`: tocar notas nas lanes
- Guitarra (desktop): tocar as cordas mapeadas (`E2`, `A2`, `D3`, `G3`) para acionar lanes
- `R`: reiniciar a run imediatamente
- `ENTER` ou `SPACE` (na tela de resultado): jogar novamente

## Guitarra / Tuner

- O launcher desktop inicia captura de input da guitarra via `TargetDataLine`.
- O HUD mostra status do input, nota detectada, cents e confianca.
- A deteccao usa melhor correspondencia entre notas alvo (E/A/D/G), com tolerancia por corda e analise entre oitavas proximas.
- Para melhor deteccao, prefira sinal limpo (ou com pouca distorcao).

## Ajuste de latencia

Se o hit parecer atrasado/adiantado com guitarra, ajuste:

- `core/src/main/java/com/guitartrainer/config/GameConfig.java`
- constante `GUITAR_INPUT_OFFSET_SECONDS`

Sugestao pratica:
- aumentar valor: compensa atraso percebido
- diminuir valor: corrige quando o hit parece adiantado

## Áudio

O jogo tenta carregar música da pasta `core/src/main/resources/audio`.

Nomes preferenciais:
- `main.mp3`
- `main.ogg`
- `main.wav`
- `mozart.mp3`
- `mozart.ogg`
- `mozart.wav`

Se nenhum desses nomes existir, o jogo tenta usar o primeiro arquivo de áudio encontrado na pasta.

## Principais classes

- `GameMain`: inicializa o jogo e define a screen inicial
- `MainGameScreen`: game loop principal (update/render), spawn, input e score
- `ResultScreen`: tela de resultado ao fim da run
- `GameObject`: classe base para objetos do jogo
- `Player`: feedback visual das lanes pressionadas
- `Note`: entidade de nota com `targetTime`
- `NoteSpawner`: spawn de notas a partir do mapa
- `InputHandler`: captura input do jogador
- `GuitarInputService`: contrato de captura de eventos de guitarra + snapshots
- `DesktopGuitarInputService`: implementacao desktop com detecao de pitch e fila de eventos com timestamp
- `PitchSnapshot`: snapshot de pitch/confianca para HUD e depuracao
- `CollisionSystem`: valida acerto por lane + timing
- `ScoreSystem`: pontuação, combo e multiplicador
- `AudioManager`: carregamento e controle de reprodução de música

## Como rodar (IntelliJ)

1. Abra a pasta raiz como projeto Gradle.
2. Aguarde a importação dos módulos (`core` e `desktop`).
3. Execute `DesktopLauncher` em `desktop/src/main/java/com/guitartrainer/DesktopLauncher.java`.

## Como rodar (Gradle)

No terminal, na raiz do projeto:

```bash
./gradlew desktop:run
```

## Uso do Codex na arquitetura

Este projeto foi desenvolvido com apoio do Codex como copiloto tecnico para:

- diagnosticar bugs de sincronizacao entre audio, spawn e julgamento de notas;
- refatorar o loop principal mantendo a arquitetura existente (sem quebrar os sistemas centrais);
- ajustar o fluxo de reinicio de partida com reset consistente de estado;
- evoluir a experiencia com tela de resultado e tempo de run configuravel;
- acelerar iteracoes com validacao rapida de compilacao e correcoes incrementais.

Importante: as decisoes finais de arquitetura, regras de jogo e priorizacao de features foram conduzidas pelo autor do projeto.
