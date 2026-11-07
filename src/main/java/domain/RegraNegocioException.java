package domain;


public class RegraNegocioException extends Exception {
    private static final long serialVersionUID = 1L;
    /**
     * Constrói uma nova exceção de negócio com mensagem explicativa.
     * @param mensagem descrição clara da violação ocorrida.
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
