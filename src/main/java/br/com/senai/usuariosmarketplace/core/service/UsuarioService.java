package br.com.senai.usuariosmarketplace.core.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import br.com.senai.usuariosmarketplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmarketplace.core.dao.FactoryDao;
import br.com.senai.usuariosmarketplace.core.dao.util.SendEmail;
import br.com.senai.usuariosmarketplace.core.domain.Usuario;

public class UsuarioService implements UsuarioServiceInterface{
	
	private DaoUsuario dao;
	private static final String CARACTERES_PERMITIDOS = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";		       

	
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
				notificarAlteracaoDeSenhaNo(login);
			}else {
				throw new IllegalArgumentException("Senha incorreta");
			}
		}else {
			System.out.println("Login informado é invalido ou inexistente");
		}
	}

	@Override
	public Usuario buscarUsuarioPor(String login) {
		if (login != null && !login.isBlank()) {
			return dao.buscarPor(login);
		}
		return null;
	}
	
	@Override
	public String resetarSenhaPor(String login) {
		Usuario usuarioEncontrado = buscarUsuarioPor(login);
		if (usuarioEncontrado != null) {
	        int comprimentoSenha = 6 + new Random().nextInt(16 - 6 + 1);
	        StringBuilder senha = new StringBuilder();
	        SecureRandom random = new SecureRandom();
	        
	        for (int i = 0; i < comprimentoSenha; i++) {
	            int index = random.nextInt(CARACTERES_PERMITIDOS.length());
	            senha.append(CARACTERES_PERMITIDOS.charAt(index));
	        }
	        usuarioEncontrado.setSenha(gerarHashDa(senha.toString()));
	        dao.alterar(usuarioEncontrado);
	        System.out.println("Senha nova gerada: " + senha.toString());
	        notificarSenhaResetada(senha.toString());
	        return senha.toString();
		}else {
			throw new IllegalArgumentException("Só se pode resetar uma senha de um login já existente");
		}
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
	
	private void notificarAlteracaoDeSenhaNo(String login) {
		NotificadorService.enviarEmail("Atualização de login", 
				"Ocorreu uma alteração de senha no login: " + login);
	}
	
	
	private void notificarSenhaResetada(String senhaGerada) {
		NotificadorService.enviarEmail("Senha Resetada", "Sua senha foi resetada para: <b>" 
				+  senhaGerada 
				+ "</b> Utilize essa senha alterar a nova.");
	}

}
