# Guitar Trainer Engine (LibGDX)

Protótipo de jogo de ritmo estilo Guitar Hero/Guitar Trainer usando Java + LibGDX.

## Estrutura do projeto

- `core`: lógica principal do jogo
- `desktop`: launcher para execução em desktop (JVM)

## Mecânicas atuais

- Input nas lanes com `A`, `S`, `D`, `F`
- Julgamento de timing por janela de acerto:
  - `PERFECT`
  - `OK`
  - `MISS`
- Sistema de score e combo
- Mapa de notas em loop durante a run
- Tempo de partida fixo em `30s`
- Tela de resultado ao final da partida com:
  - score final
  - combo máximo
  - tempo da run

## Controles

- `A / S / D / F`: tocar notas nas lanes
- `R`: reiniciar a run imediatamente
- `ENTER` ou `SPACE` (na tela de resultado): jogar novamente

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
