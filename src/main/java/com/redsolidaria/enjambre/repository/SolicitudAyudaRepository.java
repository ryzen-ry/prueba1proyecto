package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.SolicitudAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAyudaRepository extends JpaRepository<SolicitudAyuda, Long> {
}

