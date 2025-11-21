package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    // 如果你想依據 Email 找人，可以神奇地這樣寫：
    // Customer findByEmail(String email); 
    // Spring Boot 會看懂你的意思自動生成程式碼！
}