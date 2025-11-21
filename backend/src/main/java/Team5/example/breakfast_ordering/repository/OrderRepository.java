// 1. 這裡原本少了一層 breakfast_ordering，請改成這樣：
package Team5.example.breakfast_ordering.repository;

// 2. 這裡原本也少了一層，請改成這樣：
import Team5.example.breakfast_ordering.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    // 範例：找出某個顧客的所有訂單
    // List<Order> findByCustomerId(String customerId);
}