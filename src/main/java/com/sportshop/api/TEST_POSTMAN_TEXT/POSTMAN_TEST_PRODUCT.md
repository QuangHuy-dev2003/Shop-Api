# Hướng dẫn test API Sản phẩm (ProductController) bằng Postman

## 1. Lấy tất cả sản phẩm

- **Endpoint:** `GET /api/v1/products`
- **Cách test:**
  - Chọn method `GET`
  - Nhấn Send để nhận danh sách sản phẩm

## 2. Lấy sản phẩm theo ID

- **Endpoint:** `GET /api/v1/products/{id}`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{id}` bằng ID sản phẩm
  - Nhấn Send để nhận thông tin sản phẩm

## 3. Tạo mới sản phẩm

- **Endpoint:** `POST /api/v1/products`
- **Cách test:**
  - Chọn method `POST`
  - Chọn tab `Body` > `form-data`
  - Thêm trường:
    - Key: `product` (type: Text), Value: Dán JSON thông tin sản phẩm (ví dụ bên dưới)
    - Key: `images` (type: File, có thể chọn nhiều file ảnh)
  - Ví dụ JSON cho trường `product`:

```json
{
  "name": "Áo Thun Nam",
  "description": "Áo thun cotton cao cấp",
  "price": 250000,
  "sale": 10,
  "categoryId": 1,
  "brandId": 1,
  "variants": [
    { "size": "M", "color": "Đen", "stockQuantity": 10, "price": 250000 },
    { "size": "L", "color": "Trắng", "stockQuantity": 5, "price": 250000 }
  ]
}
```

- Nhấn Send để tạo sản phẩm mới

## 4. Cập nhật sản phẩm

- **Endpoint:** `PUT /api/v1/products/{id}`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{id}` bằng ID sản phẩm
  - Chọn tab `Body` > `form-data`
  - Thêm trường như khi tạo mới
  - Nhấn Send để cập nhật sản phẩm

## 5. Xóa sản phẩm

- **Endpoint:** `DELETE /api/v1/products/{id}`
- **Cách test:**
  - Chọn method `DELETE`
  - Thay `{id}` bằng ID sản phẩm
  - Nhấn Send để xóa sản phẩm

---

**Lưu ý:**

- Nếu API yêu cầu xác thực, hãy thêm token vào phần Authorization.
- Đảm bảo các ID hợp lệ trước khi test.
