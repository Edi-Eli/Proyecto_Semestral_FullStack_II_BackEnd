package com.ferreteria.maestros.producto.repository;

import com.ferreteria.maestros.producto.entity.Producto;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<Producto> conActivo(Boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    public static Specification<Producto> conTexto(String texto) {
        String patron = "%" + texto.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron),
                cb.like(cb.lower(root.get("marca")), patron),
                cb.like(cb.lower(root.get("codigo")), patron));
    }

    public static Specification<Producto> conCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("subcategoria").get("categoria").get("id"), categoriaId);
    }

    public static Specification<Producto> conSubcategoria(Long subcategoriaId) {
        return (root, query, cb) -> cb.equal(root.get("subcategoria").get("id"), subcategoriaId);
    }

    public static Specification<Producto> conEnOferta(Boolean enOferta) {
        return (root, query, cb) -> cb.equal(root.get("enOferta"), enOferta);
    }

    public static Specification<Producto> conPrecioOfertaPositivo() {
        return (root, query, cb) -> cb.greaterThan(root.get("precioOferta"), BigDecimal.ZERO);
    }

    public static Specification<Producto> conStockCritico() {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("stock"), root.get("stockMinimo"));
    }
}
