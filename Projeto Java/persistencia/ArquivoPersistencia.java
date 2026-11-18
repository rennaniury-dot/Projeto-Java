package persistencia;

import model.Planejamento;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

// Implementa o contrato salvando o planejamento inteiro por SERIALIZAÇÃO DE OBJETOS.
// Como Planejamento, Participante e Despesa são Serializable, o grafo completo é gravado de
// uma vez, inclusive os vínculos entre despesas e participantes.
// try-with-resources fecha os fluxos automaticamente, mesmo quando ocorre erro.
public class ArquivoPersistencia implements Persistencia {

    @Override
    public void salvar(Planejamento planejamento, File arquivo) throws IOException {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeObject(planejamento);
        }
    }

    @Override
    public Planejamento carregar(File arquivo) throws IOException {
        if (!arquivo.isFile()) {
            throw new IOException("Arquivo não encontrado: " + arquivo.getName());
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object lido = entrada.readObject();
            if (!(lido instanceof Planejamento)) {
                throw new IOException("O arquivo não contém um planejamento salvo por este programa.");
            }
            return (Planejamento) lido;
        } catch (StreamCorruptedException | EOFException e) {
            throw new IOException("O arquivo está corrompido ou não é um planejamento válido.", e);
        } catch (ClassNotFoundException e) {
            throw new IOException("O arquivo foi salvo por outra versão do programa.", e);
        }
    }
}
