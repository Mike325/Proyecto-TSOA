/**Marco Antonio Montaño Martin
 * D04
 * Practica 01 : Practica 05
 * Fecha Modificacion: 02/03/2016
 * Fecha PRactica 05: 09/05/2016
 */

package sistemaDistribuido.sistema.clienteServidor.modoUsuario;

import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;

/**
 * 
 */
public class ProcesoCliente extends Proceso{

	/**
	 * 
	 */
	public ProcesoCliente(Escribano esc){
		super(esc);
		start();
	}
        
        private int codop;
        private String msg;
        
        public void tomarDatos(String cod, String mensaje)
        {
            if(cod.equalsIgnoreCase("Crear"))
            {
                codop=1;
            }
            else if(cod.equalsIgnoreCase("Eliminar"))
            {
                codop=2;
            }
            else if(cod.equalsIgnoreCase("Leer"))
            {
                codop=3;
            }
            else if(cod.equalsIgnoreCase("Escribir"))
            {
                codop=4;
            }
            msg=mensaje;
            
        }

	/**
	 * 
	 */
	public void run(){
		imprimeln("Proceso cliente en ejecucion.");
		imprimeln("Generando mensaje a ser enviado, llenando dlos campos necesarios.");
                
		Nucleo.suspenderProceso();
		byte[] solCliente=new byte[1024];
		byte[] respCliente=new byte[1024];
		String respuesta;
                
		solCliente[8]= (byte) codop; //es el dato que se va a enviar al servidor
		solCliente[9]= (byte) msg.length(); //guarda en el arreglo el tamaño del mensaje
                byte[] auxMensaje = new byte[msg.length()]; //arreglo de bytes auxiliar para guardar el mensaje
                auxMensaje=msg.getBytes();  //convierte y copia el mensaje a un arreglo de bytes
                System.arraycopy(auxMensaje, 0, solCliente, 10, solCliente[9]);  //agrega el mensaje a la solicitud del cliente
                
                //String mensajePrueba = new String(solCliente, 10, solCliente[9]);
                imprimeln("Señalamiento al nucleo para envio de mensaje");
                Nucleo.send(248,solCliente);  //envia la solicitud al server
                imprimeln("Invocando a receive()");
		Nucleo.receive(dameID(),respCliente);  //recibe una respuesta del servidor
		imprimeln("Procesando respuesta recibida del servidor");
                procesarRespuesta(respCliente);
                
	}
        
        public void procesarRespuesta(byte[] respuesta)
        {
            int operacion = respuesta[8];
            if(operacion==-33)
            {
                imprimeln("No se encontro servidores activos para atender la solicitud");
            }
            else
            {
                //System.out.println("entra");
                imprimeln(new String(respuesta, 9, respuesta[8]));
            }
        }
}
