# Hướng dẫn test API Đặt Hàng (Order)

## 1. Endpoint

```
POST /api/v1/orders/place-order
```

## 2. Request Body (JSON)

```json
{
  "userId": 1,
  "paymentMethod": "CASH_ON_DELIVERY",
  "shippingMethod": "STANDARD",
  "discountCodes": ["SALE50", "FREESHIP30"]
}
```

- `userId`: ID người dùng (bắt buộc)
- `paymentMethod`: Phương thức thanh toán (`CASH_ON_DELIVERY`, `VNPAY`, `MOMO`, `PAYPAL`)
- `shippingMethod`: Phương thức vận chuyển (`STANDARD`, `EXPRESS`)
- `discountCodes`: Danh sách mã giảm giá (tối đa 1 mã giảm tiền + 1 mã freeship)
  - VD: `["SALE50", "FREESHIP30"]`

## 3. Response thành công

```json
{
  "success": true,
  "message": "Đặt hàng thành công!",
  "data": {
    "orderId": 123,
    "status": "CONFIRMED",
    "orderDate": "2024-06-01T10:00:00",
    "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
    "paymentMethod": "CASH_ON_DELIVERY",
    "paymentStatus": "UNPAID",
    "totalAmount": 1700000,
    "discountAmount": 50000,
    "items": [
      {
        "productId": 4,
        "productName": "Quần jean nam cao cấp",
        "size": "WAIST_34",
        "color": "Đen",
        "quantity": 2,
        "unitPrice": 850000,
        "subtotal": 1700000,
        "imageUrl": "https://..."
      }
    ]
  }
}
```

## 4. Response lỗi (dùng quá số lần mã giảm giá)

```json
{
  "success": false,
  "message": "Bạn không còn quyền sử dụng mã giảm giá SALE50 này nữa!",
  "data": null
}
```

## 5. Lưu ý khi test

- Mỗi đơn hàng chỉ áp dụng tối đa **1 mã giảm tiền** (FIXED_AMOUNT hoặc PERCENTAGE) **và 1 mã FREESHIP**.
- Nếu truyền nhiều hơn 1 mã giảm tiền hoặc nhiều hơn 1 mã freeship, hệ thống chỉ lấy mã đầu tiên của từng loại.
- Nếu user đã dùng hết số lượt cho mã giảm giá (`per_user_limit`), sẽ báo lỗi.
- Nếu mã không hợp lệ hoặc hết hạn, sẽ báo lỗi.
- Phí vận chuyển: `STANDARD` = 30,000đ, `EXPRESS` = 50,000đ.
- Nếu có mã freeship, phí vận chuyển sẽ được giảm tương ứng.
- Email xác nhận sẽ hiển thị đầy đủ thông tin đơn hàng, phí ship, giảm giá, tổng cộng.

---

**Ví dụ test nhanh:**

- Thêm sản phẩm vào giỏ hàng.
- Gọi API `/api/v1/orders/place-order` với body như trên.
- Kiểm tra email xác nhận và response trả về.
