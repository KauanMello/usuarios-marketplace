package br.com.senai.usuariosmarketplace.core.dao;

import br.com.senai.usuariosmarketplace.core.dao.postgres.DaoPostgresUsuario;

public class FactoryDao {

	private static FactoryDao instance;
	
	private FactoryDao() {}
	
	public DaoUsuario getDaoPostgresUsuario() {
		return new DaoPostgresUsuario();
	}

	public static FactoryDao getInstance() {
		if (instance == null) {
			return new FactoryDao();
		}
		return instance;
	}
	
}
