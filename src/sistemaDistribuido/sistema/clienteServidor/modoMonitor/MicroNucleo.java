/**Marco Antonio Montaño Martin
 * D04
 * Practica 02: Practica 05
 * Fecha de modificacion: 31/03/2016
 * Fecha Modificacion: 10/05/2016 Practica 05
 */
package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.ByteBuffer;
import sistema.clienteServidor.modoMonitor.DatosServidor;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.MicroNucleoBase;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;


public final class MicroNucleo extends MicroNucleoBase{
        
    Hashtable<Integer, Par> htEmision = new Hashtable<Integer, Par>();
    Hashtable<Integer, byte[]> htRecepcion = new Hashtable<Integer, byte[]>();
    private DatagramSocket socketRecepcion;

    public static LinkedList<DatosServidor> procLocales = new LinkedList<DatosServidor>();
    public static LinkedList<DatosServidor> procRemotos = new LinkedList<DatosServidor>();

    public static int numSer=0;

    int conta=0;        
    private static MicroNucleo nucleo=new MicroNucleo();
        
    /**
    * 
    */
    private MicroNucleo(){
    }

    /**
    * 
    */
    public final static MicroNucleo obtenerMicroNucleo(){
        return nucleo;
    }
    
    // ************************************ BUZONES ***********************************
    private static Hashtable<Integer, LinkedList<byte[]>> buzones = new Hashtable<Integer, LinkedList<byte[]>>();

    public static class PairIDAndIP implements ParMaquinaProceso
    {
        int id;
        String ip;
    
        public PairIDAndIP(int id, String ip) 
        { 
            this.id = id;
            this.ip = ip;
        }

        public String dameIP() { return this.ip; }
        public int dameID()    { return this.id; }
    }
    protected void registrarBuzon(int id)
    {
        buzones.put(id, new LinkedList<byte[]>());
    }

    protected byte[] revisaBuzon(int id, byte[] solServidor) 
    {
        byte[] paquete = null;

        if(buzones.get(id).size() > 0)
        {
            paquete = buzones.get(id).poll();
            System.arraycopy(paquete, 0, solServidor, 0, paquete.length);
        }

        return paquete;
    }

    private void envioError(String mensaje_error, String ip, byte[] bytes_origen, byte[] bytes_destino) 
    {
        DatagramPacket paquete;
        DatagramSocket socket_emisor = dameSocketEmision();

        byte[] buffer   = new byte[1024];
        paquete         = new DatagramPacket(buffer, buffer.length);

        System.arraycopy(bytes_destino, 0, buffer, 0, 4);
        System.arraycopy(bytes_origen, 0, buffer, 4, 4);

        //String au = "direccion desconocida";  
        buffer[8] = (byte)mensaje_error.getBytes().length;
        
        System.arraycopy(mensaje_error.getBytes(), 0, buffer, 9, mensaje_error.getBytes().length);

        try 
        {
            //imprimeln("Proceso destinatario no encontrado según campo dest del mensaje recibido");
            DatagramPacket error = new DatagramPacket(  buffer, 
                                                        buffer.length, 
                                                        InetAddress.getByName( ip ), 
                                                        damePuertoRecepcion()
                                                        );
            
            int id_destino = ByteBuffer.wrap(bytes_origen).getInt();
            
            //System.out.println("Enviando error a " + ip + " con id " + id_destino);

            socket_emisor.send(error);
        } 
        catch (IOException ex) 
        {
            //System.out.println("Error de entra y salida al enviar mensaje de error " + mensaje_error);
            imprimeln("Error de entra y salida al enviar mensaje de error " + mensaje_error);
        }
    }
    // ************************************ FIN BUZONES ***********************************

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

        
    protected void sendVerdadero(int dest,byte[] message){
        int id;
        String ip;
        Par parDatos;
        int codop=message[8];
        int origen=nucleo.dameIdProceso();
        String nombreArchivo= new String(message, 10, message[9]);

        empaquetarEntero(message, 0, origen);
        imprimeln("El proceso que lo solicita es "+origen);
        if(htEmision.containsKey(dest))
        {
            //System.out.println("entra en la tabla");
            parDatos=htEmision.get(dest);
            ip=parDatos.getIp();
            id=parDatos.getId();
            //System.out.println("id e ip del mensaje a enviar"+ ip +"   : "+id);
            sendMensaje(id, ip, message);
        }
        else
        {
            LSA enviarLsa= new LSA(dest, message, codop, nombreArchivo);
            enviarLsa.start();
        }
    }
        
    public void sendMensaje( int id, String ip, byte[] mensaje)
    {
        //System.out.println("");
        imprimeln("Buscando proceso correspondiente al campo "+id);                           
        empaquetarEntero(mensaje, 4, id);
            
        imprimeln("Completando campos de encabezado del mensaje a ser enviado");         
        try 
        {
            DatagramPacket dp= new DatagramPacket(mensaje, mensaje.length, InetAddress.getByName(ip), damePuertoRecepcion());
            DatagramSocket socketEmision=dameSocketEmision();
            imprimeln("Enviando mensaje proveniente de la red"); 
            socketEmision.send(dp);
        }
        catch (UnknownHostException ex) 
        {
            Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class LSA extends Thread
    {
        private int dest;
        private byte[] mensaje;
        private int origen;
        private int codop;
        private String nombreArchivo;
        
        public LSA(int dest, byte[] mensaje, int codop, String nombreArchivo)
        {
            this.dest=dest;
            this.mensaje=mensaje;
            this.codop=codop;
            this.nombreArchivo=nombreArchivo;
                
                    
        }
        
        public void run()
        {
            int intentos=0;
            DatosServidor datos = null;
            origen=mensaje[3];
            //System.out.println("mensaje origen: "+mensaje[3]);                   
            
            if(!procRemotos.isEmpty())
            {
                //System.out.println("Encontramos los datos en la tabla");
                datos=buscarServidorRemoto(dest);
            }
            else
            {
                while(intentos < 3 && datos==null)
                {
                    enviarLSA(dest);
                    datos=buscarServidorRemoto(dest);
                    //System.out.println("datos : "+datos.getId()+"  :  "+datos.getIp());
                    mostrarRemotos();
                    intentos++;
                }
            }                
            if(datos!=null)
            {
                //System.out.println("Id en el mensaje a enviar: "+datos.getId()+"\nIp: "+datos.getIp());
                enviarMensaje(datos.getId(), datos.getIp(), origen);
            }
            else
            {
                Proceso p = dameProcesoLocal(origen);
                if(p!=null)
                {
                    //empaquetarEntero(mensaje, destino, );
                    //empaquetarEntero(mensaje, origen, 4);
                    mensaje[7]=(byte) origen;
                    mensaje[8]=-33;
                    //System.out.println("No se encontro servidor");
                    imprimeln("No se encontro ningun servidor para atender la solicitud");
                    try 
                    {
                        DatagramSocket se = dameSocketEmision();
                        DatagramPacket dpNo = new DatagramPacket(mensaje, mensaje.length, InetAddress.getByName("127.0.0.1"), damePuertoRecepcion());
                        se.send(dpNo);
                    } catch (UnknownHostException ex) {
                        //Impresion de Exception
                        Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        //Impresion de Exception
                        Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        }   
            
        public DatosServidor buscarServidorRemoto(int numServicio)
        {
            for(int i=0;i<procRemotos.size();i++)
            {
                DatosServidor datos=procRemotos.get(i);
                if(datos.getDestino()==numServicio)
                {
                    return datos;
                }
            }
            return null;
        }
            
        public void enviarLSA(int destino)
        {
            byte[] lsa = new byte[1024];
            lsa[8]=-123; //numero para distinguir el lsa
            empaquetarEntero(lsa, 9, destino);
            try 
            {
                DatagramPacket dp = new DatagramPacket(lsa, lsa.length, InetAddress.getByName("localhost"), damePuertoRecepcion());
                DatagramSocket socketEmision=dameSocketEmision();                            
                socketEmision.send(dp);
                imprimeln("Enviando paquete de localizacion de servidores con numero de servicio "+destino);
                Thread.sleep(5000);
            } 
            catch (UnknownHostException ex) 
            {
                Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
            } 
            catch (IOException | InterruptedException ex) 
            {
                Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
            
        public void enviarMensaje(int id, String ip, int origen)
        {
            
            byte[] aux = new byte[nombreArchivo.length()];
            aux=nombreArchivo.getBytes();
            imprimeln("Buscando proceso correspondiente al campo "+id);
            empaquetarEntero(mensaje, 0, origen);
            empaquetarEntero(mensaje, 4, id);
            
            //System.out.println("Mensaje origen:" +mensaje[3]+ " Destino: "+mensaje[7]);
            mensaje[8]=(byte) codop;
            mensaje[9]=(byte) nombreArchivo.length();
            System.arraycopy(aux, 0, mensaje, 10, mensaje[9]);  //agrega el mensaje a la solicitud del cliente
            
            
            //System.out.println("Mensaje codop: "+mensaje[8]);
            imprimeln("Completando campos de encabezado del mensaje a ser enviado");         
            try 
            {
                DatagramPacket dp= new DatagramPacket(mensaje, mensaje.length, InetAddress.getByName(ip), damePuertoRecepcion());
                DatagramSocket socketEmision=dameSocketEmision();
                imprimeln("Enviando mensaje proveniente de la red"); 
                socketEmision.send(dp);
            }
            catch (UnknownHostException ex) 
            {
                Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
    }
        
    class FSA extends Thread
    {
        private int claveDestino;
        public FSA(int claveDestino)
        {
            this.claveDestino=claveDestino;
        }
        
        public void run()
        {
            int i=0;               
            while(i<procLocales.size())
            {
                DatosServidor objServer = procLocales.get(i);
                if(objServer.getDestino()==claveDestino)
                {
                    byte[] fsa = new byte[1024];                    
                    byte[] auxip;
                    fsa[8]=(byte) -83;
                    empaquetarEntero(fsa, 9, 248);
                    auxip=objServer.getIp().getBytes();
                    fsa[13]=(byte) objServer.getIp().length();
                    System.arraycopy(auxip, 0, fsa, 14, fsa[13]); 
                    fsa[14+fsa[13]]=(byte) objServer.getId();
                
                    //System.out.println("Servidor localizado con los datos:\nIp: "+objServer.getIp()+"\nClave: "+desempaquetarEntero(fsa, 9)+"\nId: "+ objServer.getId()); 
                    
                    DatagramPacket dpFsa;
                    try 
                    {
                        dpFsa = new DatagramPacket(fsa, fsa.length, InetAddress.getByName("127.0.0.1"), damePuertoRecepcion());
                        DatagramSocket socketEmision=dameSocketEmision();
                        socketEmision.send(dpFsa);
                    } 
                    catch (UnknownHostException ex) 
                    {
                        System.out.println("error al enviar");
                    } 
                    catch (IOException ex) 
                    {
                        System.out.println("error al enviar2");
                    }
                }
                                
                i++;
            }
        }
    }
       
    public void run()
    {
        try 
        {
            Proceso pro;
            byte[] buffer = new byte[1024];
            socketRecepcion=dameSocketRecepcion();
            DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
            
            while(seguirEsperandoDatagramas())
            {
                socketRecepcion.receive(dp);           
                int tipoPaquete=buffer[8];
                System.out.println("\ntipo de paquete: "+tipoPaquete);
                switch (tipoPaquete) 
                {
                    case -123:  //llega paquete lsa                    
                        imprimeln("Llego un paquete de localizacion de servidores");
                        //System.out.println("Llego un paquete de localizacion de servidores");
                        if(!procLocales.isEmpty())
                        {
                            int claveDest=desempaquetarEntero(buffer, 9);
                            imprimeln("Buscando servidores con la clave destino: "+claveDest);
                            //System.out.println("Buscando servidores con la clave destino: "+clave);
                            FSA enviarFSA = new FSA(claveDest);
                            enviarFSA.start();
                        }
                        else
                        {
                            imprimeln("Tabla de procesos remotos vacia");
                        }   
                        break;
                    case -83: //caso para los fsa
                        DatosServidor objServidor= new DatosServidor();
                        objServidor.setDestino(desempaquetarEntero(buffer, 9));
                        objServidor.setIp(new String(buffer, 14, buffer[13]));
                        objServidor.setId(buffer[14+objServidor.getIp().length()]);
                        mostrarRemotos();
                        //System.out.println(" Destino: "+objServidor.getDestino()+"\nIp: "+objServidor.getIp()+"\nId: "+objServidor.getId());
                        if(procRemotos.add(objServidor))
                            imprimeln("Servidor agregado a lista de procesos remotos");
                        break;
                    default:
                        int origen=desempaquetarEntero(buffer, 0);
                        int destino=desempaquetarEntero(buffer, 4);
                        
                        String ip=dp.getAddress().getHostAddress();
                        pro=dameProcesoLocal(destino);
                        imprimeln("Buscando en listas locales el par "+ip+","+origen+" solicitando a "+destino+" de la llamada a send");
                        if(pro!=null)
                        {
                            if(buffer[1023]==-1)
                            {
                                int idEliminar=buffer[3];
                                int codop=buffer[8];
                                String nombreArchivo= new String(buffer, 10, buffer[9]);
                    
                                //int idorigen = buffer[7];
                                //System.out.println("id a eliminar:" +idEliminar);
                                //System.out.println("id origen: "+idorigen);
                                //System.out.println("codop: "+buffer[8]);
                                
                                eliminarServidorRemoto(idEliminar);
                                buffer[1023]=0;
                                buffer[3]=buffer[7];
                                buffer[7]=0;
                                //System.out.println("buffer en la posicion 3 es: "+buffer[3]);
                                //System.out.println("ultima posicion del mensaje es: "+buffer[1023]);
                                LSA hlsa = new LSA(248, buffer, codop, nombreArchivo);//agregue codop
                                hlsa.start();
                                //System.out.println("codop: "+buffer[8]);
                                
                            }
                            else
                            {
                                //System.out.println("si esta en la tabla");
                                Par datos=new Par();
                                datos.setId(origen);
                                datos.setIp(ip);

                                if(htRecepcion.containsKey(destino))
                                {
                                    imprimeln("Copiando el mensaje hacia el espacio del proceso");
                                    System.arraycopy(buffer, 0, htRecepcion.get(destino), 0, 1024);
                                    htEmision.put(origen,datos);//tenia destino
                                    htRecepcion.remove(destino);
                                    reanudarProceso(pro);
                                }
                                else
                                {
                                    System.out.println("Entrando a buzones");
                                    // ************************************ BUZONES ***********************************
                                    // ************************************ Try Again *********************************
                                    
                                    byte[]  bytes_origen  = new byte[4], 
                                            bytes_destino = new byte[4];
                                    
                                    System.arraycopy(buffer, 0, bytes_origen, 0, 4);
                                    System.arraycopy(buffer, 4, bytes_destino, 0, 4);

                                    if (buzones.containsKey(destino)) 
                                    {                            
                                        int tamanioLista = buzones.get(destino).size();

                                        if (tamanioLista > 2) 
                                        {
                                            String ta = "-1: El buzon del servidor esta lleno, intente de nuevo";  
                                            System.out.println("Error " + ta);
                                        
                                            imprimeln("El buzon del servidor esta lleno");
                                            envioError(ta, ip, bytes_origen, bytes_destino );
                                        }
                                        else
                                        {
                                            htEmision.put(origen,datos);//tenia destino

                                            // Reemplazar por tabla de emision Nueva
                                            //if ( tablaEmision.get(origen) == null ) 
                                            //{
                                            //    tablaEmision.put(origen, new PairIDAndIP(origen, ip));
                                            //}
                                            // Reemplazar por tabla de emision Nueva
                                            System.out.println("Entro al buzon");
                                            
                                            byte[] copia_solicitud = new byte[1024];
                                            System.arraycopy(buffer, 0, copia_solicitud, 0, buffer.length);
                                            buzones.get(destino).offer(copia_solicitud);
                                        }
                                    }
                                    // ************************************ FIN BUZONES ***********************************
                                }
                            }
                        }
                        else//En caso de un AU
                        {
                            imprimeln("Se registro un Address Unknown");
                            DatagramSocket se=dameSocketEmision();
                            DatagramPacket dpAu;
                            buffer[3] = (byte) destino;
                            buffer[7] = (byte) origen;
                            buffer[1023] = -1;
                            dpAu= new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), damePuertoRecepcion());
                            imprimeln("Enviando paquete Au");
                            se.send(dpAu);
                        }
                        break;
                }
            }
        } 
        catch (SocketException ex) 
        {
            Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MicroNucleo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public void eliminarServidorRemoto(int id)
    {
        for(int i=0;i<procRemotos.size();i++)
        {
            DatosServidor datos=procRemotos.get(i);
            if(datos.getId()==id)
            {
                procRemotos.remove(i);
                imprimeln("Servidor eliminado de la tabla de procesos remotos");
                break;
            }
        }
    }
    
    public void mostrarRemotos()
    {
        
        for(int i=0; i<procRemotos.size();i++)
        {
            DatosServidor datos = procRemotos.get(i);
            System.out.println("Destino: "+datos.getDestino()+"\tIp: "+datos.getIp()+"\tID: "+datos.getId());
        }
    }
    
    public void mostrarLocales()
    {
        
        for(int i=0; i<procLocales.size();i++)
        {
            DatosServidor datos = procLocales.get(i);
            System.out.println("Destino: "+datos.getDestino()+"\tIp: "+datos.getIp()+"\tID: "+datos.getId());
        }
    }
        
    class Par
    {
        private String ip;
        private int id;
        
        public String getIp()
        {
            return this.ip;
        }
        
        public int getId()
        {
            return this.id;
        }
        
        public void setIp(String ip)
        {
            this.ip=ip;
        }
        
        public void setId(int id)
        {
            this.id=id;
        }
    }
        
        
    protected void receiveVerdadero(int addr,byte[] message){
        //receiveFalso(addr,message);
        //el siguiente aplica para la practica #2
        imprimeln("Recibido mensaje proveniente de la red");
        System.out.println("Añadiendo proceso " + addr);
        htRecepcion.put(addr, message);
        suspenderProceso();
	}

	/**
	 * Para el(la) encargad@ de direccionamiento por servidor de nombres en pr�ctica 5  
	 */
	protected void sendVerdadero(String dest,byte[] message){
	}

	/**
	 * Para el(la) encargad@ de primitivas sin bloqueo en pr�ctica 5
	 */
	protected void sendNBVerdadero(int dest,byte[] message){
	}

	/**
	 * Para el(la) encargad@ de primitivas sin bloqueo en pr�ctica 5
	 */
	protected void receiveNBVerdadero(int addr,byte[] message){
	}
        
    //mover a una clase utileria despues
    public void empaquetarEntero(byte[] mensaje, int posicion, int paquete)
    {
        mensaje[posicion+3]=(byte) paquete;
        mensaje[posicion+2]=(byte) (paquete>>8);
        mensaje[posicion+1]=(byte) (paquete>>16);
        mensaje[posicion]=(byte) (paquete>>24);
    }
    
    public int desempaquetarEntero(byte[] mensaje, int posicion)
    {
        long entero = 0x00000000;            
        entero = entero|blanquear(mensaje[posicion]);
        entero = entero<<8;
        entero = entero|blanquear(mensaje[posicion+1]);
        entero = entero<<8;
        entero = entero|blanquear(mensaje[posicion+2]);
        entero = entero<<8;
        entero = entero|blanquear(mensaje[posicion+3]);        
        return (int) entero;            
    }
    
    public int blanquear(short resultado)
    {
        short blanqueador= 0x00ff;
        resultado=(short) (resultado&blanqueador);
        return resultado;
    }
        
}


/* Lo siguiente es reemplazable en la practica #2,
* sin esto, en practica #1, segun el JRE, puede incrementar el uso de CPU
*/ 

/*
    try{
        sleep(60000);
    }catch(InterruptedException e){
        System.out.println("InterruptedException");
    }*/

    /*byte[] au= new byte[255];
    au[3]=(byte) origen;
    au[7]=(byte) destino;
    au[8]=-1;
    //aux=cadena.getBytes();
    //System.arraycopy(aux, 0, au, 9, au[8]);

    DatagramPacket dpAu= new DatagramPacket(au, au.length, InetAddress.getByName(ip), damePuertoRecepcion());
    DatagramSocket socketEmision=dameSocketEmision();

    imprimeln("Proceso destinatario no encontrado segun campo "+destino+" del mensaje recibido");
    //pro=dameProcesoLocal(origen);
    socketEmision.send(dpAu);
    //reanudarProceso(pro);
*/
