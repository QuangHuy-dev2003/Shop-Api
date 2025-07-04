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

# HƯỚNG DẪN TEST API SẢN PHẨM (PRODUCT) - ẢNH THEO MÀU

## 1. Thêm mới sản phẩm với nhiều ảnh theo màu

- **Method:** POST
- **URL:** `/api/v1/products`
- **Body:** `form-data`
  - `product`: (string, JSON) thông tin sản phẩm, ví dụ:
    ```json
    {
      "name": "Quần jean nam cao cấp",
      "description": "Quần jean nhiều màu, nhiều size",
      "price": 850000,
      "categoryId": 1,
      "brandId": 1,
      "additionalImages": ["img1.jpg", "img2.jpg", "img3.jpg"],
      "imageColors": ["Đen", "Xanh đậm", "Đen"],
      "variants": [
        {
          "size": "WAIST_28",
          "color": "Đen",
          "stockQuantity": 10,
          "price": 850000
        },
        {
          "size": "WAIST_30",
          "color": "Xanh đậm",
          "stockQuantity": 15,
          "price": 850000
        }
      ]
    }
    ```
  - `images`: (file, multiple) upload các ảnh sản phẩm (thứ tự phải khớp với `imageColors`)

**Lưu ý:**

- `imageColors` là mảng màu, mỗi phần tử ứng với 1 ảnh trong `images` hoặc `additionalImages`.
- Nếu ảnh là ảnh chung (không phân biệt màu), để giá trị `null` hoặc không truyền `imageColors`.

**Kết quả:**

- Ảnh sẽ được lưu vào DB với đúng trường `color`.
- Khi đặt hàng chọn màu nào, hệ thống sẽ lấy đúng ảnh theo màu đó.

---

## 2. Cập nhật sản phẩm với ảnh theo màu

- **Method:** PUT
- **URL:** `/api/v1/products/{id}`
- **Body:** giống như thêm mới sản phẩm.

---

## 3. Kiểm tra ảnh đúng màu khi đặt hàng/hiển thị đơn hàng

- Đặt hàng với sản phẩm, chọn màu (ví dụ: "Đen").
- Kiểm tra trong email xác nhận/chi tiết đơn hàng:
  - Ảnh sản phẩm đúng là ảnh có trường `color` = "Đen" trong DB.
- Nếu không có ảnh cho màu đã chọn, hệ thống sẽ lấy ảnh chung (color = null).

---

## 4. Các trường hợp đặc biệt cần test

- **Chỉ có ảnh chung:** Không truyền `imageColors` hoặc truyền toàn bộ là `null`.
- **Một số ảnh có màu, một số ảnh chung:** Truyền `imageColors` như `["Đen", null, "Xanh đậm"]`.
- **Thiếu ảnh cho một màu:** Đặt hàng với màu không có ảnh, hệ thống sẽ lấy ảnh chung.
- **Nhiều ảnh cho cùng một màu:** Hệ thống sẽ lấy ảnh đầu tiên theo màu đó.

---

## 5. Lưu ý khi test

- Thứ tự ảnh và màu trong `images`/`additionalImages` và `imageColors` phải khớp nhau.
- Nếu upload qua Postman, chọn `form-data`, key `images` là file, key `product` là JSON string.
- Đảm bảo FE gửi đúng mapping màu cho từng ảnh.
