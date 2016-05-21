/* @autor Miguel Ochoa Hernández
 * @practica no. 1, 5
 * Funciones agragadas: empaquetarSolicitud, separarRespuesta
 * Funciones modificadas: run
 * Funciones modificadas practica 5: run
 *
 */

package sistemaDistribuido.sistema.clienteServidor.modoUsuarioBuzones;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;

/**
 * 
 */
public class ProcesoCliente extends Proceso 
{
    public byte contador, codigo_operacion;
    public int id_origen, id_destino;
    private int big = 0;
    public String archivo_trabajo, datos_respuesta, codigo_error;

    public static class Reintentar extends Thread
    {
        byte[] paquete;

        public Reintentar(byte[] solCliente)
        {
            this.paquete = solCliente;
        }

        @Override
        public void run() 
        {

        }
    
    }

    /**
    * 
    * @param esc
    */
    public ProcesoCliente(Escribano esc)
    {
        super(esc);
        start();
    }

    /**
     * @param solCliente contiene la informacion de la solicitud
     *
     * El metodo se encarga de llenar el arreglo que bytes que
     * compone la solicitud del cliente (1024 bytes)
     *
     * los campos son 
     *      id_origen de 4 bytes
     *      id_destino de 4 bytes
     *      codigo_operacion de 2 bytes
     *      datos_solicitud de 1014 bytes
     * @return byte[]
     */
    public byte[] empaquetarSolicitud(byte[] solCliente) 
    {
        //codop
        solCliente[8] = 0;
        solCliente[9] = codigo_operacion;

        contador = (byte)archivo_trabajo.getBytes().length;
        solCliente[10] = contador;
        System.arraycopy(archivo_trabajo.getBytes(), 0, solCliente, 11, contador);
        return solCliente;
    }

    /**
    *    
    *  @param respCliente El arreglo de bytes contiene toda la
    *  informacion relacionada con la respuesta del servidor
    *
    *  La funcion separarRespuesta divide los datos
    *  de la solicitud del cliente
    */
    public void desempaquetarRespuesta(byte[] respCliente, int endian) 
    {
        byte[]  origen  = new byte[4], 
                destino = new byte[4];

        System.arraycopy(respCliente, 0, origen, 0, 4);
        System.arraycopy(respCliente, 4, destino, 0, 4);
        
        if (endian == big) 
        {
            id_origen  = ByteBuffer.wrap(origen).getInt();
            id_destino = ByteBuffer.wrap(destino).getInt();
        }
        else
        {
            id_origen  = ByteBuffer.wrap(origen).order(ByteOrder.LITTLE_ENDIAN).getInt();
            id_destino = ByteBuffer.wrap(destino).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }
        
        short longitud = respCliente[8];
        datos_respuesta = new String(respCliente, 9, longitud);
        System.out.println("Respuesta " + datos_respuesta);
        
        if (datos_respuesta.length() > 2)
        {
            codigo_error = datos_respuesta.substring(0, 2);
            System.out.println("codigo de error " + codigo_error);
        }
    }
    
    @Override
    public void run()
    {
        imprimeln("Iniciando Proceso del cliente");
        codigo_error = "0";

        imprimeln("Proceso cliente en ejecucion.");
        imprimeln("Esperando datos para continuar.");
        Nucleo.suspenderProceso();

        byte[] solCliente  = new byte[1024];
        byte[] respCliente = new byte[1024]; 

        try 
        {

            imprimeln("Generando mensaje a ser enviado, llenando los campos necesarios.");

            solCliente = empaquetarSolicitud(solCliente);
            do
            {
                Nucleo.send(248 ,solCliente);

                imprimeln("Invocando a recive");
                Nucleo.receive(dameID(),respCliente);
                imprimeln("Procesando respuesta del servidor");

                desempaquetarRespuesta(respCliente, 1);

                imprimeln("Resultado enviado: " + datos_respuesta);
                Pausador.pausa(5000);
            }while(codigo_error.equals("-1"));
        }
        catch(java.lang.NullPointerException e)
        {
            imprimeln("No se envio solicitud alguna");
        }
        finally 
        {
            imprimeln("Finalizando proceso del cliente");
        }
    }
}
