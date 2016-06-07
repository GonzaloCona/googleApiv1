package googleApi;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomGoogleSearch {
	
	/*************************************MAIN***************************************************/
	public static void main(String[] args) throws Exception {
		
		
			 final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			    service.scheduleWithFixedDelay(new Runnable()
			      {
			        @Override
			        public void run()
			        {
			           System.out.println(" ************************************************* ");	
				       System.out.println(" ***********  Inicio Motor Google  ************ ");
				       System.out.println(new Date());
				       System.out.println(" ************************************************* ");
			           orquestador();
			        }
			      }, 0, 1, TimeUnit.DAYS);

	}
	
	public static void orquestador(){
		System.out.println("***************************************************************************************");
		
		int count=0;
		int numBloques=0;
		List<User> listUser=new ArrayList<User>();
		List<User> listUserIns=new ArrayList<User>();
		Map<Integer,List<User>> bloqueUsuario=new HashMap<Integer,List<User>>();
		
		//recupero lista de palabras fimi
		Connection c= dbConexion.getConnection();
		PreparedStatement st;
		ResultSet rs,rs2;
		String sql;
		String wordsFimi="";
		//busco palabras de interes de 4fimi
		try{
			sql = ("select palabra from palabras_fimi where tipo_palabra=1");
			st=c.prepareStatement(sql);
			rs=st.executeQuery();
			while(rs.next()){
				wordsFimi=wordsFimi+ " " +rs.getString(1);
			}
		 }catch( SQLException ex){
				System.out.println(ex.getMessage());
		 }
		
		
		
		try{
			st=c.prepareStatement("Select id_usuario,nom1,apell1,keyMovil from usuario");
			rs=st.executeQuery();
			while(rs.next()){
				User u=new User(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));
				listUser.add(u);
				//System.out.println("usuario: "+u.getNombre());		
	
				count++;
			}
			
			
			int i=0;
			int cant=0;
			while(i<listUser.size())
			{	
				listUserIns.add(listUser.get(i));
				if(cant==499){
					bloqueUsuario.put(numBloques, listUserIns);					
					numBloques++;
					limpiaLista(listUserIns);
					cant=-1;
				}
				cant++;
				i++;
			}
			if(cant<499){
				bloqueUsuario.put(numBloques, listUserIns);					
				//numBloques++;
			}
			
			
		}catch( SQLException ex){
			ex.getMessage();
		}
		
		while(numBloques>-1){
			HebraBloqueUsuario b=new HebraBloqueUsuario(bloqueUsuario.get(numBloques),wordsFimi);
			//System.out.println("tira a hebra por bloque de usuario, "+ numBloques +" "+ bloqueUsuario.get(numBloques));
			b.start();
			numBloques--;
		}
	}
	public static void limpiaLista(List<User> list){
		
		
		for(int i =0;i< list.size();i++){
					list.remove(i);
		}	
	}
	
	
}
