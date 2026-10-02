# Guitar Trainer Engine - Documentação do Projeto

## 1. Visão Geral

O projeto `guitar-trainer-engine` é um protótipo de jogo de ritmo inspirado em Guitar Hero e Guitar Trainer, desenvolvido em Java com LibGDX. O objetivo principal é oferecer uma experiência de gameplay baseada em timing, combinações de notas e reação musical, com suporte tanto a teclado quanto a guitarra física em desktop.

A proposta do projeto vai além de um simples clone de ritmo: ele explora experimentação de sincronização, detecção de frequência de áudio, ajuste de latência e interfaces reativas em tempo real. O projeto foi criado como exercício de aprendizado e experimentação com IA, servindo como laboratório de desenvolvimento prático com Codex.

## 2. Objetivo do Projeto

O sistema busca:

- Simular uma experiência de jogo de ritmo em estilo Guitar Hero;
- Permitir input de teclado (`A`, `S`, `D`, `F`);
- Permitir input de guitarra via microfone/interface em desktop;
- Validar timing e score por janela de acerto;
- Exibir feedback visual e avaliação em tempo real;
- Explorar diferenciação de gameplay entre input humano e input analógico.

## 3. Tecnologias e Stack

### Linguagem
- Java

### Framework / runtime
- LibGDX
- LWJGL3 (desktop)

### Bibliotecas principais
- `com.badlogicgames.gdx:gdx`
- `com.badlogicgames.gdx:gdx-backend-lwjgl3`
- `com.badlogicgames.gdx:gdx-platform` (nativos desktop)

### Requisitos de execução
- JDK 17+
- Gradle
- Ambiente desktop para execução com microfone (opcional, para guitarra)

## 4. Arquitetura do Projeto

O projeto está dividido em módulos:

- `core`: lógica principal do jogo
- `desktop`: launcher e inicialização no desktop

### Estrutura principal

```text
.
├── README.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle.properties
├── config.json
├── core/
│   └── src/main/java/com/guitartrainer/
│       ├── GameMain.java
│       ├── audio/
│       ├── config/
│       ├── gameobject/
│       ├── input/
│       ├── screen/
│       └── system/
├── desktop/
│   └── src/main/java/com/guitartrainer/
│       └── DesktopLauncher.java
└── gradle/
```

## 5. Módulos e Responsabilidades

### 5.1 Módulo `core`

É responsável pela lógica principal de gameplay e arquitetura do jogo. As classes principais ficam em `com.guitartrainer`.

### 5.2 Módulo `desktop`

Responsável por abrir a janela do jogo e iniciar a aplicação LibGDX. Também instancia a implementação específica de entrada da guitarra para desktop.

## 6. Componentes Principais

### 6.1 `GameMain`

Classe principal que estende `Game` do LibGDX.

Responsabilidades:
- inicializar `SpriteBatch`;
- definir a tela inicial do jogo (`MainGameScreen`);
- expor a instância do serviço de guitarra;
- liberar recursos na finalização.

### 6.2 `MainGameScreen`

É o coração do jogo. Esta classe coordena:
- renderização do cenário;
- atualização do loop principal;
- spawn de notas;
- entrada do teclado;
- entrada da guitarra;
- colisão e score;
- timers e estados do jogo;
- música e HUD.

### 6.3 `InputHandler`

Responsável por capturar eventos do teclado e mapear entradas para lanes:
- `A`
- `S`
- `D`
- `F`

Também interpreta pressionamentos e consome eventos de forma segura durante o frame.

### 6.4 `LaneKey`

Enumeração das lanes do jogo. Naturalmente, cada lane representa um canal de notas e de input.

### 6.5 `Note`

Representa uma nota do mapa de jogo. Ela contém dados como:
- lane (canal da nota);
- tempo alvo (`targetTime`);
- estado da nota;
- posição e visualização.

### 6.6 `NoteSpawner`

Responsável por criar notas conforme a música e o mapa. Ele atualiza a fila de notas com base no tempo decorrido.

### 6.7 `CollisionSystem`

Responsável pela lógica de hit detection.

A classificação de acerto é feita por janelas temporais:
- `PERFECT`
- `OK`
- `MISS`

Há diferenciação entre input de teclado e input de guitarra:
- teclado usa janela padrão;
- guitarra usa janela ligeiramente mais tolerante;
- há compensação de offset para lidar com latência real.

### 6.8 `ScoreSystem`

Controla:
- score total;
- combo atual;
- max combo;
- multiplicador por combo;
- avaliação por hit (PERFECT / OK / MISS).

### 6.9 `AudioManager`

Gerencia:
- carregamento da música;
- reprodução/pausa;
- loop de áudio;
- sincronização de relógio com o jogo.

### 6.10 `GuitarInputService`

Interface que abstrai a coleta de eventos de guitarra. O projeto usa o padrão de dependência para permitir múltiplas implementações.

### 6.11 `DesktopGuitarInputService`

Implementação desktop que:
- acessa o `TargetDataLine` do sistema;
- captura áudio do microfone;
- detecta frequência dominante;
- interpreta notas e mapeia para as lanes do jogo;
- cria eventos com timestamp de captura;
- atualiza snapshot de tuner para o HUD.

### 6.12 `PitchSnapshot`

Estrutura de dados usada para representar:
- status da detecção;
- frequência detectada;
- confiança da medição;
- mensagem informativa para exibição na UI.

## 7. Mecânicas de Jogo

### 7.1 Inputs

#### Teclado
- `A`
- `S`
- `D`
- `F`

#### Guitarra (desktop)
O projeto mapeia notas de guitarra para lanes:

- `E2 -> A`
- `A2 -> S`
- `D3 -> D`
- `G3 -> F`

Isso transforma a guitarra em um instrumento de input para o jogo, aproximando a experiência de um simulador de guitarra.

### 7.2 Janela de acerto

A lógica usa uma janela de timing calibrada para avaliação precisa.

- `PERFECT`: maior precisão
- `OK`: acerto tolerante
- `MISS`: fora do tempo permitido

A guitarra tem uma janela um pouco mais ampliada para compensar ruído, imprecisão e latência do mundo real.

### 7.3 Score e Combo

O sistema calcula pontos por hit com multiplicador conforme evolução do combo:
- combo >= 5: multiplicador x2
- combo >= 10: multiplicador x3

Assim o jogador é incentivado a manter sequência.

### 7.4 Tempo da run

A partida tem duração fixa de 30 segundos.

Ao final da run, o jogo exibe:
- score final
- combo máximo
- tempo total

### 7.5 Tela de resultado

A classe `ResultScreen` mostra o resultado final e permite reiniciar a partida.

## 8. Lógica de Sincronização e Latência

Um dos pontos mais interessantes do projeto é o controle de latência de input da guitarra.

A lógica trabalha com timestamp de captura do evento de áudio e estima o tempo real do golpe pela diferença entre o instante de captura e o tempo atual do sistema.

Isso reduz problemas onde o input da guitarra parece atrasado ou adiantado em comparação ao gameplay.

### Configuração central

Arquivo relevante:
- `core/src/main/java/com/guitartrainer/config/GameConfig.java`

Constante relevante:
- `GUITAR_INPUT_OFFSET_SECONDS`

Essa variável ajusta a compensação de delay da guitarra. O README do projeto recomenda:
- aumentar o valor para compensar atraso;
- diminuir para corrigir acertos adiantados.

## 9. Detecção de Pitch e Tuner

O projeto também inclui um HUD de tuner que mostra:
- frequência da nota detectada;
- nome da nota;
- diferença em cents;
- confiabilidade da leitura;
- status atual da entrada.

A detecção usa melhor correspondência entre notas alvo e análise de distâncias entre oitavas e notas próximas. Isso aumenta a robustez da leitura, especialmente em sinais com ruído ou leve distorção.

## 10. Fluxo de Execução

### Inicialização
1. `DesktopLauncher` cria a aplicação LibGDX;
2. instância `GameMain` com `DesktopGuitarInputService`;
3. `GameMain.create()` inicia `MainGameScreen`;
4. o jogo carrega música e inicializa os sistemas.

### Loop principal
1. `MainGameScreen.update()` resolve tempo;
2. spawna notas;
3. coleta input do teclado e guitarra;
4. avalia colisões;
5. atualiza score, combo e efeitos visuais;
6. remove notas resolvidas;
7. calcula fim da partida ou reinício.

## 11. Arquitetura de Dados e Estado

O projto trabalha com estados do jogo como:
- `READY`
- `PLAYING`
- `FINISHED`

Além disso, cada nota passa por estados como:
- `ACTIVE`
- `HIT`
- `MISSED`

Essa separação permite controlar com clareza a vida útil das entidades e a lógica de conclusão da run.

## 12. Pontos Fortes do Projeto

### a) Realismo do input
A inserção de guitarra real como mecanismo de gameplay torna o protótipo mais interessante que um rhythm game típico.

### b) Programação prática de temporização
O projeto trata timing como um problema real de engenharia, não como um detalhe secundário. Isso é especialmente bom para aprender engenharia de sistemas interativos.

### c) Separação de responsabilidades
Há uma clara distinção entre:
- renderização;
- input;
- áudio;
- lógica de hit;
- score;
- player state.

### d) Extensibilidade
O projeto está pronto para receber:
- novos inputs;
- novas telas;
- diferentes modelos de mapa;
- múltiplas faixas;
- outras plataformas.

## 13. Pontos de Atenção

Embora muito interessante, o projeto ainda tem limitações esperadas de protótipo:

- ausência de suíte de testes automatizados;
- mapa de notas ainda simples e rígido;
- execução melhor em desktop com microfone e ambiente adequado;
- dependência de configuração de hardware/áudio para boa experiência com guitarra;
- pouca organização documental além do README principal.

## 14. Melhorias Recomendadas

### Curto prazo
- criar testes unitários para `CollisionSystem`;
- criar testes para `ScoreSystem`;
- extrair dados de mapa para JSON/CSV;
- separar configurações de gameplay em arquivo externo.

### Médio prazo
- suporte a múltiplas músicas;
- tela de seleção de nível;
- aumento de features visuais;
- criar sistema de dificuldade.

### Longo prazo
- análise de BPM e map generation automática;
- suporte a outras plataformas;
- reconhecimento de técnicas de guitarra;
- integração com IA para geração de mapas e ajustes de balanceamento.

## 15. Conclusão

O projeto `guitar-trainer-engine` é uma iniciativa muito interessante e ambiciosa: combina espírito de jogo arcade, engenharia de áudio, sincronização em tempo real e experimentação de interface em um único sistema Java + LibGDX.

O que mais chama atenção não é apenas o gameplay em si, mas a decisão de tratar entrada de guitarra real como parte essencial da lógica do jogo. Isso mostra que o objetivo do projeto é explorar conceitos de ciência de sistemas aplicados a jogos, e não apenas reproduzir um gênero conhecido.

A arquitetura atual já mostra maturidade para protótipo experimental e é um excelente ponto de partida para evoluções futuras.

## 16. Como Rodar

No terminal, na raiz do projeto:

```bash
./gradlew desktop:run
```

ou abra o projeto em IntelliJ e execute a classe:

- `desktop/src/main/java/com/guitartrainer/DesktopLauncher.java`

## 17. Como Exportar esta Documentação para PDF

Como ambiente de geração textual, não há renderização nativa de PDF diretamente neste canal. No entanto, a documentação foi preparada para ser exportada facilmente.

### Opções práticas

#### Opção 1 — GitHub + navegador
- abra o arquivo `DOCUMENTACAO_PROJETO.md` no navegador;
- use Ctrl + P;
- escolha `Salvar como PDF`.

#### Opção 2 — Pandoc
```bash
pandoc DOCUMENTACAO_PROJETO.md -o DOCUMENTACAO_PROJETO.pdf
```

#### Opção 3 — Markdown editor com exportação PDF
- VS Code com extensão;
- Obsidian;
- Typora;
- Notion exportando PDF.

## 18. Observação Final

Este projeto é um excelente exemplo de protótipo que mistura:
- desenvolvimento de jogos;
- processamento de áudio;
- sincronização temporal;
- arquitetura modular;
- experimentação com IA no ciclo de desenvolvimento.

Ele é especialmente valioso como estudo prático para quem deseja compreender como construir experiências interativas mais complexas e genuinamente técnicas.

---

Documento gerado para o repositório `mauricioffdev/guitar-trainer-engine`.
