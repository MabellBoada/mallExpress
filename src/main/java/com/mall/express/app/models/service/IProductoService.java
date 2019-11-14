package com.mall.express.app.models.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mall.express.app.models.entity.Producto;

public interface IProductoService {

	public Page<Producto> listaProductosPaginacion(Pageable pageable);

	// public Page<Producto> listaProductosPorCategoria(String categoria, Pageable
	// pageable);

	public Producto buscarProductoXId(Long id);

	public void guardarProducto(Producto producto);

	public void eliminarProducto(Long id);

}
