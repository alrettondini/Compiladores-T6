# Compiladores-T5
Trabalho 6 da Disciplina de Compiladores
Implementação de um compilador para a linguagem FactorioPlanner, responsável pelo cálculo e planejamento de cadeias de produção de itens para o jogo Factorio.

Integrantes:
- André Luis Zitelli Rettondini, 802058;
- William Matsuda, 812305;
- Giovanni Rossi, 801301;

# Documentação

## 1\. Visão Geral

O FactorioPlanner é um processador para uma linguagem desenvolvida para descrever, calcular e planejar cadeias de produção. Inspirada no jogo de automação *Factorio*, a ferramenta permite ao usuário definir receitas de fabricação e, a partir de um item final desejado, calcula a quantidade total de máquinas e recursos brutos necessários para a produção.

Ele não é um compilador tradicional que gera código de máquina, mas sim um analisador e uma calculadora que processa a lógica de produção e gera um plano de requisitos como saída.

## 2\. A Linguagem

A linguagem foi projetada para ser simples e declarativa, focando exclusivamente na definição de receitas e no objetivo de produção.

### Sintaxe Básica

Um arquivo de código na linguagem FactorioPlanner (tipicamente com uma extensão `.fp`) é composto por duas partes principais: a **definição de entidades (receitas)** e a **seção de produção**.

#### Comentários

Qualquer texto que se segue a `//` em uma linha é considerado um comentário e é ignorado pelo compilador.

```factorio
// Isto é um comentário. O compilador não o lê.
```

#### Definição de Entidades (Receitas)

As receitas são definidas em blocos que começam com a palavra-chave `entidade`. Cada entidade representa uma máquina ou um processo de fabricação.

A estrutura é a seguinte:
`entidade` `NOME_DA_MAQUINA` `[` `consome:` `LISTA_DE_INGREDIENTES` `produz:` `ITEM_PRODUZIDO` `]`

  * **`NOME_DA_MAQUINA`**: Geralmente indica o tipo de máquina, como `montador`, `fornalha` ou `mineradora`.
  * **`consome`**: Palavra-chave que precede a lista de ingredientes necessários.
      * A lista de ingredientes pode ser um item único ou múltiplos itens separados por vírgula.
      * A quantidade de cada item é especificada com um número seguido de `x`. [cite\_start]Se nenhuma quantidade for especificada, o padrão é 1. [cite: 1]
  * **`produz`**: Palavra-chave que precede o item que é o resultado da receita.

**Exemplos:**

1.  **Receita simples:** Uma fornalha que consome 1 `ferroCru` para produzir 1 `placaDeFerro`.

    ```factorio
    entidade fornalhaFerro  [ consome: ferroCru produz: placaDeFerro ]
    ```

2.  **Receita com múltiplos ingredientes:** Uma fornalha que consome 5 `placaDeFerro` e 1 `carvao` para produzir `aco`.

    ```factorio
    entidade fornalhaAco [ consome: 5x placaDeFerro, 1x carvao produz: aco ]
    ```

#### Recursos Base

Recursos base (ou matéria-prima) são itens que aparecem na cláusula `consome`, mas nunca na cláusula `produz` de nenhuma entidade. O compilador os considera o ponto de partida da cadeia de produção. Exemplos típicos são `minerioFerro` ou `minerioCobre`.

#### Seção de Produção

Esta é a seção final do arquivo e define qual item o usuário deseja fabricar.

  * Ela começa com a palavra-chave `Produção:`.
  * Em seguida, usa o comando `criar` seguido do nome do item final.

**Exemplo:**

```factorio
Produção:
    criar motor
```

## 3\. O Compilador e Seu Funcionamento

O compilador lê um arquivo `.txt`, processa-o em várias etapas e, se não houver erros, exibe o plano de produção no console.

### Etapas do Processamento

1.  **Análise Léxica e Sintática**: O programa principal (`Main.java`) lê o arquivo de entrada. Utilizando o parser e o lexer gerados pelo ANTLR, ele verifica se o código-fonte segue as regras gramaticais da linguagem. Erros como palavras-chave faltantes ou estruturas malformadas são detectados nesta fase, com mensagens de erro claras fornecidas pelo `MyErrorListener`.

2.  **Análise Semântica**: Se a sintaxe estiver correta, o `AnalisadorSemantico` entra em ação. Ele percorre a árvore sintática para realizar validações lógicas, garantindo que o programa faça sentido. Suas principais tarefas são:

      * [cite\_start]Construir um "banco de receitas" com todas as entidades definidas. [cite: 1]
      * [cite\_start]Verificar se não há receitas duplicadas para o mesmo item. [cite: 1]
      * [cite\_start]Garantir que o item final especificado na seção `Produção` tenha uma receita correspondente. [cite: 1]
      * [cite\_start]Detectar ciclos de produção (ex: A precisa de B, e B precisa de A), que tornariam a produção impossível. [cite: 1]

3.  **Cálculo da Produção**: Com as receitas validadas, a `CalculadoraProducao` assume o controle.

      * [cite\_start]Ela recebe o item final como ponto de partida. [cite: 2]
      * [cite\_start]Usando uma função recursiva, ela navega pela árvore de dependências, somando a quantidade de cada máquina e recurso base necessário. [cite: 2]
      * Ao final, ela exibe uma lista consolidada e ordenada de todos os requisitos. [cite\_start]A ordenação agrupa os itens por tipo de entidade (montadoras, fornalhas, etc.) para maior clareza. [cite: 2]

## 4\. Como Utilizar

#### Passo 1: Escrever o Código-Fonte

Crie um arquivo de texto (ex: `meu_plano.txt`) e escreva suas receitas usando a sintaxe da linguagem FactorioPlanner.

**Exemplo de Arquivo (`teste_cadeia_profunda.fp`):**

```factorio
// Nível 0: Mineração e Fundição
entidade mineradoraFerro [ consome: minerioFerro produz: ferroCru ]
entidade fornalhaFerro  [ consome: ferroCru produz: placaDeFerro ]
entidade mineradoraCobre [ consome: minerioCobre produz: cobreCru ]
entidade fornalhaCobre  [ consome: cobreCru produz: placaDeCobre ]
entidade mineradoraCarvao [ consome: minerioCarvao produz: carvao ]
entidade fornalhaAco     [ consome: 5x placaDeFerro, 1x carvao produz: aco ]

// Nível 1: Componentes Básicos
entidade montadorEngrenagem [ consome: 2x placaDeFerro produz: engrenagem ]
entidade montadorTubo      [ consome: 1x placaDeFerro produz: tubo ]
entidade montadorCabo      [ consome: 1x placaDeCobre produz: caboDeCobre ]

// Nível 2: Componentes Avançados
entidade montadorMotor [
    consome: 1x aco, 1x engrenagem, 2x tubo
    produz: motor
]
entidade montadorCircuito [
    consome: 1x placaDeFerro, 3x caboDeCobre
    produz: circuitoEletronico
]


Produção:
    criar motor
```

#### Passo 2: Executar o Compilador

Assumindo que o projeto Java foi compilado para um arquivo JAR, você pode executá-lo através da linha de comando, passando o nome do seu arquivo-fonte como argumento.

```bash
java -jar .\target\FactorioPlanner-1.0-SNAPSHOT-jar-with-dependencies.jar caminho/para/seu/arquivo.txt
```

#### Passo 3: Interpretar a Saída

O programa exibirá o resultado no console. A saída começa indicando para qual item os cálculos estão sendo feitos, seguida pela lista de requisitos totais.

**Saída para o exemplo acima:**

```
Calculando requisitos para: motor

--- REQUISITOS TOTAIS ---
1x montadorMotor
1x montadorEngrenagem
2x montadorTubo
1x fornalhaAco
9x fornalhaFerro
1x mineradoraCarvao
9x mineradoraFerro
1x minerioCarvao
9x minerioFerro
```

Esta lista informa exatamente quantas de cada máquina e matéria-prima são necessárias para produzir um `motor` de acordo com as receitas fornecidas.