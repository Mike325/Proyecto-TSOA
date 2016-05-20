/**Marco Antonio Montaño Martin
 * D04
 * Practica 01
 * Fecha Modificacion: 02/03/2016
 */

/**Marco Antonio Montaño Martin
 * D04
 * Practica02
 * Fecha Modificacion: 31/03/2016
 * Descripcion: cambio en el comando send
 * 
 * Fecha Modificacion: 17/04/2016 Practica 05
 */


package sistemaDistribuido.sistema.clienteServidor.modoUsuarioRala;

import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import sistema.clienteServidor.modoMonitor.DatosServidor;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.MicroNucleo;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;

/**
 * 
 */
public class ProcesoServidor extends Proceso {

        
        
        
    
	public ProcesoServidor(Escribano esc){
		super(esc);
		start();
	}

	public void run(){
           
		imprimeln("Proceso servidor en ejecucion.");
                imprimeln("Invocando a receive()");
                byte[] solServidor=new byte[1024];
		byte[] respServidor;
                String respuesta;
                int cod;
                String archivo;  //nombre del archivo
                String mensajeCompleto; //almacena el mensaje
                String mensaje=null;
                StringTokenizer st;
                
                //Practica 5
                DatosServidor objServer= new DatosServidor();
                objServer.setDestino(248);
                objServer.setId(nucleo.dameIdProceso()); 
                try 
                {
                    objServer.setIp(InetAddress.getLocalHost().getHostAddress()); //creo que este es para la ip
                } 
                catch (UnknownHostException ex) 
                {
                    Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
                }
                boolean agregado = MicroNucleo.procLocales.add(objServer); //agrega el servidor a la tabla de procesos locales
                if(agregado==true)
                    imprimeln("Servidor agregado a la tabla correctamente");
                else
                    imprimeln("No se pudo agregar el servidor");
                //para comprobar si se guardan y eliminan correctamente
                
                MicroNucleo.numSer++;
                
                /*for(int i=0;i<MicroNucleo.numSer;i++)
                {
                   int idProGuardado=MicroNucleo.procLocales.get(i).getId();
                   System.out.println(idProGuardado);
                }
                  */         
		while(continuar()){                       
			Nucleo.receive(dameID(),solServidor);
                        

                        imprimeln("Señalamiento al nucleo para envio de mensaje");
		
                    	//dato=solServidor[0];
                        cod=solServidor[8]; //almacena el codop
                        mensajeCompleto= new String(solServidor, 10, solServidor[9]);
                        try
                        {
                            st = new StringTokenizer(mensajeCompleto, "|");
                            archivo=st.nextToken();
                            if(st.hasMoreTokens())
                            {
                                mensaje=st.nextToken();
                            }   
                        imprimeln("Procesando peticion recibida del cliente");
                        imprimeln("Generando mensaje a ser enviado, llenando los campos necesarios");
                        respServidor=new byte[1024];
                        byte[] auxRespuesta = new byte[1024];
			switch(cod)
                        {
                            case 1://Crear : solo necesita el nombre del archivo
                                crearArchivo(archivo);
                                respuesta = "El archivo: " + archivo+" se creo con exito";
                                auxRespuesta=respuesta.getBytes();
                                respServidor[8]=(byte) respuesta.length();
                                System.arraycopy(auxRespuesta, 0, respServidor, 9, respServidor[8]);
                                break;   
                            case 2://Eliminar : solo necesita el nombre del archivo
                                eliminarArchivo(archivo);
                                respuesta = "El archivo: " + archivo +" se elimino con exito";
                                auxRespuesta = respuesta.getBytes();
                                respServidor[8] = (byte) respuesta.length();
                                System.arraycopy(auxRespuesta, 0, respServidor, 9, respServidor[8]);
                                break;
                            case 3://Leer : solo necesita el nombre del archivo
                                respuesta = "Se leyo del archivo "+archivo+" lo siguiente:  " + leerArchivo(archivo);
                                auxRespuesta = respuesta.getBytes();
                                respServidor[8] = (byte) respuesta.length();
                                System.arraycopy(auxRespuesta, 0, respServidor, 9, respServidor[8]);
                                //imprimeln(leerArchivo(archivo));
                                break;
                            case 4://Escribir : se escribe el nombre del archivo y separado con "|" lo que se quiere escribir
                                    //Ejemplo:  hola.txt|este es un mensaje de prueba
                                if(mensaje!=null)
                                {
                                    escribirArchivo(archivo, mensaje);
                                    respuesta = "Se escribio en el archivo "+archivo+" lo siguiente:  "+mensaje;
                                    auxRespuesta = respuesta.getBytes();
                                    respServidor[8] = (byte) respuesta.length();
                                    System.arraycopy(auxRespuesta, 0, respServidor, 9, respServidor[8]);
                                    
                                }
                                break;
                        }
                        
                        
			//respServidor[0]=(byte)(dato*dato);
			Pausador.pausa(1000);  //sin esta linea es posible que Servidor solicite send antes que Cliente solicite receive
			imprimeln("Enviando respuesta");
                        if(continuar()==true)
                        {
                            //System.out.println("se envio un mensaje");
                            Nucleo.send(solServidor[3],respServidor);//tenia 7
                        }                        
                        
                        }                 
                        catch(NoSuchElementException e)
                        {
                                
                        }
		}
                MicroNucleo.procLocales.remove(objServer); //elimina el servidor de la lista
                
                //solo sirve para saber si se elimino el servidor
                /*MicroNucleo.numSer--;
                System.out.println("\n");
                for(int i=0;i<MicroNucleo.numSer;i++)
                {
                   int idProGuardado=MicroNucleo.procLocales.get(i).getId();
                   System.out.println(idProGuardado);
                } 
                */
	}
        
        public void crearArchivo(String nombreArchivo)
        {
            try {
                RandomAccessFile raf = new RandomAccessFile(nombreArchivo, "rw");
                raf.close();
            } catch (FileNotFoundException ex) {
                System.out.println("No se pudo crear el archivo");
            } catch (IOException ex) {
                Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        public void eliminarArchivo(String nombreArchivo)
        {
            File archivo= new File(nombreArchivo);
            archivo.delete();
            
        }
        
        public String leerArchivo(String nombreArchivo)
        {
            String linea = null;
            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(nombreArchivo, "r");
                linea=raf.readLine();
                raf.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return linea;
        }
        
        public void escribirArchivo(String nombreArchivo, String mensaje)
        {
            try {
                RandomAccessFile raf = new RandomAccessFile(nombreArchivo, "rw");
                raf.seek(raf.getFilePointer());
                raf.writeBytes(mensaje);
                raf.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
}
