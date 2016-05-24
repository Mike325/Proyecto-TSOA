//Gonzalo Daniel Sanchez De Luna
//P5
// D04
package sistemaDistribuido.sistema.clienteServidor.modoUsuarioNombres;

import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;

/**
 * 
 */
public class ProcesoCliente extends Proceso{
	
	private String com, datos_panel;
	
	public String datos_choice(String com){
		
		return this.com=com;
	}
	
	
	public String info_enviada(String datos_panel)
	{
		return this.datos_panel=datos_panel;
	}
	
	public ProcesoCliente(Escribano esc){
		super(esc);
		start();
	}

	/**
	 * 
	 */
	public void run(){
		imprimeln("Proceso cliente en ejecucion.");
		imprimeln("Esperando datos para continuar.");
		Nucleo.suspenderProceso();
		byte[] solCliente=new byte[1024];
		byte[] respCliente=new byte[1024];
		String respuesta_servidor;
		String servidor="Esta bien dificil la practica", codigo_error = "0";
		//com segun lo que se eligio en el choice
		
		if(com=="Crear")
			solCliente[9]=(byte)1;
		else if(com=="Eliminar")
			solCliente[9]=(byte)2;
		else if(com=="Leer")
			solCliente[9]=(byte)3;
		else if(com=="Escribir")
			solCliente[9]=(byte)4;
		

		
		
		solCliente= enviar_servidor(solCliente, datos_panel);
		imprimeln("Señalando al nucleo para enviar mensaje");
		imprimeln("Enviando datos al servidor");

		//	Nucleo.send(248,solCliente);
                do
                {
                    Nucleo.send(servidor, solCliente);
                    imprimeln("Invocando a receive()");
                    Nucleo.receive(dameID(),respCliente);
                    imprimeln("Recibiendo Datos");

                    respuesta_servidor= respues_servidor(respCliente);
                    if(respCliente[8]==-100)
                            imprimeln("Destinatario no encontrado");
                    else{
                            respuesta_servidor=new String(respCliente,11,respCliente[10]);
                            codigo_error = respuesta_servidor.substring(0, 2);
                            imprimeln("El servidor respondio \n"+ respuesta_servidor);
                    }
                    Pausador.pausa(5000);
                }while(codigo_error.contains(":"));
	}

	
	//no estaba enviando la respuesta al servidor porque tenia la posicion 8 en respCliente en vez de 9
	private String respues_servidor(byte[] respCliente) {
		
		String mensaje=new String(respCliente,11,respCliente[10]);
		
		return mensaje;
	}


	//esto con el cual llenamos la info ingresada por el cliente 
	private byte[] enviar_servidor(byte[] solCliente, String datos_panel) {
		
		byte[] tam= new byte[datos_panel.length()+1];
		int auxuliar=0;
		tam= datos_panel.getBytes();
		solCliente[10]= (byte) tam.length; 
		for(int i=11; i<datos_panel.length()+11;i++){
			solCliente[i]= tam[auxuliar];
			auxuliar++;
		}
		
		return solCliente;
	}
}
