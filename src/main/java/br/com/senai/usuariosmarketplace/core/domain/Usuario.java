package br.com.senai.usuariosmarketplace.core.domain;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

public class Usuario {
	
	@Getter @Setter
	private String login;
	
	@Getter @Setter
	private String nome;
	
	@Getter @Setter
	private String senha;

	public Usuario(String login, String nome, String senha) {
		this.login = login;
		this.nome = nome;
		this.senha = senha;
	}

	@Override
	public int hashCode() {
		return Objects.hash(login);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(login, other.login);
	}
	
}
