package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}