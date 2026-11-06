
class Orcamento {

    private double ana = 70.00;
    private double bruno = 60.00;
    private double carla = 70.00;

    public void descontarAna(double despesa) {
        ana -= despesa;
    }

    public void descontarBruno(double despesa) {
        bruno -= despesa;
    }

    public void descontarCarla(double despesa) {
        carla -= despesa;
    }

    public void mostrarOrcamentos() {

        System.out.println("=== ORCAMENTOS ===");

        System.out.println("Ana: R$ " + ana);
        System.out.println("Bruno: R$ " + bruno);
        System.out.println("Carla: R$ " + carla);
    }
}
