import java.util.ArrayList;
import java.util.List;

public class Viagem {

   private List<Participante> listas = new ArrayList<>();

   private List<Despesa> lista = new ArrayList<>();

   public List<Participante> getlistas() {
      return this.listas;
   }
   public List<Despesa> getListaDespesa() {
      return this.lista;
   }

   public void adcionandoUsuario(Participante adicionado) {

      if (adicionado.getNome()==null || adicionado.getNome().trim().isEmpty()) {
         throw new IllegalArgumentException("Adicione um nome");
      }
         if (adicionado.getOrcamento() < 0) {
         throw new IllegalArgumentException("Valor invalido.");
      }
      if (listas.contains(adicionado)) {
         throw new IllegalArgumentException("Usuario já existe.");
      }
      listas.add(adicionado);
   }

   public void identificandoValores(Despesa identificando) {
      if (identificando.getValor() <= 0) {
         throw new IllegalArgumentException("Valor invalido.");
      }
      if (identificando.getparticipanteDespesas() == null || identificando.getparticipanteDespesas().isEmpty()) {
         throw new IllegalArgumentException("Adicione o participante.");
      }
      lista.add(identificando);
   }

   public void excluirUsuario(Participante p) {
      for (Despesa existente : lista) {
         if (existente.getparticipanteDespesas().contains(p)) {
            throw new IllegalArgumentException("Usuario vinculado a despesa, não é possivel excluir no momento" + existente.getDescricaodespesas());
         }
      }
      listas.remove(p);
   }

   public int despesasPorusuario(Participante p) {
      int saldo = 0;
      for (Despesa existente : lista) {
         saldo += existente.calculoPorUsuario(p);
      }
      return saldo;
   }
      public int calculoDoSaldousuario (Participante p){
         return p.getOrcamento() - despesasPorusuario(p);
      }

      public int CalcularTotalViagem(){
      int total=0;
      for (Participante p : listas) {
         total +=despesasPorusuario(p);
      }
      return total;
      }

   }

