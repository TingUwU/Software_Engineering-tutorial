package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "order") // 對應資料庫中的 "order" 集合
public class Order {

    @Id
    private String id; // 訂單ID (ObjectId)

    // required: true, ref: 'store'
    private String storeId; // 店家ID

    // ref: 'customer' (登入者才有，訪客為 null)
    private String customerId; // 顧客ID

    // (未登入顧客必填)
    private String customerPhone; // 顧客電話號碼

    // required: true, enum: ['內用', '外帶']
    private String orderType; // 訂單類型

    // (當 orderType 為 '內用' 時需要)
    private DineInDetail dineInDetail; // 內用詳情

    // (當 orderType 為 '外帶' 時需要)
    private TakeoutDetail takeoutDetail; // 外帶詳情

    // required: true, default: []
    private List<OrderItem> items = new ArrayList<>(); // 訂單項目列表

    // required: true
    private Integer totalAmount; // 訂單總金額

    // default: ""
    private String remarks = ""; // 顧客備註

    // required: true, enum: ['店內付款']
    private String paymentMethod; // 支付方式

    // required: true, default: '已送出'
    // enum: ['已送出', '已接單', '準備中', '已完成', '顧客已取餐']
    private String state = "已送出"; // 訂單狀態

    @CreatedDate
    private Date createAt; // 訂單成立時間 (自動產生)

    @LastModifiedDate
    private Date updatedAt; // 訂單更新時間 (自動產生)

    // --- 內部類別 (Inner Classes) 定義複雜結構 ---

    // 內用詳情結構
    public static class DineInDetail {
        private String tableNumber; // 桌號

        // Getters and Setters
        public String getTableNumber() { return tableNumber; }
        public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    }

    // 外帶詳情結構
    public static class TakeoutDetail {
        private Date takeoutTime; // 預定取餐時間

        // Getters and Setters
        public Date getTakeoutTime() { return takeoutTime; }
        public void setTakeoutTime(Date takeoutTime) { this.takeoutTime = takeoutTime; }
    }

    // 訂單項目結構 (對應 items 陣列裡的內容)
    public static class OrderItem {
        private String menuItemId; // 餐點ID
        private String itemName;   // 餐點名稱
        private Integer quantity;  // 數量
        private Integer unitPrice; // 單價
        private List<String> customization = new ArrayList<>(); // 客製化選項 (例如: ["去冰", "半糖"])
        private Integer itemSubTotal; // 該項目小計 (單價 * 數量)

        // Getters and Setters
        public String getMenuItemId() { return menuItemId; }
        public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }
        public List<String> getCustomization() { return customization; }
        public void setCustomization(List<String> customization) { this.customization = customization; }
        public Integer getItemSubTotal() { return itemSubTotal; }
        public void setItemSubTotal(Integer itemSubTotal) { this.itemSubTotal = itemSubTotal; }
    }

    // --- 主類別的 Getters and Setters ---
    // (你可以用 VS Code 右鍵 -> Source Action -> Generate Getters and Setters 快速產生)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public DineInDetail getDineInDetail() { return dineInDetail; }
    public void setDineInDetail(DineInDetail dineInDetail) { this.dineInDetail = dineInDetail; }
    public TakeoutDetail getTakeoutDetail() { return takeoutDetail; }
    public void setTakeoutDetail(TakeoutDetail takeoutDetail) { this.takeoutDetail = takeoutDetail; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Integer getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}