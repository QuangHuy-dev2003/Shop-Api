# Hướng dẫn test API Mã giảm giá (DiscountsController) bằng Postman

## 1. Lấy tất cả mã giảm giá

- **Endpoint:** `GET /api/v1/discounts`
- **Cách test:**
  - Chọn method `GET`
  - Nhấn Send để nhận danh sách mã giảm giá

## 2. Lấy mã giảm giá theo ID

- **Endpoint:** `GET /api/v1/discounts/{id}`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{id}` bằng ID mã giảm giá
  - Nhấn Send để nhận thông tin mã giảm giá

## 3. Lấy mã giảm giá theo code

- **Endpoint:** `GET /api/v1/discounts/code/{code}`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{code}` bằng mã code
  - Nhấn Send để nhận thông tin mã giảm giá

## 4. Tạo mới mã giảm giá

- **Endpoint:** `POST /api/v1/discounts`
- **Cách test:**
  - Chọn method `POST`
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung ví dụ:

```json
{
  "code": "SALE10",
  "name": "Giảm 10% toàn bộ đơn hàng",
  "description": "Áp dụng cho tất cả khách hàng",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "minimumOrderAmount": 100000,
  "usageLimit": 100,
  "perUserLimit": 1,
  "startDate": "2024-07-01T00:00:00",
  "endDate": "2024-07-31T23:59:59",
  "isActive": true
}
```

- Nhấn Send để tạo mã giảm giá mới

## 5. Cập nhật mã giảm giá

- **Endpoint:** `PUT /api/v1/discounts/{id}`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{id}` bằng ID mã giảm giá
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung như khi tạo mới
  - Nhấn Send để cập nhật mã giảm giá

## 6. Xóa mã giảm giá

- **Endpoint:** `DELETE /api/v1/discounts/{id}`
- **Cách test:**
  - Chọn method `DELETE`
  - Thay `{id}` bằng ID mã giảm giá
  - Nhấn Send để xóa mã giảm giá

## 7. Kích hoạt/hủy kích hoạt mã giảm giá

- **Endpoint:** `PATCH /api/v1/discounts/{id}/toggle`
- **Cách test:**
  - Chọn method `PATCH`
  - Thay `{id}` bằng ID mã giảm giá
  - Nhấn Send để đổi trạng thái mã giảm giá

## 8. Validate mã giảm giá

- **Endpoint:** `POST /api/v1/discounts/validate`
- **Cách test:**
  - Chọn method `POST`
  - Chọn tab `Params`, thêm:
    - Key: `code`, Value: mã giảm giá
    - Key: `orderAmount`, Value: số tiền đơn hàng
  - Nhấn Send để kiểm tra mã giảm giá

---

**Lưu ý:**

- Nếu API yêu cầu xác thực, hãy thêm token vào phần Authorization.
- Đảm bảo các ID hợp lệ trước khi test.
