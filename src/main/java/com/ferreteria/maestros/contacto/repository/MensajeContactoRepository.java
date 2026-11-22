package com.ferreteria.maestros.contacto.repository;

import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import com.ferreteria.maestros.contacto.entity.MensajeContacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    List<MensajeContacto> findAllByOrderByFechaCreacionDesc();

    List<MensajeContacto> findByEstadoOrderByFechaCreacionDesc(EstadoMensaje estado);
}
