package Team5.example.breakfast_ordering.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import Team5.example.breakfast_ordering.model.Cart;
import Team5.example.breakfast_ordering.model.Cart.CartItem;
import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.CartRepository;
import Team5.example.breakfast_ordering.repository.StoreRepository;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private StoreRepository storeRepository;

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

            // 如果現有項目沒有圖片URL，嘗試從店家資料中獲取
            if (item.getImgUrl() == null || item.getImgUrl().isEmpty()) {
                Store store = storeRepository.findById(storeId).orElse(null);
                if (store != null) {
                    Store.MenuItem existingMenuItem = store.getMenu().stream()
                        .filter(menuItem -> menuItem.getId().toString().equals(item.getItemId()))
                        .findFirst().orElse(null);
                    if (existingMenuItem != null && existingMenuItem.getImgUrl() != null) {
                        item.setImgUrl(existingMenuItem.getImgUrl());
                    }
                }
            }
        }
        else{
            // 從店家資料中獲取商品圖片URL
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                Store.MenuItem menuItem = store.getMenu().stream()
                    .filter(item -> item.getId().toString().equals(newItem.getItemId()))
                    .findFirst().orElse(null);
                if (menuItem != null) {
                    newItem.setImgUrl(menuItem.getImgUrl());
                }
            }

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

        // 如果購物車已空，直接刪除整個購物車記錄
        if(cart.getItems().isEmpty()){
            cartRepository.delete(cart);
            // 返回空的購物車物件（不存入資料庫）
            Cart emptyCart = new Cart();
            emptyCart.setUserId(userId);
            emptyCart.setStoreId(null);
            emptyCart.setTotalPrice(0);
            return emptyCart;
        }

        cart.recalculateTotalPrice();
        return cartRepository.save(cart);
    }

    // 清空購物車
    @DeleteMapping("/{userId}")
    public String deleteAllItemsFromCart(@PathVariable String userId){
        Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("購物車不存在"));

        // 直接刪除整個購物車記錄
        cartRepository.delete(cart);

        return "使用者 ID 為：" + userId + "的購物車已清空";
    }
}
