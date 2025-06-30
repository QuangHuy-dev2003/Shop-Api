# Hướng dẫn test API Thương hiệu (BrandController) bằng Postman

## 1. Lấy tất cả thương hiệu

- **Endpoint:** `GET /api/v1/brands`
- **Cách test:**
  - Chọn method `GET`
  - Nhấn Send để nhận danh sách thương hiệu

## 2. Lấy thương hiệu theo ID

- **Endpoint:** `GET /api/v1/brands/{id}`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{id}` bằng ID thương hiệu
  - Nhấn Send để nhận thông tin thương hiệu

## 3. Tạo mới thương hiệu

- **Endpoint:** `POST /api/v1/brands`
- **Cách test:**
  - Chọn method `POST`
  - Chọn tab `Body` > `form-data`
  - Thêm trường:
    - Key: `brand` (type: Text), Value: Dán JSON thông tin thương hiệu (ví dụ bên dưới)
    - Key: `logo` (type: File, có thể chọn file ảnh logo)
  - Ví dụ JSON cho trường `brand`:

```json
{
  "name": "Nike",
  "description": "Thương hiệu thể thao nổi tiếng"
}
```

- Nhấn Send để tạo thương hiệu mới

## 4. Cập nhật thương hiệu

- **Endpoint:** `PUT /api/v1/brands/{id}`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{id}` bằng ID thương hiệu
  - Chọn tab `Body` > `form-data`
  - Thêm trường như khi tạo mới
  - Nhấn Send để cập nhật thương hiệu

## 5. Xóa thương hiệu

- **Endpoint:** `DELETE /api/v1/brands/{id}`
- **Cách test:**
  - Chọn method `DELETE`
  - Thay `{id}` bằng ID thương hiệu
  - Nhấn Send để xóa thương hiệu

---

**Lưu ý:**

- Nếu API yêu cầu xác thực, hãy thêm token vào phần Authorization.
- Đảm bảo các ID hợp lệ trước khi test.
