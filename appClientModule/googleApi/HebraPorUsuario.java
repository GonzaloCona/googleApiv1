package googleApi;
//import org.apache.commons.lang.StringUtils;
import java.io.*;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.google.gson.Gson;

public class HebraPorUsuario extends Thread{
	private User nom;
	private String palabras;
	
	final static String apiKey = "AIzaSyBsm97GWF2WLuAGGcyugUMAu2c4Pm27j_I";
	final static String customSearchEngineKey = "007380653580648478702:j5liwfex5-m";
	final static String searchURL = "https://www.googleapis.com/customsearch/v1?";
	
	public User getnom() {
		return nom;
	}

	public void setnom(User nom) {
		this.nom = nom;
	}

	public String getPalabras() {
		return palabras;
	}

	public void setPalabras(String palabras) //throws UnsupportedEncodingException 
	{
		
		//byte[] byteText = palabras.getBytes(Charset.forName("UTF-8"));
		//String palabras_aux= new String(byteText , "UTF-8");
		//palabras=palabras_aux;
		
		this.palabras = palabras;
	}

	
	
	public HebraPorUsuario (){
		
	}
	
	public HebraPorUsuario (User nom, String palabras) //throws UnsupportedEncodingException 
	{
		this.nom=nom;
		
		//byte[] byteText = palabras.getBytes(Charset.forName("UTF-8"));
		//String palabras_aux= new String(byteText , "UTF-8");
		//palabras=palabras_aux;
		
		this.palabras=palabras;
	} 
	
	@Override
	public void run() {
		//Iterator<String> it= palabras.iterator();
		Connection c= dbConexion.getConnection();
		PreparedStatement st;
		ResultSet rs,rs2;
		String sql;
		
		//busco palabras de interes de usuario
		try{
			sql = ("select palabra from palabras_usuario where tipo_palabra=1 and id_usuario="+nom.getId());
			st=c.prepareStatement(sql);
			rs=st.executeQuery();
			while(rs.next()){
				palabras=palabras+ " " +rs.getString(1);
			}
		 }catch( SQLException ex){
				System.out.println(ex.getMessage());
		}
		
		
		
		
		System.out.println("Usuario en proceso:"+nom.getNombre()+ " "+ nom.getApellido()+" Palabras a buscar:"+palabras);
		 
		 int inserto=0;
		
		String url = buildSearchString(nom.getNombre()+ " "+ nom.getApellido()+" "+palabras, 1, 5);
		String result = search(url);
		//System.out.println(result);
		//String result=LeeFichero();
		
		int cont=1;
		try {
			JSONObject resultJson = new JSONObject(result);
			JSONArray arrayData = new JSONArray(resultJson.getString("items"));
			for (int i = 0; i < arrayData.length(); i++)
	        {
				inserto=inserta(arrayData.getJSONObject(i),nom.getId(),cont);
				cont++;
	        }
		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}
		
		if (inserto==1){
			System.out.println("inserto, llama push" );
			String resultp=push(nom.getKey());
			System.out.println("iresultado push:"+resultp );
			
		}
		
	}
	public static String push(String key){
		String url = "http://192.168.30.206:7010/fimi_v0/webapi/u/SPush;id="+key+";cod=1;contenido=desde%20GoogleApi";
		//String url = "http://localhost:8080/fimi_v0/webapi/u/SPush;id="+key+";cod=1;contenido=desde%20Instagram";
		String result = search2(url);
		
		return result;
	}
	public static String search2(String pUrl) {
		try {
			URL url = new URL(pUrl);
			HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
			connection2.setRequestMethod("GET");
			int responseCode = connection2.getResponseCode();
			//System.out.println("\nSending 'GET' request to URL : " + url);
			//System.out.println("Response Code : " + responseCode);
			
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(connection2.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int inserta(JSONObject jsonObj, int id,int it){
		int inserto=0;
		Connection c= dbConexion.getConnection();
		PreparedStatement st;
		String sql,comentario;
		try{
			comentario=jsonObj.getString("snippet").replace("'", "");
			//String asd=StringUtils.substring(comentario,0,250);
			comentario= comentario.substring(0, 100);
			System.out.println("Artículo n°:"+it+" comentario:"+comentario+" fuente:"+jsonObj.getString("displayLink"));
			sql = ("Insert into historial_usuario(id_usuario_red,id_red_social,comentario,tipo_comentario,fecha,id_onombre_quien_comenta,is_falso_positivo) values("+id+",4,'"+comentario+"','negativo',SYSDATE(),'"+jsonObj.getString("displayLink")+"',1 )");
			//System.out.println("QUERY: "+sql);
			st=c.prepareStatement(sql);
			st.executeUpdate();
			System.out.println("");
			System.out.println("Artículo google insertado");
			System.out.print("Fecha de Insersion: ");
			System.out.print(new Date());
			System.out.println("");
			inserto=1;
		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}catch( SQLException ex){
			
			System.out.println("");
			System.out.println("Insert Duplicado: Artículo google ya existente");
			System.out.print("Fecha de Insersion Duplicado: ");
			System.out.print(new Date());
			//System.out.println(ex.getMessage());
			System.out.println("");
		}
		return inserto;
	}
	
	

	public String LeeFichero() {
	   
	      File archivo = null;
	      FileReader fr = null;
	      BufferedReader br = null;

	      try {
	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         archivo = new File ("C:\\busqueda.txt");
	         fr = new FileReader (archivo);
	         br = new BufferedReader(fr);
	         StringBuffer buffer = new StringBuffer();
	         // Lectura del fichero
	         String linea;
	         while((linea=br.readLine())!=null){
	            //System.out.println(linea);
	         	buffer.append(linea);
	         }
	         return buffer.toString();
	      }
	      catch(Exception e){
	         e.printStackTrace();
	      }finally{
	         // En el finally cerramos el fichero, para asegurarnos
	         // que se cierra tanto si todo va bien como si salta 
	         // una excepcion.
	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	            e2.printStackTrace();
	         }
	      }
	      return null;
	   }
	
	/*************************************GOOGLE SEARCH***************************************************/
	public static String search(String pUrl) {
		try {
			URL url = new URL(pUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			
		}
		return null;
	}
	
	private static String buildSearchString(String searchString, int start, int numOfResults) {
		String toSearch = searchURL + "key=" + apiKey + "&cx=" + customSearchEngineKey + "&q=";

		// replace spaces in the search query with +
		String newSearchString = searchString.replace(" ", "%20");

		toSearch += newSearchString;

		// specify response format as json
		toSearch += "&alt=json";

		// specify starting result number
		toSearch += "&start=" + start;

		// specify the number of results you need from the starting position
		toSearch += "&num=" + numOfResults;

		//System.out.println("Seacrh URL: " + toSearch);
		return toSearch;
	}
	
	
}
