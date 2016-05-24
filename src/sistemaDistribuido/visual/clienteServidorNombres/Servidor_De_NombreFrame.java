//Gonzalo Daniel Sanchez De Luna
//D04
//P5
package sistemaDistribuido.visual.clienteServidor;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Servidor_De_NombreFrame extends JFrame {
	private JPanel panel;
	private JTextArea servidores;
	private JTextArea mensajes;

	public Servidor_De_NombreFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 300);
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(panel);
		panel.setLayout(null);
		
		JLabel etiqueta_nombre = new JLabel("Nombre Servidor");
		etiqueta_nombre.setBounds(20, 11, 100, 14);
		panel.add(etiqueta_nombre);
		
		JLabel etiqueta_ID = new JLabel("ID");
		etiqueta_ID.setBounds(190, 11, 100, 14);
		panel.add(etiqueta_ID);
		
		JLabel etiqueta_IP = new JLabel("IP");
		etiqueta_IP.setBounds(300, 11, 100, 14);
		panel.add(etiqueta_IP);
		
		
		JScrollPane arriba = new JScrollPane();
		arriba.setBounds(20, 35, 450, 90);
		panel.add(arriba);
	
		
		servidores = new JTextArea();
		servidores.setEnabled(false);
		servidores.setEditable(false);
		arriba.setViewportView(servidores);
		
		JScrollPane abajo = new JScrollPane();
		abajo.setBounds(20, 150, 450, 90);
		panel.add(abajo);
	
		mensajes = new JTextArea();
		mensajes.setEnabled(false);
		mensajes.setEditable(false);
		abajo.setViewportView(mensajes);
	}
	
	public void actualiza_ventana(String dns){
		servidores.setText(dns);
	}
	

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Servidor_De_NombreFrame holi = new Servidor_De_NombreFrame();
					holi.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void imprimelnSDN(String mensaje) {
		mensajes.append(mensaje);
	}
}
