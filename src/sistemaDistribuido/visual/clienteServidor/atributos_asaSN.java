//Gonzalo Daniel Sanchez De Luna
//P5
// D04
package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

public class atributos_asaSN {

    private String nombre_servidor;
    private ParMaquinaProceso ASA;
    
	public atributos_asaSN(String nombre_servidor, ParMaquinaProceso ASA) {
		this.nombre_servidor=nombre_servidor;
		this.ASA=ASA;
	}
	
	public String dame_nombre_servidor() {
		return nombre_servidor;
	}
	
	public ParMaquinaProceso dame_asa() {
		return ASA;
	}

}
