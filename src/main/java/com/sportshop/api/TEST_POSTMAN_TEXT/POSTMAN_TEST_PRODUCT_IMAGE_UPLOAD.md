# 🖼️ UPLOAD ẢNH SẢN PHẨM - HƯỚNG DẪN TEST

## 📋 Tổng quan

API upload ảnh sản phẩm cho phép:

- ✅ **Upload nhiều ảnh** cho 1 sản phẩm
- ✅ **Liên kết ảnh với màu sắc** sản phẩm
- ✅ **Tự động resize/crop** ảnh
- ✅ **Lưu vào Cloudinary** và database
- ✅ **Validation đầy đủ** (kích thước, định dạng)

## 🔗 API Endpoints

### 1. Upload ảnh cơ bản

```http
POST /api/v1/products/{productId}/upload-images
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

**Parameters:**

- `productId` (path): ID của sản phẩm
- `images` (form-data): Danh sách file ảnh
- `colors` (form-data, optional): Danh sách màu tương ứng

### 2. Upload ảnh với transformation

```http
POST /api/v1/products/{productId}/upload-images-with-transformation
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

**Parameters:**

- `productId` (path): ID của sản phẩm
- `images` (form-data): Danh sách file ảnh
- `colors` (form-data, optional): Danh sách màu tương ứng
- `width` (form-data, optional): Chiều rộng mong muốn
- `height` (form-data, optional): Chiều cao mong muốn
- `crop` (form-data, optional): Loại crop (fill, scale, fit, etc.)

## 🛠️ Cách test trong Postman

### Bước 1: Tạo sản phẩm trước

```http
POST /api/v1/products
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}

Body:
- product: {
  "name": "Test Product",
  "description": "Product for testing image upload",
  "price": 100000,
  "categoryId": 1,
  "brandId": 1
}
```

### Bước 2: Upload ảnh cơ bản

1. **Tạo request mới:**

   ```
   POST http://localhost:8080/api/v1/products/1/upload-images
   ```

2. **Headers:**

   ```
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

3. **Body (form-data):**
   ```
   images: [file1.jpg] (Type: File)
   images: [file2.jpg] (Type: File)
   colors: ["Red", "Blue"] (Type: Text)
   ```

### Bước 3: Upload ảnh với transformation

1. **Tạo request mới:**

   ```
   POST http://localhost:8080/api/v1/products/1/upload-images-with-transformation
   ```

2. **Headers:**

   ```
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

3. **Body (form-data):**
   ```
   images: [file1.jpg] (Type: File)
   images: [file2.jpg] (Type: File)
   colors: ["Red", "Blue"] (Type: Text)
   width: 800 (Type: Text)
   height: 600 (Type: Text)
   crop: fill (Type: Text)
   ```

## 📝 Ví dụ test cases

### Test Case 1: Upload ảnh cơ bản

**Request:**

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/upload-images" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg" \
  -F "colors=Red" \
  -F "colors=Blue"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Upload ảnh thành công",
  "data": [
    "https://res.cloudinary.com/your-cloud/image/upload/v123/products/image1.jpg",
    "https://res.cloudinary.com/your-cloud/image/upload/v123/products/image2.jpg"
  ]
}
```

### Test Case 2: Upload ảnh với transformation

**Request:**

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/upload-images-with-transformation" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "images=@image1.jpg" \
  -F "width=800" \
  -F "height=600" \
  -F "crop=fill"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Upload ảnh với transformation thành công",
  "data": [
    "https://res.cloudinary.com/your-cloud/image/upload/w_800,h_600,c_fill,f_auto,q_auto/v123/products/image1.jpg"
  ]
}
```

## ⚠️ Validation Rules

### File Validation:

- **Kích thước tối đa**: 5MB
- **Định dạng cho phép**: JPEG, JPG, PNG, GIF, WebP
- **Tên file**: Không được để trống

### Business Rules:

- **Sản phẩm phải tồn tại**: productId phải hợp lệ
- **Ít nhất 1 ảnh**: Danh sách images không được rỗng
- **Colors tương ứng**: Nếu có colors thì phải cùng size với images

## 🔧 Transformation Options

### Crop Types:

- `fill`: Cắt và resize để vừa kích thước
- `scale`: Scale toàn bộ ảnh
- `fit`: Fit ảnh vào kích thước (giữ tỷ lệ)
- `thumb`: Tạo thumbnail
- `limit`: Giới hạn kích thước tối đa

### Ví dụ transformation:

```
width=800, height=600, crop=fill
→ Ảnh sẽ được resize thành 800x600px, cắt nếu cần
```

## 📊 Database Changes

### Bảng `product_images`:

```sql
| id | product_id | image_url | color |
|----|------------|-----------|-------|
| 1  | 1          | url1      | Red   |
| 2  | 1          | url2      | Blue  |
```

### Bảng `products`:

```sql
| id | name | image_url | ... |
|----|------|-----------|-----|
| 1  | Test | url1      | ... |
```

## 🐛 Error Handling

### Common Errors:

| Error                     | Nguyên nhân             | Giải pháp                |
| ------------------------- | ----------------------- | ------------------------ |
| "Không tìm thấy sản phẩm" | productId không tồn tại | Kiểm tra productId       |
| "Kích thước file > 5MB"   | File quá lớn            | Nén ảnh trước khi upload |
| "Định dạng không hợp lệ"  | File không phải ảnh     | Chỉ upload file ảnh      |
| "Danh sách ảnh rỗng"      | Không có file nào       | Chọn ít nhất 1 file      |

### Error Response:

```json
{
  "success": false,
  "message": "Lỗi upload ảnh: Kích thước file không được vượt quá 5MB",
  "data": null
}
```

## 🧪 Test Scenarios

### 1. Happy Path

- ✅ Upload 1 ảnh cho sản phẩm mới
- ✅ Upload nhiều ảnh với màu sắc
- ✅ Upload với transformation

### 2. Edge Cases

- ✅ Upload ảnh cho sản phẩm không tồn tại
- ✅ Upload file không phải ảnh
- ✅ Upload file quá lớn
- ✅ Upload không có file

### 3. Business Logic

- ✅ Kiểm tra ảnh được lưu vào database
- ✅ Kiểm tra URL Cloudinary được trả về
- ✅ Kiểm tra ảnh đại diện được cập nhật

## 📈 Performance Tips

### Optimization:

- **Batch upload**: Upload nhiều ảnh cùng lúc
- **Compression**: Nén ảnh trước khi upload
- **Transformation**: Sử dụng Cloudinary transformation thay vì resize client-side

### Limits:

- **File size**: 5MB per file
- **Batch size**: Khuyến nghị < 10 files per request
- **Concurrent uploads**: Khuyến nghị < 5 requests cùng lúc

## 🔮 Future Enhancements

### Planned Features:

- [ ] **Progress tracking** cho upload lớn
- [ ] **Background processing** cho batch upload
- [ ] **Image optimization** tự động
- [ ] **Watermark** tự động
- [ ] **CDN integration** cho performance

### API Improvements:

- [ ] **Async upload** với callback
- [ ] **Upload resume** cho file lớn
- [ ] **Bulk delete** ảnh
- [ ] **Image metadata** extraction

---

**🎉 Chúc bạn test API upload ảnh thành công!**
