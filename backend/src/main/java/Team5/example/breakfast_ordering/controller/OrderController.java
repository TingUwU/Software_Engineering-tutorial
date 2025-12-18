package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders") // 訂單的總機號碼
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // API: 建立訂單 (POST /api/orders)
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    // API: 查詢我的訂單 (GET /api/orders/customer/{customerId})
    @GetMapping("/customer/{customerId}")
    public List<Order> getCustomerOrders(@PathVariable String customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    // 【新增】API: 商家查詢訂單 (GET /api/orders/store/{storeId})
    @GetMapping("/store/{storeId}")
    public List<Order> getStoreOrders(@PathVariable String storeId) {
        return orderService.getOrdersByStore(storeId);
    }

    // 【新增】API: 更新訂單狀態 (PATCH /api/orders/{id})
    // Body: { "state": "已接單" }
    @PatchMapping("/{id}")
    public Order updateOrderState(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String newState = payload.get("state");
        return orderService.updateOrderState(id, newState);
    }
}