package Team5.example.breakfast_ordering.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Document(collection = "store")
public class Store {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String name;
    private String description;
    private String phone;
    private String email;
    private String address;
    private String category;
    private List<Double> coordinates;
    private List<BusinessHour> businessHours;
    private Boolean isActive;
    private String updatedAt;
    private String ownerId;
    private String avatar; // 商家頭貼
    private List<MenuItem> menu;

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<Double> getCoordinates() { return coordinates; }
    public void setCoordinates(List<Double> coordinates) { this.coordinates = coordinates; }

    public List<BusinessHour> getBusinessHours() { return businessHours; }
    public void setBusinessHours(List<BusinessHour> businessHours) { this.businessHours = businessHours; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<MenuItem> getMenu() { return menu; }
    public void setMenu(List<MenuItem> menu) { this.menu = menu; }

    // Inner class for BusinessHour
    public static class BusinessHour {
        private String day;
        private String start;
        private String end;
        private String note;

        // Getters and Setters
        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }

        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }

        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    // MenuItem
    public static class MenuItem {
        @JsonSerialize(using = ToStringSerializer.class)
        private ObjectId id;
        private String itemName;
        private Double price;
        private String description;
        private String imgUrl;
        private Boolean isAvailable;
        private String tag;
        private List<CustomOption> customOptions;

        public ObjectId getId() { return id; }
        public void setId(ObjectId id) { this.id = id; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImgUrl() { return imgUrl; }
        public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public List<CustomOption> getCustomOptions() { return customOptions; }
        public void setCustomOptions(List<CustomOption> customOptions) { this.customOptions = customOptions; }
    }

    // CustomOption for MenuItem
    public static class CustomOption {
        private String name;
        private Double price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}