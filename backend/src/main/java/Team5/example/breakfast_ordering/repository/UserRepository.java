package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}