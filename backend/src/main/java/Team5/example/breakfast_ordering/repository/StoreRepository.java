package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    Store findByOwnerId(String ownerId);
}
