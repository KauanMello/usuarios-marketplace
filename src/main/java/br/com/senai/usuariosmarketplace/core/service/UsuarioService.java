package br.com.senai.usuariosmarketplace.core.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import br.com.senai.usuariosmarketplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmarketplace.core.dao.FactoryDao;
import br.com.senai.usuariosmarketplace.core.domain.Usuario;

public class UsuarioService {
	
	private DaoUsuario dao;
	
	public UsuarioService() {
		this.dao = FactoryDao.getInstance().getDaoPostgresUsuario();
	}
	
	public String gerarLoginPor(String nome) {
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
	
	public String gerarHashDa(String senha) {
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
