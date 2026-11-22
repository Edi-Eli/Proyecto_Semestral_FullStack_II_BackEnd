package com.ferreteria.maestros.contacto.service;

import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.contacto.dto.CrearMensajeRequest;
import com.ferreteria.maestros.contacto.dto.MensajeContactoDTO;
import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import com.ferreteria.maestros.contacto.entity.MensajeContacto;
import com.ferreteria.maestros.contacto.mapper.MensajeContactoMapper;
import com.ferreteria.maestros.contacto.repository.MensajeContactoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del módulo contacto: registro de mensajes (formulario público) y su gestión
 * desde el panel de administración (listar, ver, marcar leído/resuelto).
 */
@Service
public class ContactoService {

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    private final MensajeContactoRepository mensajeRepository;
    private final MensajeContactoMapper mensajeMapper;

    public ContactoService(MensajeContactoRepository mensajeRepository,
                           MensajeContactoMapper mensajeMapper) {
        this.mensajeRepository = mensajeRepository;
        this.mensajeMapper = mensajeMapper;
    }

    /** Registra un mensaje nuevo (formulario público). */
    @Transactional
    public MensajeContactoDTO crear(CrearMensajeRequest request) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(request.nombre().trim());
        mensaje.setCorreo(request.correo().trim().toLowerCase(java.util.Locale.ROOT));
        mensaje.setMensaje(request.mensaje().trim());
        mensaje.setEstado(EstadoMensaje.NUEVO);

        MensajeContacto guardado = mensajeRepository.save(mensaje);
        log.info("Mensaje de contacto recibido de '{}'", guardado.getCorreo());
        return mensajeMapper.aDTO(guardado);
    }

    /** Lista los mensajes, opcionalmente filtrando por estado (más recientes primero). */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listar(EstadoMensaje estado) {
        List<MensajeContacto> mensajes = (estado == null)
                ? mensajeRepository.findAllByOrderByFechaCreacionDesc()
                : mensajeRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return mensajeMapper.aDTOs(mensajes);
    }

    /** Detalle de un mensaje. */
    @Transactional(readOnly = true)
    public MensajeContactoDTO obtener(Long id) {
        return mensajeMapper.aDTO(buscarEntidad(id));
    }

    /** Marca un mensaje como LEIDO o RESUELTO. */
    @Transactional
    public MensajeContactoDTO cambiarEstado(Long id, EstadoMensaje estado) {
        MensajeContacto mensaje = buscarEntidad(id);
        mensaje.setEstado(estado);
        MensajeContacto guardado = mensajeRepository.save(mensaje);
        log.info("Mensaje {} marcado como {}", guardado.getId(), estado);
        return mensajeMapper.aDTO(guardado);
    }

    private MensajeContacto buscarEntidad(Long id) {
        return mensajeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un mensaje de contacto con id " + id + "."));
    }
}
