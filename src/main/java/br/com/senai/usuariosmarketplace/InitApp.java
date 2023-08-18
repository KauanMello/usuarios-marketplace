package br.com.senai.usuariosmarketplace;

import javax.swing.JOptionPane;

import br.com.senai.usuariosmarketplace.core.dao.ManagerDb;

public class InitApp {

	public static void main(String[] args) {
		ManagerDb.getInstance().getConexao();
		JOptionPane.showMessageDialog(null, "Conectou com o banco");
	}

}
