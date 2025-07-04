# 🖼️ UPLOAD ẢNH LÊN CLOUDINARY - HƯỚNG DẪN TEST

## 📋 Tổng quan

API upload ảnh lên Cloudinary để chuẩn bị cho việc import Excel. Ảnh sẽ được lưu trong folder "products" trên Cloudinary và trả về URL để sử dụng trong Excel.

## 🔗 API Endpoints

### 1. Upload ảnh cơ bản

```http
POST /api/v1/upload-images
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
Body:
  - images: [file1, file2, ...] (MultipartFile)
```

### 2. Upload ảnh với transformation

```http
POST /api/v1/upload-images-with-transformation
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
Body:
  - images: [file1, file2, ...] (MultipartFile)
  - width: 800 (Integer, optional)
  - height: 600 (Integer, optional)
  - crop: "fill" (String, optional)
```

## 📊 Response Examples

### Success Response:

```json
{
  "success": true,
  "message": "Upload ảnh lên Cloudinary thành công",
  "data": [
    "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/products/image1.jpg",
    "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/products/image2.jpg"
  ]
}
```

### Error Response:

```json
{
  "success": false,
  "message": "Lỗi upload ảnh: File không được để trống",
  "data": null
}
```

## 🛠️ Cách sử dụng trong Postman

### Bước 1: Tạo request mới

**Method:** POST  
**URL:** `http://localhost:8080/api/v1/upload-images`

### Bước 2: Headers

```
Content-Type: multipart/form-data
Authorization: Bearer YOUR_JWT_TOKEN
```

### Bước 3: Body (form-data)

| Key    | Type | Value          | Description                |
| ------ | ---- | -------------- | -------------------------- |
| images | File | [Select files] | Chọn 1 hoặc nhiều file ảnh |

### Bước 4: Test với transformation

**URL:** `http://localhost:8080/api/v1/upload-images-with-transformation`

| Key    | Type | Value          | Description                |
| ------ | ---- | -------------- | -------------------------- |
| images | File | [Select files] | Chọn 1 hoặc nhiều file ảnh |
| width  | Text | 800            | Chiều rộng (optional)      |
| height | Text | 600            | Chiều cao (optional)       |
| crop   | Text | fill           | Loại crop (optional)       |

## 📝 Ví dụ sử dụng

### 1. Upload ảnh cơ bản:

```bash
curl -X POST "http://localhost:8080/api/v1/upload-images" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "images=@product1.jpg" \
  -F "images=@product2.jpg"
```

### 2. Upload với resize:

```bash
curl -X POST "http://localhost:8080/api/v1/upload-images-with-transformation" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "images=@product1.jpg" \
  -F "width=800" \
  -F "height=600" \
  -F "crop=fill"
```

## 🎯 Sử dụng URL trong Excel Import

Sau khi upload thành công, copy các URL và sử dụng trong Excel:

| Product Code | Product Name | ... | Image URLs                                                                          |
| ------------ | ------------ | --- | ----------------------------------------------------------------------------------- |
| JC8004       | Adidas Tee   | ... | https://res.cloudinary.com/.../image1.jpg,https://res.cloudinary.com/.../image2.jpg |

## ⚠️ Lưu ý quan trọng

### 1. Giới hạn file:

- **Kích thước tối đa:** 5MB per file
- **Định dạng hỗ trợ:** JPEG, PNG, GIF, WebP
- **Số lượng:** Không giới hạn (tùy theo server)

### 2. Folder lưu trữ:

- Tất cả ảnh được lưu trong folder **"products"** trên Cloudinary
- **Tên file được giữ nguyên** (đã loại bỏ ký tự đặc biệt)
- URL sẽ có dạng: `https://res.cloudinary.com/your-cloud/image/upload/v1234567890/products/adidas_house_of_tiro_nations_pack_tee.jpg`

### 3. Quy tắc đặt tên file:

- **Ký tự đặc biệt** (space, dấu câu) → chuyển thành `_`
- **Nhiều `_` liên tiếp** → gộp thành 1 `_`
- **Chuyển thành lowercase**
- **Ví dụ:** `ADIDAS HOUSE OF TIRO NATIONS PACK TEE.jpg` → `adidas_house_of_tiro_nations_pack_tee`

### 4. Transformation options:

- **width/height:** Kích thước mong muốn (pixel)
- **crop:**
  - `fill`: Cắt và resize để vừa kích thước
  - `scale`: Resize giữ nguyên tỷ lệ
  - `fit`: Resize để vừa trong khung

## 🧪 Test Cases

### 1. Upload 1 ảnh:

- Chọn 1 file ảnh JPEG/PNG
- Kích thước < 5MB
- Expected: Trả về 1 URL

### 2. Upload nhiều ảnh:

- Chọn 3-5 file ảnh
- Expected: Trả về danh sách URL tương ứng

### 3. Upload với transformation:

- Chọn ảnh lớn
- Set width=800, height=600, crop=fill
- Expected: Ảnh được resize về 800x600

### 4. Upload file không hợp lệ:

- Chọn file PDF hoặc file > 5MB
- Expected: Trả về lỗi validation

## 🔧 Troubleshooting

### Lỗi thường gặp:

| Lỗi                                       | Nguyên nhân         | Giải pháp                      |
| ----------------------------------------- | ------------------- | ------------------------------ |
| "File không được để trống"                | Chưa chọn file      | Chọn ít nhất 1 file ảnh        |
| "Kích thước file không được vượt quá 5MB" | File quá lớn        | Nén ảnh hoặc chọn file nhỏ hơn |
| "Chỉ chấp nhận file ảnh"                  | File không phải ảnh | Chọn file JPEG, PNG, GIF, WebP |
| "Lỗi upload ảnh"                          | Lỗi Cloudinary      | Kiểm tra cấu hình Cloudinary   |

### Debug Tips:

1. **Kiểm tra file size** trước khi upload
2. **Đảm bảo file là ảnh** (không phải PDF, DOC, etc.)
3. **Kiểm tra kết nối internet** khi upload
4. **Xem log server** để debug lỗi Cloudinary

## 📈 Performance

### Tối ưu hóa:

- **Batch upload:** Upload nhiều ảnh cùng lúc
- **Compression:** Cloudinary tự động nén ảnh
- **CDN:** Ảnh được serve qua CDN toàn cầu

### Giới hạn:

- **File size:** 5MB per file
- **Concurrent uploads:** Tùy theo server capacity
- **Total size:** Không giới hạn (tùy theo Cloudinary plan)

---

**🎉 Chúc bạn upload ảnh thành công và sử dụng hiệu quả trong Excel import!**
