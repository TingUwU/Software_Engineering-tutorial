package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "order") // 對應資料庫中的 order 集合 
public class Order {

    @Id
    private String id; // 訂單ID 

    // required: true, ref: 'store'
    private String storeId; // 店家ID 

    // ref: 'customer' (Optional)
    private String customerId; // 顧客ID 

    // (未登入顧客必填)
    private String customerPhone; // 顧客電話號碼 

    // required: true, enum: ['內用', '外帶']
    private String orderType; // 訂單類型 

    private DineInDetail dineInDetail; // 內用詳情 

    private TakeoutDetail takeoutDetail; // 外帶詳情 

    // required: true
    private List<OrderItem> items = new ArrayList<>(); // 訂單項目 

    // required: true
    private Integer totalAmount; // 訂單總金額 

    // default: ""
    private String remarks = ""; // 顧客備註 

    // required: true, enum: ['店內付款']
    private String paymentMethod; // 支付方式 

    // required: true, default: '已送出', enum: ...
    private String state = "已送出"; // 訂單狀態 

    @CreatedDate
    private Date createAt; // 訂單成立時間 

    @LastModifiedDate
    private Date updatedAt; // 訂單更新時間 

    // --- 內部類別 ---

    public static class DineInDetail {
        private String tableNumber;
        // getters, setters...
    }

    public static class TakeoutDetail {
        private Date takeoutTime;
        // getters, setters...
    }

    public static class OrderItem {
        private String menuItemId;
        private String itemName;
        private Integer quantity;
        private Integer unitPrice;
        private List<String> customization;
        private Integer itemSubTotal;
        // getters, setters...
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public DineInDetail getDineInDetail() {
        return dineInDetail;
    }

    public void setDineInDetail(DineInDetail dineInDetail) {
        this.dineInDetail = dineInDetail;
    }

    public TakeoutDetail getTakeoutDetail() {
        return takeoutDetail;
    }

    public void setTakeoutDetail(TakeoutDetail takeoutDetail) {
        this.takeoutDetail = takeoutDetail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // (記得生成 Getters 和 Setters)

    
}