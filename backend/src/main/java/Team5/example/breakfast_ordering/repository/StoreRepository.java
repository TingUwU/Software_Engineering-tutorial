// 1. 這裡原本少了一層 breakfast_ordering，請改成這樣：
package Team5.example.breakfast_ordering.repository;

// 2. 這裡原本也少了一層，請改成這樣：
import Team5.example.breakfast_ordering.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
// <Store, String> 的意思是：我要管理 Store 這個類別，它的 ID 是 String 型態
public interface StoreRepository extends MongoRepository<Store, String> {
    // 裡面什麼都不用寫！
    // Spring Boot 已經自動幫你做好了 save(), findAll(), findById(), delete() 等方法
}
