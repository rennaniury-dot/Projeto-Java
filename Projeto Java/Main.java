

 public class Main {

    public static void main(String[] args) {
 


// CADASTRO DOS PARTICIPANTES

        Participante ana = new Participante("Ana", 70.00);

        Participante bruno = new Participante("Bruno", 60.00);

        Participante carla = new Participante("Carla", 70.00);

        // EXIBIR PARTICIPANTES

        System.out.println("Sistema de Planejamento de Viagem");

        System.out.println("Participante: " + ana.getNome());
        System.out.println("Orcamento: R$ " + ana.getOrcamento());

        System.out.println("Participante: " + bruno.getNome());
        System.out.println("Orcamento: R$ " + bruno.getOrcamento());

        System.out.println("Participante: " + carla.getNome());
        System.out.println("Orcamento: R$ " + carla.getOrcamento());

    }
}

    