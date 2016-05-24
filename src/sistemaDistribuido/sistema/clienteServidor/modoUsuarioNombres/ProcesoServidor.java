//Gonzalo Daniel Sanchez De Luna
//P5
// D04
package sistemaDistribuido.sistema.clienteServidor.modoUsuarioNombres;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.ParMaquinaProceso;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.atributos_asaSN;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.sistema.rpc.modoUsuario.atributos_asa;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;

/**
 * @param <Emision>
 * 
 */
public class ProcesoServidor<Emision> extends Proceso{

	/**
	 * 
	 */
	public ProcesoServidor(Escribano esc){
		super(esc);
		start();
	}

	/**
	 * 
	 */
	public void run(){
		imprimeln("Proceso servidor en ejecucion.");
		byte[] solServidor=new byte[1024];
		byte[] respServidor= new byte[1024], buzon;
		String dato, mensaje = "";
		int com;
		int origen;
		int id_serv;
		ParMaquinaProceso asa=null;
		String servidor="Esta bien dificil la practica";
		
		try {
			asa = new atributos_asa(InetAddress.getLocalHost().getHostAddress(), Nucleo.dameIdProceso());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		id_serv=Nucleo.registrar_servidor(servidor,asa);
                
                Nucleo.registrarBuzon(dameID());
		
		while(continuar()){

			buzon = Nucleo.revisaBuzon(dameID(), solServidor);
                        if (buzon != null) 
                        {
                            imprimeln("Atendiendo solicitud del buzon");
                            System.arraycopy(buzon, 0, solServidor, 0, buzon.length);
                        }
                        else
                        {
                            imprimeln("Invocando a recive");
                            Nucleo.receive(dameID(),solServidor);
                        }
			//estoy asigandole el origen directamente
			origen=solServidor[3];
			imprimeln("Procesando solicitud recibida del cliente");
			Pausador.pausa(1000);  //sin esta l�nea es posible que Servidor solicite send antes que Cliente solicite receive
			com= (int) solServidor[9];
			
			// esto se lo puse porque al mandar llamar al metodo anterior se estaba borrando el origen y destino
			respServidor[3] = solServidor[3];//origen
			respServidor[7] = solServidor[7];//dstino
			dato=new String(solServidor,11,solServidor[10]);
			switch (com){
			
			case 1: 
				imprimeln("Creando archivo con el nombre de " + dato);
				mensaje="Archivo creado "+dato;
				break;
			case 2:
				imprimeln("Eliminado archivo " + dato);
				 mensaje="El archivo "+dato+ " se ha elminado";
				break;
				
			case 3:
				imprimeln("Leyendo archivo " + dato);
				mensaje="El archivo "+ dato +" se ha leido";
				break;
				
			case 4:
				imprimeln("Modificando archivo " +dato);
				mensaje="Se ha escrito en el archivo "+ dato;
				break;
			}//switch
			
			respServidor=new byte[1024];
			imprimeln("Creando mensaje que vamos a responder");
			
			//esto es lo que hacia el metodo datos_cliente
			byte [] Cadena=mensaje.getBytes();
			respServidor[10]=(byte)mensaje.length();
			System.arraycopy(Cadena, 0, respServidor, 11, respServidor[10]);
			imprimeln("Enviando respuesta");
			Nucleo.send(origen,respServidor);
                        Pausador.pausa(5000);
		}
		Nucleo.deregistro_servidor(id_serv);
	}

	//Eliminar el archivo 
	public String elimnar_archivo(String dato) {
		
		String mensaje=" ";
		File a=new File(dato+".txt");
		if(!a.exists())
			mensaje="No existe el archivo que quieres eliminar";
		else if (a.delete())
			   mensaje="El archivo "+dato+ " se ha elminado";

			else
			   mensaje="El archivo no puede ser  eliminado";
		return mensaje;
	}

	
	//Crea el archivo
	public String crear_archivo(String dato) {
		String mensaje=" ";
		try {
			File a=new File(dato+".txt");
			  if (a.createNewFile())
			   mensaje="Archivo creado "+dato;
			  else
			    mensaje= "No se pudo crear archivo";
			} catch (IOException ioe) {
			  ioe.printStackTrace();
			}
		return mensaje;
	}


	private byte[] enviarDatos(String mensaje, byte[] respServidor) {
		
		byte[] mensaje_regresar= new byte[mensaje.length()+1];
		mensaje_regresar = mensaje.getBytes();
		respServidor[9]= (byte) mensaje.length();
		for(int i=10; i<mensaje.length()+10;i++){
			respServidor[i]= mensaje_regresar[i-10];
					
		}
		
		return respServidor;

	}

	private String datos_cliente(byte[] solCliente) {

		String mensaje=new String(solCliente,11,solCliente[10]);
		
		return mensaje;
	}
	
	
}
