package com.mall.express.app.view.pdf;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mall.express.app.models.entity.Producto;

@Component("listaCarrito")
public class FacturaPdfView extends AbstractPdfView {

	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		@SuppressWarnings("unchecked")
		List<Producto> productos = (List<Producto>) model.get("miCarrito");
		Long total = (Long) model.get("total");

		PdfPTable tablaTitulo = new PdfPTable(1);
		PdfPCell cell = null;
		cell = new PdfPCell(new Phrase("FACTURA SMALL EXPRESS"));
		cell.setBackgroundColor(new Color(200, 240, 255));
		cell.setPadding(8f);
		tablaTitulo.addCell(cell);
		tablaTitulo.setSpacingAfter(20);
		document.add(tablaTitulo);

		PdfPTable tabla = new PdfPTable(4);
		tabla.setWidths(new float[] { 3.5f, 1, 1, 1 });
		tabla.setSpacingAfter(20);
		tabla.addCell("Producto");
		tabla.addCell("Precio");
		tabla.addCell("Cantidad");
		tabla.addCell("Total");

		for (Producto producto : productos) {
			tabla.addCell(producto.getNombre());
			tabla.addCell("$ " + producto.getPrecio().toString());
			tabla.addCell(producto.getCantidad().toString());
			tabla.addCell("$ " + producto.getTotal().toString());
		}

		cell = new PdfPCell(new Phrase("TOTAL: "));
		cell.setBackgroundColor(new Color(200, 240, 255));
		cell.setColspan(3);
		cell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
		tabla.addCell(cell);
		cell = new PdfPCell(new Phrase("$ " + total.toString()));
		cell.setBackgroundColor(new Color(200, 240, 255));
		tabla.addCell(cell);
		document.add(tabla);
	}

}
