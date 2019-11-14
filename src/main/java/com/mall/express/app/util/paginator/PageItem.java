package com.mall.express.app.util.paginator;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PageItem implements Serializable{

	private int numero;
	private boolean actual;
	
	public PageItem(int numero, boolean actual) {
		this.numero = numero;
		this.actual = actual;
	}

	public int getNumero() {
		return numero;
	}

	public boolean isActual() {
		return actual;
	}
	
}
