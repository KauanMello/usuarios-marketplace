package br.com.senai.usuariosmarketplace.core.service;

import br.com.senai.usuariosmarketplace.core.dao.util.SendEmail;

public class NotificadorService {
	
	private NotificadorService() {}
	
	public static void enviarEmail(String assunto, String mensagem) {
		if (SendEmail.isEmailAtivado()) {
			SendEmail.enviarEmail(assunto , mensagem);
		}else {
			System.out.println("Email está desativado");
		}
	}

}
