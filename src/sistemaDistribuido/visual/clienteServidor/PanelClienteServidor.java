package sistemaDistribuido.visual.clienteServidor;

import java.awt.Panel;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

public class PanelClienteServidor extends Panel{
  private static final long serialVersionUID=1;
  private Button botonClienteRala,botonServidorRala, botonClienteNombres, botonClienteBuzones, botonServidorNombres, botonServidorBuzones;

  public PanelClienteServidor(){
     setLayout(new GridLayout(3,2));
     botonClienteRala=new Button("Cliente Rala");
     botonServidorRala=new Button("Servidor Rala");
     botonClienteNombres=new Button("Cliente Nombres");
     botonServidorNombres=new Button("Servidor Nombres");
     botonClienteBuzones=new Button("Cliente Buzones");
     botonServidorBuzones=new Button("Servidor Buzones");
     
     add(botonClienteRala);
     add(botonServidorRala);
     add(botonClienteNombres);
     add(botonServidorNombres);
     add(botonClienteBuzones);
     add(botonServidorBuzones);
  }
  
  public Button dameBotonClienteRala(){
    return botonClienteRala;
  }
  
  public Button dameBotonClienteNombres(){
    return botonClienteNombres;
  }
  
  public Button dameBotonClienteBuzones(){
    return botonClienteBuzones;
  }
  
  public Button dameBotonServidorRala(){
    return botonServidorRala;
  }
  
  public Button dameBotonServidorBuzones(){
    return botonServidorBuzones;
  }
  
  public Button dameBotonServidorNombres(){
    return botonServidorNombres;
  }
  
  public void agregarActionListener(ActionListener al){
    botonClienteRala.addActionListener(al);
    botonServidorRala.addActionListener(al);
    botonClienteNombres.addActionListener(al);
    botonServidorNombres.addActionListener(al);
    botonClienteBuzones.addActionListener(al);
    botonServidorBuzones.addActionListener(al);
  }
}
