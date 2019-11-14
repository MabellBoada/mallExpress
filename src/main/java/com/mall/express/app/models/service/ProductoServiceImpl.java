package com.mall.express.app.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mall.express.app.models.dao.IProductoDao;
import com.mall.express.app.models.entity.Producto;

@Service
public class ProductoServiceImpl implements IProductoService {

	@Autowired
	private IProductoDao productoDao;

	@Override
	public Page<Producto> listaProductosPaginacion(Pageable pageable) {
		return productoDao.findAll(pageable);
	}

//	@Override
//	public Page<Producto> listaProductosPorCategoria(String categoria, Pageable pageable) {
//		return productoDao.findAll(pageable);
//	}

	@Override
	@Transactional(readOnly = true)
	public Producto buscarProductoXId(Long id) {
		return productoDao.findById(id).orElse(null);
	}

	@Override
	public void guardarProducto(Producto producto) {
		productoDao.save(producto);

	}

	@Override
	public void eliminarProducto(Long id) {
		productoDao.deleteById(id);

	}
}
