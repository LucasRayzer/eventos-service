package service.eventos.repository;

import service.eventos.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.eventos.model.StatusEvento;

import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    Page<Evento> findByOrganizerId(UUID organizerId, Pageable pageable);

    Page<Evento> findByParticipanteIdContains(UUID participanteId, Pageable pageable);

    Page<Evento> findByStatus(StatusEvento status, Pageable pageable);
}