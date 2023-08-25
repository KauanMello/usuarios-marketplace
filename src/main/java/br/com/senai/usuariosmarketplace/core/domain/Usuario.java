package br.com.senai.usuariosmarketplace.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@ToString

public class Usuario {
	
	@EqualsAndHashCode.Include
	private String login;
	
	private String nome;
	
	private String senha;
	
}
