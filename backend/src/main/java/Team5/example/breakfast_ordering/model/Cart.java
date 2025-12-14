package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "cart") // 對應資料庫中的 cart 集合 
public class Cart {
    @Id
    private String id;      // 購物車 ID

    @Indexed
    private String userId;  // 使用者 ID

    // required: true
    private String storeId; // 店家 ID

    // default: []
    private List<CartItem> items = new ArrayList<>();  // 購物車內容

    // default: 0
    private Integer totalPrice = 0;

    // default: 現在時間
    @LastModifiedDate
    private Date updateAt;

    public static class CartItem{
        private String itemId;
        private String itemName;
        private Integer price;
        private Integer quantity;
        private Integer subtotal;
        private String description;

        public CartItem(){}

        public CartItem(String itemId, String itemName, Integer price, Integer quantity, Integer subtotal, String description){
            this.itemId = itemId;
            this.itemName = itemName;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price * quantity;
            this.description = description;
        }

        public String getItemId(){
            return this.itemId;
        }

        public void setItemId(String itemId){
            this.itemId = itemId;
        }

        public String getItemName(){
            return this.itemName;
        }

        public void setItemName(String itemName){
            this.itemName = itemName;
        }

        public Integer getPrice(){
            return this.price;
        }

        public void setPrice(Integer price){
            this.price = price;
        }

        public Integer getQuantity(){
            return this.quantity;
        }

        public void setQuantity(Integer quantity){
            this.quantity = quantity;
        }

        public Integer getSubtotal(){
            return this.subtotal;
        }

        public void setSubtotal(Integer subtotal){
            this.subtotal = subtotal;
        }

        public String getDescription(){
            return this.description;
        }

        public void setDescription(String description){
            this.description = description;
        }
    }

    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUserId(){
        return this.userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getStoreId(){
        return this.storeId;
    }

    public void setStoreId(String storeId){
        this.storeId = storeId;
    }

    public List<CartItem> getItems(){
        return this.items;
    }

    public void setItems(List<CartItem> items){
        this.items = items;
    }

    public Integer getTotalPrice(){
        return this.totalPrice;
    }

    public void setTotalPrice(Integer totalPrice){
        this.totalPrice = totalPrice;
    }

    public Date getUpdateAt(){
        return this.updateAt;
    }

    public void setUpdateAt(Date updateAt){
        this.updateAt = updateAt;
    }

    // 輔助函式，拿 items 裡的所有物件的 subtotal 相加
    public void recalculateTotalPrice(){
        this.totalPrice = this.items.stream()
                          .mapToInt(CartItem::getSubtotal)
                          .sum();
    }
}
