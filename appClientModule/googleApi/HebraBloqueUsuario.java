package googleApi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class HebraBloqueUsuario extends Thread{
	
	private String palabras;
	private List<User> usuarios = new ArrayList<User>();
	
	public String getPalabras() {
		return palabras;
	}
	public void setPalabras(String palabras) {
		this.palabras = palabras;
	}
	public List<User> getUsuarios() {
		return usuarios;
	}
	public void setUsuarios(List<User> usuarios) {
		this.usuarios = usuarios;
	}
	
	
	
	public HebraBloqueUsuario(){
		
	}
	public HebraBloqueUsuario(List<User> usuarios,String palabras){
		this.usuarios=usuarios;
		this.palabras=palabras;
	}
	
	@Override
	public void run() {
		Iterator<User> it= usuarios.iterator();
		User nom =new User();
		
		while(it.hasNext()){
			nom=it.next();
			HebraPorUsuario u = new HebraPorUsuario(nom,palabras);
			u.start();
			
		}
	}
	

}
