grammar FactorioPlanner;

// A regra principal que define a estrutura de um programa completo
// Um programa consiste em um bloco de definições seguido por um bloco de produção
programa : definicoes PRODUCAO ':' producao_final EOF;

// Define o bloco de definições, que pode conter zero ou mais regras de entidade
definicoes : regra_entidade* ;

// Descreve a sintaxe para definir uma receita (entidade)
regra_entidade :
    ENTIDADE IDENT '['                                          // ex: entidade montadorEngrenagem [
        'consome' ':' (consumo_s=item_simples | consumo_l=lista_consumo) // Define o que a entidade consome (um item ou uma lista).
        'produz'  ':' produz=item_simples                           // Define o que a entidade produz.
    ']';

// Regra para a seção final do programa, que especifica o item a ser criado
producao_final : CRIAR item_simples;

// Regras para os diferentes tipos de "listas" de itens
lista_consumo : item_quantificado (',' item_quantificado)*; // Uma lista de um ou mais itens com quantidade.
item_quantificado : NUM 'x' IDENT;                          // Um item com quantidade, ex: 5x placaDeFerro
item_simples : IDENT;                                       // Um item simples, sem quantidade explícita.

// Palavras-chave
ENTIDADE: 'entidade';
PRODUCAO: 'Produção';
CRIAR   : 'criar';

// Definição dos TOKENS básicos
IDENT: [a-zA-Z_][a-zA-Z0-9_]*; // Identificadores (nomes de itens, entidades).
NUM: [0-9]+;                  // Números inteiros.

// Regras para ignorar espaços em branco e comentários
WS          : [ \t\r\n]+ -> skip;
COMENTARIO  : '//' .*? ('\r'|'\n'|EOF) -> skip; // Ignora comentários de linha única.