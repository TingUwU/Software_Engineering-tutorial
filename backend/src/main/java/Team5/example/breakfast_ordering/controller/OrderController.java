package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders") // 訂單的總機號碼
// @CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // 用來推播通知

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
        Order updatedOrder = orderService.updateOrderState(id, newState);

        if("已完成".equals(newState)){
            String customerId = updatedOrder.getCustomerId();
            String message = "您的訂單 (編號: " + updatedOrder.getId().substring(0, 5) + ") 已經準備好，請前往取餐！";

            messagingTemplate.convertAndSend("/topic/orders/" + customerId, message);
        }

        return updatedOrder;
    }
}