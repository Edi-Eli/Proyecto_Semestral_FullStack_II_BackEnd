package com.ferreteria.maestros.pedido.repository;

import com.ferreteria.maestros.pedido.entity.EstadoPedido;
import com.ferreteria.maestros.pedido.entity.Pedido;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);
}