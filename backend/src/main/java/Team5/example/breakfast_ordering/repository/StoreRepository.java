package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    // 可以在這裡定義自定義查詢，例如 findByName
    Store findByEmail(String email);
}
