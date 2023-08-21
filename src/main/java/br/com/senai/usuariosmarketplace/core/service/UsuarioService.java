package br.com.senai.usuariosmarketplace.core.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import br.com.senai.usuariosmarketplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmarketplace.core.dao.FactoryDao;
import br.com.senai.usuariosmarketplace.core.domain.Usuario;

public class UsuarioService implements UsuarioServiceInterface{
	
	private DaoUsuario dao;
	
	public UsuarioService() {
		this.dao = FactoryDao.getInstance().getDaoPostgresUsuario();
	}
	
	
	@Override
	public void criarNovo(String nome, String senha) {
		Usuario usuario = new Usuario(null, nome, senha);
		validar(usuario);
		gerarLoginPor(usuario.getNome());
		dao.inserir(usuario);
	}

	@Override
	public void atualizarNomeESenha(String login, String nome, String senhaAntiga, String senhaNova) {
		Usuario usuarioEncontrado = buscarUsuarioPor(login);
		if (usuarioEncontrado != null) {
			boolean isSenhaIgual = gerarHashDa(senhaAntiga).equals(usuarioEncontrado.getSenha());
			if (isSenhaIgual) {
				usuarioEncontrado.setNome(nome);
				usuarioEncontrado.setSenha(senhaNova);
				validar(usuarioEncontrado);
				dao.alterar(usuarioEncontrado);
				System.out.println("atualizado com sucesso");
			}else {
				throw new IllegalArgumentException("Senha incorreta");
			}
		}else {
			System.out.println("Login informado é invalido ou inexistente");
		}
	}

	@Override
	public Usuario buscarUsuarioPor(String login) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String resetarSenhaPor(String login) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void validar(Usuario usuario) {
		boolean isNomeESenhaPreenchida = fracionar(usuario.getNome()).size() > 0 
				&& usuario.getSenha() != null;
		
		if (isNomeESenhaPreenchida) {
			
			boolean isNomeValido = usuario.getNome().length() > 5
					&& usuario.getNome().length() < 120;
			
			boolean isSenhaValida = usuario.getSenha() != null 
                    && usuario.getSenha().length() >= 6
                    && usuario.getSenha().length() <= 15
                    && usuario.getSenha().matches(".*\\d.*") 
                    && usuario.getSenha().matches(".*[a-zA-Z].*"); 

			if (isNomeValido) {
				if (usuario.getLogin() == null) {
					usuario.setLogin(gerarLoginPor(usuario.getNome()));
				}
			}else {
				throw new IllegalArgumentException("O nome deve conter entre 5 e 120 caracteres");
			}
			
			if (isSenhaValida) {
				usuario.setSenha(gerarHashDa(usuario.getSenha()));
			}else {
				throw new IllegalArgumentException("A senha deve conter entre 6 e 15 caracteres contendo numeros e letras");
			}
			
		}else {
			throw new IllegalArgumentException("Os paramêtros nome e senha são obrigátorios");
		}
		
	}
	
	
	private String gerarLoginPor(String nome) {
		List<String> partesDoNome = fracionar(removerAcendoDo(nome));
		String loginGerado = null;
		Usuario usuarioEncontrado = null;
		if (!partesDoNome.isEmpty()) {
			for (int i = 1; i < partesDoNome.size(); i++) {
					loginGerado = partesDoNome.get(0) + "." + partesDoNome.get(i);
					usuarioEncontrado = dao.buscarPor(loginGerado);
					if (usuarioEncontrado == null) {
						return loginGerado;
					}
			}
			int proximoSequencia = 0;
			String loginDisponivel = null;
			while (usuarioEncontrado != null) {
				loginDisponivel = loginGerado + ++proximoSequencia;
				usuarioEncontrado = dao.buscarPor(loginDisponivel);
			}
			loginGerado = loginDisponivel;
		}
		return loginGerado;
	}
	
	private String gerarHashDa(String senha) {
		return new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(senha);
	}
	
	private String removerAcendoDo(String nome) {
		return Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
	
	private List<String> fracionar(String nome){
		List<String> nomeFracionada = new ArrayList<>();
		
		if (nome != null && !nome.isBlank()) {
			
			String[] partesNome = nome.split(" ");
			
			for (String parte : partesNome) {
				if (isNaoContemArtigo(parte)) {
					nomeFracionada.add(parte.toLowerCase());
				}
			}
		}
		return nomeFracionada;
		
	}
	
	private boolean isNaoContemArtigo(String parte) {
		return	!parte.equalsIgnoreCase("de")
				&& !parte.equalsIgnoreCase("e")
				&& !parte.equalsIgnoreCase("dos")
				&& !parte.equalsIgnoreCase("da")
				&& !parte.equalsIgnoreCase("das");
	}

}
