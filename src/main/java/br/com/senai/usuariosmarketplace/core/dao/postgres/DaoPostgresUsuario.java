package br.com.senai.usuariosmarketplace.core.dao.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.senai.usuariosmarketplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmarketplace.core.dao.ManagerDb;
import br.com.senai.usuariosmarketplace.core.domain.Usuario;

public class DaoPostgresUsuario implements DaoUsuario{

	private final String INSERT = "INSERT INTO usuarios(login, nome, senha) VALUES(?, ?, ?)";
	
	private final String UPDATE = "UPDATE usuarios SET nome = ?, senha = ? WHERE login = ?";
	
	private final String FIND_BY_LOGIN = "SELECT u.login, u.nome, u.senha "
			+ "FROM usuarios u "
			+ "WHERE u.login = ?";
	
	private Connection connection;

	public DaoPostgresUsuario() {
		this.connection = ManagerDb.getInstance().getConexao();
	}
	
	@Override
	public void inserir(Usuario usuario) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(INSERT);
			ps.setString(1, usuario.getLogin());
			ps.setString(2, usuario.getNome());
			ps.setString(3, usuario.getSenha());
			ps.execute();
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro ao inserir usuário "
					+ "Motivo: " + e.getMessage());
		}finally {
			ManagerDb.getInstance().fechar(ps);
		}
	}

	@Override
	public void alterar(Usuario usuario) {
		PreparedStatement ps = null;
		try {
			ManagerDb.getInstance().configurarAutocommitDa(connection, false);
			ps = connection.prepareStatement(UPDATE);
			ps.setString(1, usuario.getNome());
			ps.setString(2, usuario.getSenha());
			ps.setString(3, usuario.getLogin());
			boolean isAlteracaoOk = ps.executeUpdate() == 1;
			if (isAlteracaoOk) {
				this.connection.commit();
			}else {
				this.connection.rollback();
			}
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro ao alterar o usuário "
					+ "Motivo: " + e.getMessage());
		}finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().configurarAutocommitDa(connection, true);
		}
	}

	@Override
	public Usuario buscarPor(String login) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(FIND_BY_LOGIN);
			ps.setString(1, login);
			rs = ps.executeQuery();
			if (rs.next()) {
				return extrairDo(rs);
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro ao buscar por usuário por login "
					+ "Motivo: " + e.getMessage());
		}finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().fechar(rs);
		}
	}
	
	private Usuario extrairDo(ResultSet rs) {
		try {
			String login = rs.getString("login");
			String senha = rs.getString("senha");
			String nome = rs.getString("nome");
			
			return new Usuario(login, nome, senha);
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro ao extrair usuário "
					+ "Motivo" + e.getMessage());
		}
		
	}
	
}
