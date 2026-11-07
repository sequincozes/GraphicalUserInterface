package domain;


public enum TipoProjeto {
    PESQUISA("Pesquisa Científica"),
    EXTENSAO("Extensão Universitária");

    private final String descricao;

    TipoProjeto(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Obtém a descrição legível da modalidade.
     * @return texto descritivo do tipo de projeto.
     */
    public String getDescricao() {
        return this.descricao;
    }
}
