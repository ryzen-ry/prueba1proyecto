package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.SolicitudAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAyudaRepository extends JpaRepository<SolicitudAyuda, Long> {

    void deleteByDiscapacitado_Id(Long discapacitadoId);

    @Modifying
    @Query("UPDATE SolicitudAyuda s SET s.voluntarioAceptado = null WHERE s.voluntarioAceptado.id = :voluntarioId")
    void desasociarVoluntarioAceptado(@Param("voluntarioId") Long voluntarioId);
}

