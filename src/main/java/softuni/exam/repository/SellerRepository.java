package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entities.Rating;
import softuni.exam.models.entities.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Seller findFirstByFirstNameAndLastNameAndRatingAndEmail(String firstName,
                                                            String lastName,
                                                            Rating rating,
                                                            String email);

    Seller findFirstById(Long id);
}
