package sistemaDistribuido.sistema.rpc.modoUsuario;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.ParMaquinaProceso;

public class atributos_asa implements ParMaquinaProceso{
        private int id;
        private String hostAddress;
        
        public atributos_asa(String hostAddress, int id){
                this.id=id;
                this.hostAddress=hostAddress;
        }


		@Override
		public String dameIP() {
			// TODO Auto-generated method stub
			return hostAddress;
		}
		@Override
		public int dameID() {
			// TODO Auto-generated method stub
			return id;
		}
}