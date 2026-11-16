package com.ferreteria.maestros.pedido.repository;

import com.ferreteria.maestros.pedido.entity.PedidoItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    List<PedidoItem> findByProductoId(Long productoId);
}