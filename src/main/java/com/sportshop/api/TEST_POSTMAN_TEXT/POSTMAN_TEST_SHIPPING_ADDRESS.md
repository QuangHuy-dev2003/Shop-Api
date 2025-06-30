# Hướng dẫn test API Địa chỉ giao hàng (ShippingAddressController) bằng Postman

## 1. Lấy danh sách địa chỉ của user

- **Endpoint:** `GET /api/v1/users/{userId}/shipping-addresses`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{userId}` bằng ID của user muốn lấy địa chỉ
  - Nhấn Send để nhận danh sách địa chỉ

## 2. Tạo địa chỉ mới cho user

- **Endpoint:** `POST /api/v1/users/{userId}/shipping-addresses`
- **Cách test:**
  - Chọn method `POST`
  - Thay `{userId}` bằng ID của user
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung ví dụ:

```json
{
  "addressLine": "123 Đường ABC",
  "ward": "Phường 1",
  "district": "Quận 1",
  "province": "TP.HCM",
  "isDefault": true
}
```

- Nhấn Send để tạo địa chỉ mới

## 3. Cập nhật địa chỉ

- **Endpoint:** `PUT /api/v1/users/{userId}/shipping-addresses/{addressId}`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{userId}` và `{addressId}` bằng ID tương ứng
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung ví dụ:

```json
{
  "addressLine": "456 Đường XYZ",
  "ward": "Phường 2",
  "district": "Quận 2",
  "province": "TP.HCM",
  "isDefault": false
}
```

- Nhấn Send để cập nhật địa chỉ

## 4. Đặt địa chỉ làm mặc định

- **Endpoint:** `PUT /api/v1/users/{userId}/shipping-addresses/{addressId}/set-default`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{userId}` và `{addressId}` bằng ID tương ứng
  - Không cần Body
  - Nhấn Send để đặt địa chỉ này làm mặc định

## 5. Xóa địa chỉ

- **Endpoint:** `DELETE /api/v1/users/{userId}/shipping-addresses/{addressId}`
- **Cách test:**
  - Chọn method `DELETE`
  - Thay `{userId}` và `{addressId}` bằng ID tương ứng
  - Nhấn Send để xóa địa chỉ

---

**Lưu ý:**

- Nếu API yêu cầu xác thực, hãy thêm token vào phần Authorization.
- Đảm bảo userId và addressId hợp lệ trước khi test.
