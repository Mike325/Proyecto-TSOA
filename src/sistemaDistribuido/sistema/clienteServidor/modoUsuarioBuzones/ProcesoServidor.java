/* @autor Miguel Ochoa Hernández
 * @practica no. 1
 * Funciones agragadas: tratarSolicitud, separarSolicitud, empaquetaRespuesta
 * Funciones modificadas: run
 *
 */

package sistemaDistribuido.sistema.clienteServidor.modoUsuarioBuzones;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.MicroNucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import sistema.clienteServidor.modoMonitor.DatosServidor;


/**
 * 
 */
public class ProcesoServidor extends Proceso 
{
    byte contador;
    int codigo_operacion, id_destino, id_origen;
    String archivo_trabajo, datos_respuesta;

    String peticion, datos_enviados;

    /**
    * 
    * @param esc
    */
    public ProcesoServidor(Escribano esc)
    {
        super(esc);
        start();
    }

    /**
    *  Funcion que procesa
    *  la solicitud del cliente
    *  y realiza las operaciones necesarias 
    *  para efectuarla
    * 
    */
    public void tratarSolicitud() 
    {
        File archivo;
        String[] arreglo;
        switch (codigo_operacion) 
        {
            case 0:
                try 
                {
                    archivo = new File(datos_enviados);

                    if (archivo.createNewFile()) 
                    {
                        datos_respuesta = "Archivo creado satisfactoriamente";   
                    }
                    else 
                    {
                        datos_respuesta = "Archivo no pudo ser creado";   
                    }

                    datos_respuesta = "Archivo creado satisfactoriamente";   
                } 
                catch(java.io.IOException e)
                {
                    datos_respuesta = "No se pudo concretar la creacion del archivo";
                }
                break;

            case 1:
                archivo = new File(datos_enviados);
                if (archivo.delete()) 
                {
                    datos_respuesta = "Archivo eliminado satisfactoriamente";   
                }
                else 
                {
                    datos_respuesta = "Archivo no pudo ser eliminado";   
                }
                break;

            case 2:
                arreglo = datos_enviados.split("-");
                archivo = new File(arreglo[0]);
                int limite = Integer.parseInt(arreglo[1]);               
                datos_respuesta = "";
                
                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) 
                {
                    for (int i = 0; i < limite + 1; i++) { 
                        String temporal = br.readLine();
                        if(i == limite)
                        {
                            datos_respuesta = temporal;
                        }
                    }
                    if(datos_respuesta.isEmpty()) 
                    {
                        datos_respuesta = "La linea solicitada no existe";
                    }
                    //datos_respuesta = "";
                }
                catch(java.io.IOException e)
                {
                    datos_respuesta = "No se pudo concretar la lectura del archivo";
                }
                catch (NullPointerException e) 
                {
                    datos_respuesta = "La linea solicitada no existe";
                }
                break;

            case 3:
                arreglo = datos_enviados.split("-");
                archivo = new File(arreglo[0]);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) 
                {
                    bw.write(arreglo[1] + "\n");
                    datos_respuesta = "Archivo escrito satisfactoriamente";   
                }     
                catch(java.io.IOException e)
                {
                     datos_respuesta = "No se pudo concretar la escritura del archivo";
                }                
                break;
        }
    }

    /**
    *    
    *  @param solServidor El arreglo de bytes contiene toda la
    *  informacion relacionada con la solicitud del cliente
    *
    *  La funcion separarSolicitud divide los datos
    *  de la solicitud del cliente
    */
    public void desempaquetarSolicitud(byte[] solServidor) 
    {
        byte[]  origen  = new byte[4], 
                destino = new byte[4];

        System.arraycopy(solServidor, 0, origen, 0, 4);
        System.arraycopy(solServidor, 4, destino, 0, 4);
        
        id_origen  = ByteBuffer.wrap(origen).getInt();
        //id_destino = ByteBuffer.wrap(destino).getInt();
        id_destino = dameID();

        codigo_operacion = solServidor[9];
        byte longitud    = solServidor[10];
        datos_enviados   = new String(solServidor, 11, longitud);
    }

    /**
     * @param solCliente usado para sacar los id recibidos
     * @param respServidor contiene la informacion de la solicitud
     *
     * El metodo se encarga de llenar el arreglo que bytes que
     * compone la respuesta del cliente (1024 bytes)
     *
     * los campos son 
     *      id_origen de 4 bytes
     *      id_destino de 4 bytes
     *      datos_respuesta de 1016 bytes
     * @return 
     */
    public byte[] empaquetarRespuesta(byte[] respServidor, byte[] solCliente) 
    {
        System.arraycopy(ByteBuffer.allocate(4).putInt(id_destino).array(), 0, respServidor, 0, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(id_origen).array(), 0, respServidor, 4, 4);
        
        respServidor[8] = (byte)datos_respuesta.getBytes().length;
        
        System.arraycopy(datos_respuesta.getBytes(), 0, respServidor, 9, datos_respuesta.getBytes().length);
        return respServidor;
    }

    @Override
    public void run()
    {
        imprimeln("Iniciando Proceso del servidor");
        imprimeln("Proceso servidor en ejecucion.");

        byte[] solServidor = new byte[1024];
        byte[] buzon = null;
        byte[] respServidor;
        byte dato;

        Nucleo.registrarBuzon(dameID());

        //Practica 5 Rala
        DatosServidor objServer= new DatosServidor();
        objServer.setDestino(248);
        objServer.setId(nucleo.dameIdProceso()); 
        try 
        {
            objServer.setIp(InetAddress.getLocalHost().getHostAddress()); //creo que este es para la ip
        } 
        catch (UnknownHostException ex) 
        {
            //Logger.getLogger(ProcesoServidor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error de host desconocido");
        }
        boolean agregado = MicroNucleo.procLocales.add(objServer); //agrega el servidor a la tabla de procesos locales
        if(agregado==true)
            imprimeln("Servidor agregado a la tabla correctamente");
        else
            imprimeln("No se pudo agregar el servidor");
        //para comprobar si se guardan y eliminan correctamente
        
        MicroNucleo.numSer++;
        //Practica 5 Rala
        
        while(continuar())
        {
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

            imprimeln("Procesando petición recibida del cliente");
            desempaquetarSolicitud(solServidor);

            respServidor = new byte[1024];
            imprimeln("Peticion: " + codigo_operacion + " Datos proporcionados " + datos_enviados);
            
            tratarSolicitud();
            imprimeln("Generando mensaje a ser enviado, llenando los campos necesarios.");

            respServidor = empaquetarRespuesta(respServidor, solServidor);
            Pausador.pausa(1000);  
            imprimeln("enviando respuesta");
            System.out.println("Enviando desde " + id_destino + " a " + id_origen);
            Nucleo.send( id_origen, respServidor );
            Pausador.pausa(5000);
        }

        imprimeln("Finalizando Proceso del servidor");
    }
}
