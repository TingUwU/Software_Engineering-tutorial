package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.model.User;
import Team5.example.breakfast_ordering.repository.OrderRepository;
import Team5.example.breakfast_ordering.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // 建立新訂單
    public Order createOrder(Order order) {
        // 這裡未來可以加入邏輯，例如：檢查庫存、計算總金額是否正確
        return orderRepository.save(order);
    }

    // 查詢某位顧客的訂單
    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }


    public List<Map<String, Object>> getOrdersByStore(String storeId) {
        List<Order> orders = orderRepository.findByStoreIdOrderByCreateAtDesc(storeId);
        return orders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("storeId", order.getStoreId());
            orderMap.put("customerId", order.getCustomerId());
            orderMap.put("customerPhone", order.getCustomerPhone());
            orderMap.put("orderType", order.getOrderType());
            orderMap.put("dineInDetail", order.getDineInDetail());
            orderMap.put("takeoutDetail", order.getTakeoutDetail());
            orderMap.put("items", order.getItems());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("remarks", order.getRemarks());
            orderMap.put("paymentMethod", order.getPaymentMethod());
            orderMap.put("state", order.getState());
            orderMap.put("createAt", order.getCreateAt());
            orderMap.put("updatedAt", order.getUpdatedAt());

            // 添加顧客名稱
            if (order.getCustomerId() != null) {
                Optional<User> userOpt = userRepository.findById(order.getCustomerId());
                if (userOpt.isPresent()) {
                    orderMap.put("customerNickname", userOpt.get().getNickname());
                } else {
                    orderMap.put("customerNickname", "未知顧客");
                }
            } else {
                orderMap.put("customerNickname", null);
            }

            return orderMap;
        }).collect(java.util.stream.Collectors.toList());
    }


    public Order updateOrderState(String orderId, String newState) {
        // 1. 先把訂單找出來
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在 ID: " + orderId));
        
        // 2. 更新狀態
        order.setState(newState);
        
        // 3. 存回資料庫
        return orderRepository.save(order);
    }
}

