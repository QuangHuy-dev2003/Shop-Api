# HƯỚNG DẪN TEST API ĐƠN HÀNG (ORDER)

## 1. Lấy tất cả đơn hàng (admin)

- **Method:** GET
- **URL:** `/api/v1/orders`
- **Query params:**
  - `page` (int, default 0)
  - `size` (int, default 10)
  - `status` (string, optional)
  - `userId` (long, optional)
  - `orderCode` (string, optional)
  - `dateFrom` (yyyy-MM-dd, optional)
  - `dateTo` (yyyy-MM-dd, optional)

**Ví dụ:**

```
GET /api/v1/orders?page=0&size=5&status=CONFIRMED
```

**Response thành công:**

```
{
  "success": true,
  "message": "Lấy danh sách đơn hàng thành công!",
  "data": {
    "content": [
      {
        "orderId": 1,
        "status": "CONFIRMED",
        "orderDate": "2024-07-04T10:00:00",
        "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
        "paymentMethod": "CASH_ON_DELIVERY",
        "paymentStatus": "UNPAID",
        "totalAmount": 1000000,
        "discountAmount": 50000,
        "items": null
      }
    ],
    "totalElements": 10,
    "totalPages": 2,
    "size": 5,
    "number": 0
  },
  "timestamp": "..."
}
```

---

## 2. Lấy đơn hàng theo ID

- **Method:** GET
- **URL:** `/api/v1/orders/{orderId}`

**Ví dụ:**

```
GET /api/v1/orders/1
```

**Response thành công:**

```
{
  "success": true,
  "message": "Lấy đơn hàng thành công!",
  "data": {
    "orderId": 1,
    "status": "CONFIRMED",
    "orderDate": "2024-07-04T10:00:00",
    "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
    "paymentMethod": "CASH_ON_DELIVERY",
    "paymentStatus": "UNPAID",
    "totalAmount": 1000000,
    "discountAmount": 50000,
    "items": [
      {
        "productId": 4,
        "productName": "Quần jean nam cao cấp",
        "size": "WAIST_36",
        "color": "Đen",
        "quantity": 2,
        "unitPrice": 850000,
        "subtotal": 1700000,
        "imageUrl": "..."
      }
    ]
  },
  "timestamp": "..."
}
```

**Trường hợp lỗi:**

- Đơn hàng không tồn tại: trả về `success: false`, message lỗi.

---

## 3. Lấy đơn hàng theo userId

- **Method:** GET
- **URL:** `/api/v1/orders/user/{userId}`
- **Query params:** `page`, `size`

**Ví dụ:**

```
GET /api/v1/orders/user/9?page=0&size=5
```

**Response:** giống API lấy tất cả đơn hàng, chỉ lọc theo user.

---

## 4. Tìm kiếm đơn hàng theo mã

- **Method:** GET
- **URL:** `/api/v1/orders/search?orderCode=VNSPX1234567`

**Response:** giống API lấy đơn hàng theo ID.

---

## 5. Thống kê đơn hàng

- **Method:** GET
- **URL:** `/api/v1/orders/statistics?from=2024-07-01&to=2024-07-31`

**Response thành công:**

```
{
  "success": true,
  "message": "Thống kê đơn hàng thành công!",
  "data": {
    "totalOrders": 20,
    "totalRevenue": 20000000,
    "orderCountByStatus": {
      "CONFIRMED": 10,
      "CANCELLED": 2
    },
    "revenueByStatus": {
      "CONFIRMED": 18000000,
      "CANCELLED": 0
    }
  },
  "timestamp": "..."
}
```

---

## 6. Cập nhật đơn hàng

- **Method:** PUT
- **URL:** `/api/v1/orders/{orderId}`
- **Body:**

```
{
  "status": "SHIPPING",
  "addressLine": "456 Đường XYZ",
  "ward": "Phường 2",
  "district": "Quận 3",
  "province": "TP.HCM"
}
```

**Response:** giống API lấy đơn hàng theo ID.
**Lưu ý:**

- Chỉ cho phép cập nhật trạng thái và thông tin nhận hàng.
- Validate trạng thái hợp lệ.

---

## 7. Xoá đơn hàng

- **Method:** DELETE
- **URL:** `/api/v1/orders/{orderId}`

**Response thành công:**

```
{
  "success": true,
  "message": "Xoá đơn hàng thành công!",
  "data": "Xoá đơn hàng thành công!",
  "timestamp": "..."
}
```

**Trường hợp lỗi:**

- Đơn hàng không tồn tại hoặc đã bị huỷ trước đó.

---

## 8. Hủy đơn hàng (user)

- **Method:** POST
- **URL:** `/api/v1/orders/{orderId}/cancel/user?userId=9`

**Response thành công:**

```
{
  "success": true,
  "message": "Hủy đơn hàng (user) thành công!",
  "data": "Hủy đơn hàng (user) thành công!",
  "timestamp": "..."
}
```

**Trường hợp lỗi:**

- Không phải chủ đơn hàng.
- Đơn hàng đã quá 1 tiếng không được huỷ.
- Đơn hàng đã bị huỷ trước đó.

---

## 9. Hủy đơn hàng (admin)

- **Method:** POST
- **URL:** `/api/v1/orders/{orderId}/cancel/admin?adminId=1`

**Response thành công:**

```
{
  "success": true,
  "message": "Hủy đơn hàng (admin) thành công!",
  "data": "Hủy đơn hàng (admin) thành công!",
  "timestamp": "..."
}
```

**Trường hợp lỗi:**

- Không có quyền admin (nếu có kiểm tra quyền).
- Đơn hàng đã bị huỷ trước đó.

---

## Lưu ý chung

- Tất cả API trả về chuẩn JSON, có trường `success`, `message`, `data`, `timestamp`.
- Nếu lỗi sẽ trả về `success: false`, message lỗi rõ ràng.
- Các API phân trang trả về object Page chuẩn Spring.
- Validate đầu vào, trạng thái, quyền truy cập.
