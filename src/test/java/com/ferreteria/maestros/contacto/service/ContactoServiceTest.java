// Contacto (ContactoService) — realizado por: Edi (Fase 8)
package com.ferreteria.maestros.contacto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.contacto.dto.CrearMensajeRequest;
import com.ferreteria.maestros.contacto.dto.MensajeContactoDTO;
import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import com.ferreteria.maestros.contacto.entity.MensajeContacto;
import com.ferreteria.maestros.contacto.mapper.MensajeContactoMapper;
import com.ferreteria.maestros.contacto.repository.MensajeContactoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private MensajeContactoRepository mensajeRepository;

    @Mock
    private MensajeContactoMapper mensajeMapper;

    @InjectMocks
    private ContactoService contactoService;

    private MensajeContacto entidad(Long id, EstadoMensaje estado) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setId(id);
        mensaje.setNombre("Ana");
        mensaje.setCorreo("ana@gmail.com");
        mensaje.setMensaje("hola");
        mensaje.setEstado(estado);
        mensaje.setFechaCreacion(LocalDateTime.now());
        return mensaje;
    }

    private MensajeContactoDTO aDto(MensajeContacto m) {
        return new MensajeContactoDTO(m.getId(), m.getNombre(), m.getCorreo(), m.getMensaje(), m.getEstado(), m.getFechaCreacion());
    }

    @Test
    void crearRegistraElMensajeComoNuevoYNormalizaElCorreo() {
        when(mensajeRepository.save(any(MensajeContacto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mensajeMapper.aDTO(any(MensajeContacto.class))).thenAnswer(inv -> aDto(inv.getArgument(0)));

        MensajeContactoDTO dto = contactoService.crear(
                new CrearMensajeRequest("Ana", "ANA@Gmail.com", "hola"));

        assertEquals(EstadoMensaje.NUEVO, dto.estado());
        assertEquals("ana@gmail.com", dto.correo());
        assertEquals("hola", dto.mensaje());
    }

    @Test
    void obtenerInexistenteLanzaRecursoNoEncontrado() {
        when(mensajeRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> contactoService.obtener(9L));
    }

    @Test
    void cambiarEstadoActualizaElMensaje() {
        MensajeContacto mensaje = entidad(3L, EstadoMensaje.NUEVO);
        when(mensajeRepository.findById(3L)).thenReturn(Optional.of(mensaje));
        when(mensajeRepository.save(any(MensajeContacto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mensajeMapper.aDTO(any(MensajeContacto.class))).thenAnswer(inv -> aDto(inv.getArgument(0)));

        MensajeContactoDTO dto = contactoService.cambiarEstado(3L, EstadoMensaje.RESUELTO);

        assertEquals(EstadoMensaje.RESUELTO, dto.estado());
        assertEquals(EstadoMensaje.RESUELTO, mensaje.getEstado());
        verify(mensajeRepository).save(mensaje);
    }
}
