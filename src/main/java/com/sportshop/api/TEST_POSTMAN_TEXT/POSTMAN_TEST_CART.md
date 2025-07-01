# Hướng dẫn test API Giỏ hàng (Cart) với Postman

Tất cả các endpoint đều bắt đầu với `/api/v1/cart`

---

## 1. Lấy thông tin giỏ hàng của user

- **GET** `/api/v1/cart/{userId}`
- Response mẫu:

```json
{
  "success": true,
  "message": "Lấy thông tin giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 2,
    "items": [
      {
        "id": 10,
        "productId": 4,
        "productName": "Quần jean nam cao cấp",
        "variantId": 17,
        "size": "WAIST_28",
        "color": "Xanh đậm",
        "quantity": 2,
        "unitPrice": 850000,
        "totalPrice": 1700000
      }
    ],
    "totalQuantity": 1,
    "totalPrice": 1700000
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 2. Lấy danh sách sản phẩm trong giỏ hàng

- **GET** `/api/v1/cart/{userId}/items`
- Response mẫu:

```json
{
  "success": true,
  "message": "Lấy danh sách sản phẩm trong giỏ hàng thành công",
  "data": [
    {
      "id": 10,
      "productId": 4,
      "productName": "Quần jean nam cao cấp",
      "variantId": 17,
      "size": "WAIST_28",
      "color": "Xanh đậm",
      "quantity": 2,
      "unitPrice": 850000,
      "totalPrice": 1700000
    }
  ],
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 3. Thêm mới sản phẩm vào giỏ hàng

### 3.1. Thêm sản phẩm hợp lệ (có variant)

- **POST** `/api/v1/cart/{userId}/items`
- Body (JSON):

```json
{
  "productId": 4,
  "variantId": 17,
  "size": "WAIST_28",
  "color": "Xanh đậm",
  "quantity": 2
}
```

- Response mẫu:

```json
{
  "success": true,
  "message": "Thêm sản phẩm vào giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 2,
    "items": [
      {
        "id": 10,
        "productId": 4,
        "productName": "Quần jean nam cao cấp",
        "variantId": 17,
        "size": "WAIST_28",
        "color": "Xanh đậm",
        "quantity": 2,
        "unitPrice": 850000,
        "totalPrice": 1700000
      }
    ],
    "totalQuantity": 1,
    "totalPrice": 1700000
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

### 3.2. Thêm sản phẩm không tồn tại

```json
{
  "productId": 9999,
  "variantId": 9999,
  "size": "WAIST_50",
  "color": "Hồng",
  "quantity": 1
}
```

- Response mẫu:

```json
{
  "success": false,
  "message": "Không tìm thấy sản phẩm",
  "data": null,
  "timestamp": "2025-01-27T10:30:00"
}
```

### 3.3. Thêm variant không tồn tại cho sản phẩm có thật

```json
{
  "productId": 4,
  "variantId": 9999,
  "size": "WAIST_28",
  "color": "Xanh đậm",
  "quantity": 1
}
```

- Response mẫu:

```json
{
  "success": false,
  "message": "Không tìm thấy biến thể sản phẩm",
  "data": null,
  "timestamp": "2025-01-27T10:30:00"
}
```

### 3.4. Thiếu trường bắt buộc

```json
{
  "productId": 4,
  "quantity": 1
}
```

- Response mẫu:

```json
{
  "success": false,
  "message": "ID sản phẩm không được để trống",
  "data": null,
  "timestamp": "2025-01-27T10:30:00"
}
```

### 3.5. Thêm quantity <= 0

```json
{
  "productId": 4,
  "variantId": 17,
  "size": "WAIST_28",
  "color": "Xanh đậm",
  "quantity": 0
}
```

- Response mẫu:

```json
{
  "success": false,
  "message": "Số lượng phải lớn hơn 0",
  "data": null,
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 4. Cập nhật sản phẩm trong giỏ hàng (cộng dồn số lượng)

- **PUT** `/api/v1/cart/{userId}/items`
- Body (JSON):

```json
{
  "productId": 4,
  "variantId": 17,
  "size": "WAIST_28",
  "color": "Xanh đậm",
  "quantity": 3
}
```

- Response mẫu:

```json
{
  "success": true,
  "message": "Cập nhật sản phẩm trong giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 2,
    "items": [
      {
        "id": 10,
        "productId": 4,
        "productName": "Quần jean nam cao cấp",
        "variantId": 17,
        "size": "WAIST_28",
        "color": "Xanh đậm",
        "quantity": 5,
        "unitPrice": 850000,
        "totalPrice": 4250000
      }
    ],
    "totalQuantity": 1,
    "totalPrice": 4250000
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 5. Xóa một sản phẩm khỏi giỏ hàng

- **DELETE** `/api/v1/cart/{userId}/items?productId=4&variantId=17`
- Response mẫu:

```json
{
  "success": true,
  "message": "Xóa sản phẩm khỏi giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 2,
    "items": [],
    "totalQuantity": 0,
    "totalPrice": 0
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 6. Xóa toàn bộ sản phẩm trong giỏ hàng

- **DELETE** `/api/v1/cart/{userId}/items/all`
- Response mẫu:

```json
{
  "success": true,
  "message": "Xóa toàn bộ sản phẩm trong giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 2,
    "items": [],
    "totalQuantity": 0,
    "totalPrice": 0
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 7. Xóa giỏ hàng của user

- **DELETE** `/api/v1/cart/{userId}`
- Response mẫu:

```json
{
  "success": true,
  "message": "Xóa giỏ hàng thành công",
  "data": "Xóa giỏ hàng thành công",
  "timestamp": "2025-01-27T10:30:00"
}
```

---

## 8. Lưu ý chung

- Nếu API có bảo mật, thêm header: `Authorization: Bearer <token>`
- Tất cả response đều có cấu trúc:
  - `success`: boolean - trạng thái thành công/thất bại
  - `message`: string - thông báo kết quả
  - `data`: object/array/null - dữ liệu trả về
  - `timestamp`: string - thời gian response
- Các trường hợp lỗi sẽ trả về `success: false` cùng thông báo chi tiết.
- Khi test thêm/cập nhật sản phẩm, nên kiểm tra các trường hợp:
  - Thêm sản phẩm hợp lệ (có/không có variant)
  - Thêm sản phẩm không tồn tại
  - Thêm variant không tồn tại
  - Thêm thiếu trường bắt buộc (productId, quantity, variantId nếu có)
  - Thêm quantity <= 0
  - Thêm sản phẩm đã có trong giỏ
  - Xóa sản phẩm không có trong giỏ
- Để lấy danh sách sản phẩm/variant hợp lệ, nên test trước các API `/api/v1/products` và `/api/v1/products/{id}`.
