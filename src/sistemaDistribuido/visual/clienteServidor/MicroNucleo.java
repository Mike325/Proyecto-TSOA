//Gonzalo Daniel Sanchez De Luna
//D04
//P5
package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.sistema.rpc.modoMonitor.atributos_asa;

/**
 * @param <atributos_IP_ID>
 * 
 */
public final class MicroNucleo<atributos_IP_ID> extends MicroNucleoBase{
	private static MicroNucleo nucleo=new MicroNucleo();
	Hashtable<Integer, ParMaquinaProceso> tabla_emision=new Hashtable<Integer,ParMaquinaProceso>();
	Hashtable<Integer,byte[]> tabla_recepcion= new Hashtable<Integer,byte[]>();
	private Servidor_De_Nombre SN;
	
	
    public class atributos_IP_ID implements ParMaquinaProceso {
        private String ip="";
        private int id;
        atributos_IP_ID(String ip,int id){

            this.ip=ip;
            this.id=id;

        }
		@Override
		public String dameIP() {
			// TODO Auto-generated method stub
			return ip;
		}
		@Override
		public int dameID() {
			// TODO Auto-generated method stub
			return id;
		}

    }

	/**
	 * 
	 */
	private MicroNucleo(){
		tabla_emision= new Hashtable<Integer,ParMaquinaProceso>();
		tabla_recepcion= new Hashtable<Integer,byte[]>();
		SN= new Servidor_De_Nombre();
	}

	/**
	 * 
	 */
	public final static MicroNucleo obtenerMicroNucleo(){
		return nucleo;
	}

	/*---Metodos para probar el paso de mensajes entre los procesos cliente y servidor en ausencia de datagramas.
    Esta es una forma incorrecta de programacion "por uso de variables globales" (en este caso atributos de clase)
    ya que, para empezar, no se usan ambos parametros en los metodos y fallaria si dos procesos invocaran
    simultaneamente a receiveFalso() al reescriir el atributo mensaje---*/
	byte[] mensaje;

	public void sendFalso(int dest,byte[] message){
		System.arraycopy(message,0,mensaje,0,message.length);
		notificarHilos();  //Reanuda la ejecucion del proceso que haya invocado a receiveFalso()
	}

	public void receiveFalso(int addr,byte[] message){
		mensaje=message;
		suspenderProceso();
	}
	/*---------------------------------------------------------*/

	/**
	 * 
	 */
	protected boolean iniciarModulos(){
		return true;
	}

	/**
	 * 
	 */
	protected void sendVerdadero(int dest,byte[] message){
	//	sendFalso(dest,message);
	//	imprimeln("El proceso invocante es el "+super.dameIdProceso());

		
		// el error estaba porque no estaba invocando a ParMaquinaProceso y lo implemente en la clase atributos_IP_ID
		ParMaquinaProceso atributos= tabla_emision.get(dest);
		DatagramPacket datap;
		DatagramSocket socket_emision=dameSocketEmision();
		imprimeln("Buscando la maquina proceso de los que solicita el cliente");

		if(atributos!=null){
			//no estaba pasando el origeny destino corrcto
			imprimeln("Creando encabezado para enviarlo");
			message[3] = (byte) super.dameIdProceso(); //id origen
			message[7] = (byte) atributos.dameID(); //id destino
		}
		
			
		else {
			imprimeln("Creando encabezado para enviarlo");
			atributos=dameDestinatarioDesdeInterfaz();
			message[3] = (byte) super.dameIdProceso(); //id origen
			message[7] = (byte) atributos.dameID(); //id destino
			imprimeln("Mensaje recibido contiene: ubicación (máquina "+atributos.dameIP()+", proceso "+super.dameIdProceso()+" del servidor");
		}
		
		socket_emision=dameSocketEmision();
		try {
			datap=new DatagramPacket(message,message.length,InetAddress.getByName(atributos.dameIP()),damePuertoRecepcion());
			imprimeln("Enviando mensaje a traves de la red");
			socket_emision.send(datap);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	/**
	 * 
	 */
	protected void receiveVerdadero(int addr,byte[] message){
		//receiveFalso(addr,message);
		//el siguiente aplica para la práctica #2
		tabla_recepcion.put(addr,message);
		suspenderProceso();
	}


	/**
	 * Para el(la) encargad@ de direccionamiento por servidor de nombres en prï¿½ctica 5  
	 */
	protected void sendVerdadero(String dest,byte[] message){
		ParMaquinaProceso asa=null;
		asa= SN.buscar_servidor(dest);
		DatagramPacket dp;
		DatagramSocket socket_emision=dameSocketEmision();
		
		if(asa==null){
			imprimeln("No puede ser atendida la solicitud");
			
			imprimeln("Creando encabezado para enviarlo");
			message[3] = (byte) super.dameIdProceso(); //id origen
			message[7] = (byte) super.dameIdProceso(); //id destino
			message[8]=-1;
			
		}//if
		
		else{
			asa=dameDestinatarioDesdeInterfaz();
			message[3] = (byte) super.dameIdProceso(); //id origen
			message[7] = (byte) asa.dameID(); //id destino
			
		}//else

		try {
			dp=new DatagramPacket(message,message.length,InetAddress.getByName(asa.dameIP()),damePuertoRecepcion());
			imprimeln("Enviando mensaje a traves de la red");
			socket_emision.send(dp);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Para el(la) encargad@ de primitivas sin bloqueo en prï¿½ctica 5
	 */
	protected void sendNBVerdadero(int dest,byte[] message){
	}

	/**
	 * Para el(la) encargad@ de primitivas sin bloqueo en prï¿½ctica 5
	 */
	protected void receiveNBVerdadero(int addr,byte[] message){
	}

	/**
	 * 
	 */
	public void run(){

		while(seguirEsperandoDatagramas()){
			/* Lo siguiente es reemplazable en la prï¿½ctica #2,
			 * sin esto, en prï¿½ctica #1, segï¿½n el JRE, puede incrementar el uso de CPU
			 */ 
			DatagramPacket datap;
			String ip= null;
			int destino=0,origen=0;
			byte[] message=new byte[1024];
			DatagramSocket socket_recepcion= dameSocketRecepcion();
			datap=new DatagramPacket(message,message.length);
			
				try {	
					//como no se estab enviando el origen y destino correcto por eso en hilo se estaba ciclando siempre y lo enviaba al mismo servidor
					socket_recepcion.receive(datap);
					imprimeln("Se ha recido un mensaje de la red");
					origen=(byte)message[3];
					destino=(byte)message[7];
					ip=datap.getAddress().getHostAddress();	//ip origen
					imprimeln("Origen: " + origen);
					imprimeln("Destino: " + destino);
					imprimeln("IP origen: " + ip);
					Proceso proceso= dameProcesoLocal(destino);
					imprimeln("Buscando el mensaje que correspondete al destino del mensaje recibido");
					
					if(proceso!=null){//Verifica que exista
						//como ya no estaba usando los metodos los elimine
						byte[] arregloSolicitud=new byte[1024];
						arregloSolicitud=tabla_recepcion.get(destino);
						if(buscarPar_tabla_recepcion(destino)){//verifica que este en la tabla de recepcion
							registra_tabla_emison(origen,ip,origen);
							imprimeln("Copiando mensaje hace el proceso");
							System.arraycopy(message,0,arregloSolicitud,0,message.length);
							tabla_recepcion.remove(destino);
							reanudarProceso(proceso);
							}
						}//if_buscar
					else{
						try {//AU
							byte[] mensaje=new byte[9];
							mensaje[8]= (byte) -100;
							mensaje[3] = (byte) 0;
							mensaje[7] = (byte) origen;
							
							datap=new DatagramPacket(mensaje,mensaje.length,InetAddress.getByName(ip),damePuertoRecepcion());
							DatagramSocket ds1 = dameSocketRecepcion();
							ds1.send(datap);
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}//try socket_recepcion
			catch(SocketException ex){
				System.out.println("Error en el socket: "+ex.getMessage());
		}catch(IOException e){
				System.out.println("IOException: "+e.getMessage());
		}
		}//while
	}

	private boolean buscarPar_tabla_recepcion(int destino) {
		
		return tabla_recepcion.containsKey(Integer.valueOf(destino));
	}

	private void registra_tabla_emison(int origen, String ip, int id) {

		atributos_IP_ID nueva= new atributos_IP_ID(ip,id);
		tabla_emision.put(Integer.valueOf(origen),nueva);
		
	}


	public void registra_tabla_emison(sistemaDistribuido.sistema.rpc.modoUsuario.atributos_asa asa) {
		atributos_IP_ID nueva = new atributos_IP_ID(asa.dameIP(), asa.dameID());
		tabla_emision.put(new Integer(nueva.dameID()), nueva);
		
	}



	public int registrar_servidor(String nombre_servidor, ParMaquinaProceso asa) {
		return SN.registrar_servidor(nombre_servidor, asa);
	}

	public boolean deregistrar_servidor(int id) {
		return SN.deregistrar_servidor(id);	
	}

	public ParMaquinaProceso buscar_servidor(String nombre_servidor){
		return SN.buscar_servidor(nombre_servidor);		
	}

}
