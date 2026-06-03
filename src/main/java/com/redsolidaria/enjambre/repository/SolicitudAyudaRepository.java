package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.SolicitudAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudAyudaRepository extends JpaRepository<SolicitudAyuda, Long> {
    List<SolicitudAyuda> findByDiscapacitado_IdOrderByCreadaEnDesc(Long discapacitadoId);
    List<SolicitudAyuda> findByVoluntarioAceptado_IdOrderByCreadaEnDesc(Long voluntarioId);
    long countByVoluntarioAceptado_IdAndEstado(Long voluntarioId, String estado);
}


