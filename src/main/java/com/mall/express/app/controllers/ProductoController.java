package com.mall.express.app.controllers;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mall.express.app.models.entity.Producto;
import com.mall.express.app.models.service.IProductoService;
import com.mall.express.app.util.paginator.PageRender;
import com.mall.express.app.models.service.IUploadFileService;

@SuppressWarnings("serial")
@Controller
@SessionAttributes("cliente")
public class ProductoController implements Serializable {

	protected final Log logger = LogFactory.getLog(this.getClass());
	private List<Producto> miCarrito = new ArrayList<Producto>();
	private Producto prod;
	private Boolean creado;

	private Long total;

	@Autowired
	private IProductoService productoService;

	@Autowired
	private IUploadFileService uploadFileService;

	@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	@RequestMapping(value = { "/listar", "/" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication) {
		if (authentication != null) {
			logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			logger.info("Usuario autenticado: ".concat(auth.getName()));
		}
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Producto> productos = productoService.listaProductosPaginacion(pageRequest);
		PageRender<Producto> pageRender = new PageRender<>("/listar", productos);
		model.addAttribute("titulo", "Lista de Productos");
		model.addAttribute("productos", productos);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Producto producto = productoService.buscarProductoXId(id);
		if (producto == null) {
			flash.addFlashAttribute("error", "El producto no existe en la base de datos");
			return "redirect:/listar";
		}
		model.put("producto", producto);
		model.put("titulo", "Detalle Producto: " + producto.getNombre());
		return "ver";
	}

	@RequestMapping(value = "/agregarProducto")
	public String crear(Map<String, Object> model) {
		Producto producto = new Producto();
		creado = true;
		model.put("producto", producto);
		model.put("titulo", "Crear Producto");
		return "agregarProducto";
	}

	@RequestMapping(value = "/agregarProducto", method = RequestMethod.POST)
	public String guardar(@Valid Producto producto, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
		if (!creado) {
			prod.setCantidad(1);
			prod.setCategoria(producto.getCategoria());
			prod.setDescripcion(producto.getDescripcion());
			prod.setNombre(producto.getNombre());
			prod.setPrecio(producto.getPrecio());
			producto = prod;
		}
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Agregar Producto");
			return "agregarProducto";
		}
		if (!foto.isEmpty()) {
			if (producto.getId() != null && producto.getId() > 0 && producto.getUrl() != null
					&& producto.getUrl().length() > 0) {
				uploadFileService.delete(producto.getUrl());
			}
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFileService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			producto.setUrl(uniqueFilename);
		}
		String mensaje = (producto.getId() != null) ? "editado" : "creado";
		productoService.guardarProducto(producto);
		status.setComplete();
		flash.addFlashAttribute("success", "Cliente:  " + producto.getNombre() + " se ha " + mensaje + " con éxito");
		return "redirect:/listar";
	}

	@RequestMapping(value = "/agregar/{id}/{cantidad}", method = RequestMethod.POST)
	public String agregarCarrito(@PathVariable("id") Long id,
			@RequestParam(value = "cantidad", defaultValue = "1") Integer cantidad, RedirectAttributes flash) {
		Producto producto = productoService.buscarProductoXId(id);
		producto.setCantidad(cantidad);
		producto.setTotal(producto.getPrecio() * cantidad);
		System.out.println(cantidad);
		miCarrito.add(producto);
		flash.addFlashAttribute("info", "Has agregado: " + cantidad + " " + producto.getNombre() + " a tu carrito ");
		return "redirect:/listar";
	}

	@RequestMapping(value = "/eliminarCarrito/{id}")
	public String eliminarCarrito(@PathVariable("id") Long id, RedirectAttributes flash) {
		Producto producto = new Producto();
		for (Producto prod : miCarrito) {
			if (prod.getId() == id) {
				producto = prod;
				break;
			}
		}
		miCarrito.remove(producto);
		if (producto.getNombre() != null) {
			flash.addFlashAttribute("info", "Has eliminado: " + producto.getNombre() + " de tu carrito ");
		} else {
			flash.addFlashAttribute("info", "Este producto no está agregado en tu carrito ");
		}
		return "redirect:/listar";
	}

	@RequestMapping(value = "/listaCarrito")
	public String listaCarrito(Model model) {
		total = (long) 0d;
		for (Producto producto : miCarrito) {
			total += producto.getTotal();
		}
		model.addAttribute("titulo", "Mi Carrito");
		model.addAttribute("miCarrito", miCarrito);
		model.addAttribute("total", total);
		return "listaCarrito";
	}

//	@RequestMapping(value = "/generarPdf")
//	public String generarPdf() throws Exception {
//		return "listaCarrito";
//	}

	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			Producto producto = productoService.buscarProductoXId(id);
			productoService.eliminarProducto(id);
			flash.addFlashAttribute("success", "Producto eliminado con éxito!");
			if (uploadFileService.delete(producto.getUrl())) {
				flash.addFlashAttribute("info", "Foto " + producto.getUrl() + " eliminada con exito!");
			}
		}
		return "redirect:/listar";
	}

	@RequestMapping(value = "/editar/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Producto producto = null;
		if (id > 0) {
			producto = productoService.buscarProductoXId(id);
			prod = producto;
			creado = false;
			if (producto == null) {
				flash.addFlashAttribute("error", "El ID del producto no existe en la BBDD!");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
			return "redirect:/listar";
		}
		model.put("producto", producto);
		model.put("titulo", "Editar Producto");
		return "agregarProducto";
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

}
