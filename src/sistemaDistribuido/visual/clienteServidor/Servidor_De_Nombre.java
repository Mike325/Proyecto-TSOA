//Gonzalo Daniel Sanchez De Luna
//D04
//P5
package sistemaDistribuido.sistema.clienteServidor.modoMonitor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import sistemaDistribuido.visual.clienteServidor.Servidor_De_NombreFrame;

public class Servidor_De_Nombre {
	private Hashtable<Integer,Object> servidor_nombres;
	private int id=0;
	Servidor_De_NombreFrame servidorDnombres= new Servidor_De_NombreFrame();
	Servidor_De_NombreFrame frameSevidorN= new Servidor_De_NombreFrame();
	
	public Servidor_De_Nombre() {
		servidor_nombres=new Hashtable<Integer,Object>();
		frameSevidorN.setVisible(true);
	}
	
	public int registrar_servidor(String nombre_servidor,ParMaquinaProceso ASA){
		frameSevidorN.imprimelnSDN(" Registrando servidor \n");
		atributos_asaSN ds= new atributos_asaSN(nombre_servidor,ASA);
		id=id+1;
		servidor_nombres.put(id,ds);
		frameSevidorN.actualiza_ventana((String) actualizar());
		return id;
	}//registrar_servidor

	
	public boolean deregistrar_servidor(int idUnica){
		frameSevidorN.imprimelnSDN("Deregistrando Servidor \n");
		
		if(servidor_nombres.containsKey(idUnica)){
			servidor_nombres.remove(idUnica);
			frameSevidorN.actualiza_ventana((String) actualizar());
			return true;
		}
		else
			return false;
	}//deregistrar_servidor
	
	public ParMaquinaProceso buscar_servidor(String nombre_servidor){
	
		int cont=0;
		frameSevidorN.imprimelnSDN("Buscando al servidor "+nombre_servidor+"\n");
		ParMaquinaProceso asa=null;
		Random rnd = new Random();
		Enumeration elementos= servidor_nombres.elements();
		Hashtable<Integer,atributos_asaSN> x = new Hashtable<Integer,atributos_asaSN>();
		
		while(elementos.hasMoreElements()){
			atributos_asaSN y=(atributos_asaSN) elementos.nextElement();
			if(nombre_servidor.equalsIgnoreCase(y.dame_nombre_servidor())){
				x.put(Integer.valueOf(cont),y);
				cont++;				
				
			}
		}
		if(servidor_nombres.size()!=0){
			int id = (rnd.nextInt(cont));
			asa=x.get(id).dame_asa();
			frameSevidorN.imprimelnSDN("Servidor Encontrado  "+asa.dameID()+" IP: "+asa.dameIP()+"\n");
			return asa;
		}
		frameSevidorN.imprimelnSDN("No se encontro servidor");
		return asa;		
	}
	
	public String actualizar(){
		String imprime=" ";
		Enumeration elementos= servidor_nombres.elements();
		while(elementos.hasMoreElements()){
			atributos_asaSN x=(atributos_asaSN) elementos.nextElement();
			imprime=imprime+x.dame_nombre_servidor()+"    "+x.dame_asa().dameID()+"    "+x.dame_asa().dameIP()+"\n";
		}
		return imprime.toString();
	}
	
}//Servidor_De_Nombre
