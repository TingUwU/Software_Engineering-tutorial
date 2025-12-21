package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders") // 訂單的總機號碼
// @CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }


    @GetMapping("/customer/{customerId}")
    public List<Order> getCustomerOrders(@PathVariable String customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/store/{storeId}")
    public List<Map<String, Object>> getStoreOrders(@PathVariable String storeId) {
        return orderService.getOrdersByStore(storeId);
    }

    @PatchMapping("/{id}")
    public Order updateOrderState(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String newState = payload.get("state");
        return orderService.updateOrderState(id, newState);
    }
}