grammar FactorioPlanner;

programa : definicoes PRODUCAO ':' producao_final;

definicoes : regra_entidade* ;

regra_entidade :
    ENTIDADE IDENT '['
        'consome' ':' (consumo_s=item_simples | consumo_l=lista_consumo)
        'produz'  ':' produz=item_simples
    ']';

producao_final : CRIAR item_simples;

lista_consumo : item_quantificado (',' item_quantificado)*;
item_quantificado : NUM 'x' IDENT;
item_simples : IDENT;

ENTIDADE: 'entidade';
PRODUCAO: 'Produção';
CRIAR   : 'criar';

IDENT: [a-zA-Z_][a-zA-Z0-9_]*;
NUM: [0-9]+;

WS          : [ \t\r\n]+ -> skip;
COMENTARIO  : '//' .*? ('\r'|'\n'|EOF) -> skip;