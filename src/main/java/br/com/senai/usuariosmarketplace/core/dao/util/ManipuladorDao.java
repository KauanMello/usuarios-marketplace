package br.com.senai.usuariosmarketplace.core.dao.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ManipuladorDao {

	public static Properties getProp() throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream("src/main/resources/application.properties");
		props.load(file);
		return props;
	}
	
	private static Properties prop;

	public static Properties pegarProp() {
		try {
			return prop = getProp();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	
	public static String getDriver() {
		return pegarProp().getProperty("database-jdbc-driver");
	}
	
	public static String getUrl() {
		return pegarProp().getProperty("database-url");
	}
	
	public static String getUsername() {
		return pegarProp().getProperty("database-user");
	}
	
	public static String getPassword() {
		return pegarProp().getProperty("database-password");
	}
	
}
