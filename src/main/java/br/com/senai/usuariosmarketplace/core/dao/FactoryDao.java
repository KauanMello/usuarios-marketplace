package br.com.senai.usuariosmarketplace.core.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import br.com.senai.usuariosmarketplace.core.dao.postgres.DaoPostgresUsuario;

@Service
public class FactoryDao {

	@Bean
	public DaoUsuario getDaoPostgresUsuario() {
		return new DaoPostgresUsuario();
	}

}
