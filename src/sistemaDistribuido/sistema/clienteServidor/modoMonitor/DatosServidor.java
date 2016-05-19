/**Marco Antonio Montaño Martin
 * D04
 * Practica 05
 * Ultima modificacion: 17/04/2016
 */

package sistema.clienteServidor.modoMonitor;

/**
 *
 * @author Antonio
 */
public class DatosServidor {
    
    private int destino;
            private int id;
            private String ip;
            
            public int getDestino()
            {
                return this.destino;
            }
            
            public int getId()
            {
                return this.id;
            }
            
            public String getIp()
            {
                return this.ip;
            }
            
            public void setDestino(int destino)
            {
                this.destino=destino;
            }
            
            public void setId(int id)
            {
                this.id=id;
            }
            
            public void setIp(String ip)
            {
                this.ip=ip;
            }
    
}
