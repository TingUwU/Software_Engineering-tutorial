package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    // 1. 找出某個顧客的所有訂單 (歷史紀錄)
    List<Order> findByCustomerId(String customerId);

    // 2. 找出某個店家的所有訂單 (商家管理後台用)
    List<Order> findByStoreId(String storeId);
    
    // 3. 找出某個店家且狀態為「已送出」的訂單 (商家接單用)
    List<Order> findByStoreIdAndState(String storeId, String state);
}