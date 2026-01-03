package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Order;
import Team5.example.breakfast_ordering.service.OrderService;
import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.StoreService;
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
    private StoreService storeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // 用來推播通知

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        Order newOrder = orderService.createOrder(order);

        try {
            String storeId = newOrder.getStoreId();
            Store store = storeService.getStoreById(storeId);
            String ownerId = store.getOwnerId();

            if (ownerId != null) {
                String message = "🔔 您有新的訂單！\n訂單編號: (" + newOrder.getId().substring(0, 5) + "...) " + 
                                 "\n總金額: $" + newOrder.getTotalAmount();

                messagingTemplate.convertAndSend("/topic/orders/" + ownerId, message);
            }

        } catch (Exception e) {
            System.err.println("推播通知失敗: " + e.getMessage());
        }

        return newOrder;
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
            String message = "您的訂單 (編號: " + updatedOrder.getId().substring(0, 5) + "...) 已經準備好，請前往取餐！";

            messagingTemplate.convertAndSend("/topic/orders/" + customerId, message);
        }

        return updatedOrder;
    }
}