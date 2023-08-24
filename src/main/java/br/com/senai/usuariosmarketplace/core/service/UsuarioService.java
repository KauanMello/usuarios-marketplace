package br.com.senai.usuariosmarketplace.core.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import br.com.senai.usuariosmarketplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmarketplace.core.dao.FactoryDao;
import br.com.senai.usuariosmarketplace.core.domain.Usuario;

public class UsuarioService implements UsuarioServiceInterface{
	
	private DaoUsuario dao;

	private static final String CARACTERES_PERMITIDOS = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";		       

	
	public UsuarioService() {
		this.dao = FactoryDao.getInstance().getDaoPostgresUsuario();
	}
	
	
	@Override
	public Usuario criarNovo(String nome, String senha) {
		validar(nome, senha);
		String login = gerarLoginPor(nome);
		dao.inserir(new Usuario(login, nome, gerarHashDa(senha)));
		return dao.buscarPor(login);
	}

	@Override
	public Usuario atualizarNomeESenha(String login, String nome, String senhaAntiga, String senhaNova) {
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(login), "O login é obrigatório");
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(senhaAntiga), "A senha é obrigatória");
		
		validar(nome, senhaNova);
		
		Usuario usuarioSalvo = buscarUsuarioPor(login);

		Preconditions.checkNotNull(usuarioSalvo, "Não foi encontrado usuário vinculado ao login informado");
	
		boolean isSenhaValida = gerarHashDa(senhaAntiga).equals(usuarioSalvo.getSenha());
		
		Preconditions.checkArgument(isSenhaValida, "A senha antiga não confere");
		
		Preconditions.checkArgument(!senhaAntiga.equals(senhaNova), "A senha nova não pode ser igual a antiga");
		
		Usuario usuarioAlterado = new Usuario(login, nome, gerarHashDa(senhaNova));

		dao.alterar(usuarioAlterado);
		
		notificarAlteracaoDeSenhaNo(login);
		
		return buscarUsuarioPor(login);
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
	        int comprimentoSenha = 16;
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
	
	@SuppressWarnings("deprecation")
	public void validar(String senha) {
		boolean isSenhaValida = !Strings.isNullOrEmpty(senha)
					&& senha.length() >= 6
					&& senha.length() <= 15;
	
		Preconditions.checkArgument(isSenhaValida, 
				"Senha é obrigatória e deve conter entre 6 e 15 caracteres");
		
		boolean isContemLetra = CharMatcher.inRange('a', 'z').countIn(senha.toLowerCase()) > 0;
		boolean isContemNumero = CharMatcher.inRange('0', '9').countIn(senha) > 0;
		boolean isContemEspaco = !CharMatcher.javaLetterOrDigit().matchesAllOf(senha);
		
		Preconditions.checkArgument(!isContemEspaco && isContemLetra && isContemNumero, "A senha deve ter letras e numeros");;

		if (!isContemLetra || !isContemNumero || isContemEspaco) {
			throw new IllegalArgumentException("A senha deve conter letras e números");
		}
	}
	
	public void validar(String nome, String senha) {
		List<String> partesDoNome = fracionar(nome);
		boolean isNomeCompleto = partesDoNome.size() > 1;
		boolean isNomeValido = !Strings.isNullOrEmpty(nome) && isNomeCompleto
				&& nome.length() >= 5
				&& nome.length() <= 120;
				
		Preconditions.checkArgument(isNomeValido, "O nome é obrigatório e deve conter sobrenome e deve estar entre 5 e 120 caracteres");
		validar(senha);
	}
	
	
	private String gerarLoginPor(String nome) {
		List<String> partesDoNome = fracionar(removerAcendoDo(nome));
		String loginGerado = null;
		if (!partesDoNome.isEmpty()) {
			Usuario usuarioEncontrado = null;
			for (int i = 1; i < partesDoNome.size(); i++) {
					loginGerado = partesDoNome.get(0) + "." + partesDoNome.get(i);
					usuarioEncontrado = buscarUsuarioPor(loginGerado);
					if (usuarioEncontrado == null) {
						return loginGerado;
					}
			}
			int proximoSequencia = 1;
			String loginDisponivel = null;
			while (usuarioEncontrado != null) {
				loginDisponivel = loginGerado + proximoSequencia;
				proximoSequencia++;
				usuarioEncontrado = buscarUsuarioPor(loginDisponivel);
			}
			loginGerado = loginDisponivel;
		}
		return loginGerado;
	}
	
	private String gerarHashDa(String senha) {
		return new DigestUtils(MessageDigestAlgorithms.SHA3_256).digestAsHex(senha);
	}
	
	private String removerAcendoDo(String nome) {
		return Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
	
	private List<String> fracionar(String nome){
		List<String> nomeFracionada = new ArrayList<>();
		
		if (nome != null && !nome.isBlank()) {
			
			String[] partesNome = nome.split(" ");
			
			for (String parte : partesNome) {
				if (partesNome[0].length() + parte.length() < 50) {
					if (isNaoContemArtigo(parte)) {
						nomeFracionada.add(parte.toLowerCase());
					}
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
				&& !parte.equalsIgnoreCase("das")
				&& !parte.equalsIgnoreCase("do");
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
