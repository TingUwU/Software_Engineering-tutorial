package Team5.example.breakfast_ordering.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import Team5.example.breakfast_ordering.model.Cart;
import Team5.example.breakfast_ordering.model.Cart.CartItem;
import Team5.example.breakfast_ordering.repository.CartRepository;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    private CartRepository cartRepository;

    // 用 userId 回傳該使用者的購物車
    @GetMapping("/{userId}")
    public Cart getCartByUserId(@PathVariable String userId){
        return cartRepository.findByUserId(userId)
               .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
               });
    }

    // 將商品加入使用者 ID 為 userId 的購物車
    @PostMapping("/{userId}/items")
    public Cart addItemToCart(@PathVariable String userId, @RequestBody CartItem newItem, @RequestParam String storeId){
        Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUserId(userId);
                        return cartRepository.save(newCart);
                    });

        if(cart.getStoreId() != null && !cart.getItems().isEmpty() && !cart.getStoreId().equals(storeId)){
            throw new RuntimeException("加入失敗，購物車不可跨店家點餐");
        }

        cart.setStoreId(storeId);
        Optional<CartItem> existingItems = cart.getItems().stream()
                                           .filter(item -> item.getItemId().equals(newItem.getItemId()) &&
                                                           String.valueOf(item.getDescription()).equals(newItem.getDescription()))
                                           .findFirst();
        if(existingItems.isPresent()){
            CartItem item = existingItems.get();
            item.setQuantity(item.getQuantity() + newItem.getQuantity());
            item.setSubtotal(item.getPrice() * item.getQuantity());
        }
        else{
            newItem.setSubtotal(newItem.getPrice() * newItem.getQuantity());
            cart.getItems().add(newItem);
        }

        cart.recalculateTotalPrice();
        return cartRepository.save(cart);
    }

    // 移除購物車內的商品
    @DeleteMapping("/{userId}/items/{itemId}")
    public Cart deleteItemFromCart(@PathVariable String userId, @PathVariable String itemId){
        Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("購物車不存在"));

        boolean isRemoved = cart.getItems().removeIf(item -> item.getItemId().equals(itemId));
        if(!isRemoved){
            throw new RuntimeException("商品不存在在購物車中");
        }

        if(cart.getItems().isEmpty()){
            cart.setStoreId(null);
        }

        cart.recalculateTotalPrice();
        return cartRepository.save(cart);
    }

    // 清空購物車
    @DeleteMapping("/{userId}")
    public String deleteAllItemsFromCart(@PathVariable String userId){
        Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("購物車不存在"));

        cart.getItems().clear();
        cart.setStoreId(null);
        cart.setTotalPrice(0);
        cartRepository.save(cart);

        return "使用者 ID 為：" + userId + "的購物車已清空";
    }
}
