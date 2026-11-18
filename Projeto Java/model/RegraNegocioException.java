package model;

// Erro lançado quando uma regra do planejamento é violada
// (campo vazio, valor inválido, código repetido, participante inexistente...).
// A mensagem é escrita para ser mostrada direto ao usuário.
public class RegraNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
