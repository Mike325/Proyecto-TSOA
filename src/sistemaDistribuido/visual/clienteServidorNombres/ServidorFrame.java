package sistemaDistribuido.visual.clienteServidorNombres;

import sistemaDistribuido.sistema.clienteServidor.modoUsuarioNombres.ProcesoServidor;
import sistemaDistribuido.visual.clienteServidor.MicroNucleoFrame;
import sistemaDistribuido.visual.clienteServidor.ProcesoFrame;

public class ServidorFrame extends ProcesoFrame{
  private static final long serialVersionUID=1;
  private ProcesoServidor proc;

  public ServidorFrame(MicroNucleoFrame frameNucleo){
    super(frameNucleo,"Servidor de Archivos Servidor Nombres");
    proc=new ProcesoServidor(this);
    fijarProceso(proc);
  }
}