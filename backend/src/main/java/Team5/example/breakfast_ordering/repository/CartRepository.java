package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, String>{
}
