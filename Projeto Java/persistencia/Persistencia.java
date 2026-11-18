package persistencia;

import model.Planejamento;

import java.io.File;
import java.io.IOException;

// CONTRATO de persistência: define O QUE fazer (salvar e carregar), sem dizer COMO.
// O Controller usa esta interface, nunca a classe de arquivo diretamente.
public interface Persistencia {

    void salvar(Planejamento planejamento, File arquivo) throws IOException;

    // Devolve um planejamento NOVO. Se der erro, lança IOException com mensagem clara
    // e quem chamou continua com o planejamento que já estava em uso.
    Planejamento carregar(File arquivo) throws IOException;
}
