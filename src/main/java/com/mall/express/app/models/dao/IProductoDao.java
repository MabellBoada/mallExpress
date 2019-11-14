package com.mall.express.app.models.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.mall.express.app.models.entity.Producto;

public interface IProductoDao extends CrudRepository<Producto, Long>, PagingAndSortingRepository<Producto, Long> {
	
	@Query("SELECT p FROM Producto p WHERE p.categoria LIKE ?1")
	Page<Producto> listaProductosPorCategoria( @Param("categoria") String categoria, Pageable pageable);


}
