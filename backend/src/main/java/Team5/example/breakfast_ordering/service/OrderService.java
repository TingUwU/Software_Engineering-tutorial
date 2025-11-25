package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // 建立新訂單
    public Order createOrder(Order order) {
        // 這裡未來可以加入邏輯，例如：檢查庫存、計算總金額是否正確
        return orderRepository.save(order);
    }

    // 查詢某位顧客的訂單
    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}