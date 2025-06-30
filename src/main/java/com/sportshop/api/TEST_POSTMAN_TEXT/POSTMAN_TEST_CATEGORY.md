# Hướng dẫn test API Danh mục (CategoryController) bằng Postman

## 1. Lấy tất cả danh mục

- **Endpoint:** `GET /api/v1/categories`
- **Cách test:**
  - Chọn method `GET`
  - Nhấn Send để nhận danh sách danh mục

## 2. Lấy danh mục theo ID

- **Endpoint:** `GET /api/v1/categories/{id}`
- **Cách test:**
  - Chọn method `GET`
  - Thay `{id}` bằng ID danh mục
  - Nhấn Send để nhận thông tin danh mục

## 3. Tạo mới danh mục

- **Endpoint:** `POST /api/v1/categories`
- **Cách test:**
  - Chọn method `POST`
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung ví dụ:

```json
{
  "name": "Áo Nam",
  "description": "Các loại áo nam thời trang"
}
```

- Nhấn Send để tạo danh mục mới

## 4. Cập nhật danh mục

- **Endpoint:** `PUT /api/v1/categories/{id}`
- **Cách test:**
  - Chọn method `PUT`
  - Thay `{id}` bằng ID danh mục
  - Chọn tab `Body` > `raw` > `JSON`
  - Dán nội dung ví dụ như khi tạo mới
  - Nhấn Send để cập nhật danh mục

## 5. Xóa danh mục

- **Endpoint:** `DELETE /api/v1/categories/{id}`
- **Cách test:**
  - Chọn method `DELETE`
  - Thay `{id}` bằng ID danh mục
  - Nhấn Send để xóa danh mục

---

**Lưu ý:**

- Nếu API yêu cầu xác thực, hãy thêm token vào phần Authorization.
- Đảm bảo các ID hợp lệ trước khi test.
