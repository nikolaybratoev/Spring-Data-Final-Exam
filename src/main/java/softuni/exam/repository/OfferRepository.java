package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softuni.exam.models.entities.Offer;

import java.time.LocalDateTime;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Offer findFirstByDescriptionAndAddedOn(String description, LocalDateTime localDateTime);
}
