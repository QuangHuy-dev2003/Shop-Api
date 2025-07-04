# 🚀 BULK IMPORT SẢN PHẨM - HƯỚNG DẪN HOÀN CHỈNH

## 📋 Tổng quan

Tính năng bulk import sản phẩm từ Excel đã được hoàn thiện với đầy đủ các tính năng:

- ✅ **Tạo template Excel tự động** với dữ liệu mẫu và hướng dẫn
- ✅ **Import dữ liệu từ Excel** với validation đầy đủ
- ✅ **Xử lý lỗi thông minh** với file Excel lỗi được highlight
- ✅ **Tự động tạo category/brand** nếu chưa tồn tại
- ✅ **Liên kết ảnh với màu sắc** sản phẩm
- ✅ **Tính toán stock quantity** từ các variants

## 🔗 API Endpoints

### 1. Download Excel Template

```http
GET /api/v1/products/download-excel-template
Authorization: Bearer {jwt_token}
```

### 2. Import Products from Excel

```http
POST /api/products/import-products-from-excel
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
Body: file (Excel file)
```

## 📊 Format Excel

### Cấu trúc cột:

| A              | B              | C           | D             | E          | F     | G    | H       | I                | J          |
| -------------- | -------------- | ----------- | ------------- | ---------- | ----- | ---- | ------- | ---------------- | ---------- |
| Product Code\* | Product Name\* | Description | Category Name | Brand Name | Color | Size | Price\* | Stock Quantity\* | Image URLs |

### Quy tắc dữ liệu:

| Cột            | Bắt buộc | Mô tả                                | Ví dụ                                                       |
| -------------- | -------- | ------------------------------------ | ----------------------------------------------------------- |
| Product Code   | ✅       | Mã sản phẩm duy nhất (nhóm variants) | "JC8004", "SP001"                                           |
| Product Name   | ✅       | Tên sản phẩm                         | "ADIDAS HOUSE OF TIRO NATIONS PACK TEE"                     |
| Description    | ❌       | Mô tả sản phẩm                       | "Áo thể thao Adidas cao cấp"                                |
| Category Name  | ❌       | Tên danh mục (tự tạo nếu chưa có)    | "Áo thể thao"                                               |
| Brand Name     | ❌       | Tên thương hiệu (tự tạo nếu chưa có) | "Adidas"                                                    |
| Color          | ❌       | Màu sắc variant                      | "Trắng", "Vàng"                                             |
| Size           | ❌       | Kích thước (enum có sẵn)             | "S", "M", "L", "XL"                                         |
| Price          | ✅       | Giá sản phẩm (VND)                   | 1050000                                                     |
| Stock Quantity | ✅       | Số lượng tồn kho                     | 5                                                           |
| Image URLs     | ❌       | URL ảnh (phân cách bằng dấu phẩy)    | "https://example.com/img1.jpg,https://example.com/img2.jpg" |

### Size hợp lệ:

#### Áo:

- XS, S, M, L, XL, XXL, XXXL

#### Giày:

- SIZE_36, SIZE_37, SIZE_38, SIZE_39, SIZE_40, SIZE_41, SIZE_42, SIZE_43, SIZE_44, SIZE_45, SIZE_46

#### Quần:

- WAIST_28, WAIST_29, WAIST_30, WAIST_31, WAIST_32, WAIST_33, WAIST_34, WAIST_35, WAIST_36
- WAIST_38, WAIST_40, WAIST_42, WAIST_44, WAIST_46, WAIST_48, WAIST_50

## 🛠️ Cách sử dụng

### Bước 1: Tải template Excel

```bash
curl -X GET "http://localhost:8080/api/v1/products/download-excel-template" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o product_template.xlsx
```

### Bước 2: Chuẩn bị dữ liệu

1. Mở file `product_template.xlsx`
2. Đọc hướng dẫn trong Sheet 2
3. Điền dữ liệu vào Sheet 1 theo format
4. Upload ảnh lên Cloudinary và lấy URL

### Bước 3: Import dữ liệu

```bash
curl -X POST "http://localhost:8080/api/v1/products/import-products-from-excel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@your_data.xlsx"
```

## 📝 Ví dụ dữ liệu

### Ví dụ 1: Sản phẩm Adidas với nhiều màu và size

**Thông tin sản phẩm:**

- Tên: ADIDAS HOUSE OF TIRO NATIONS PACK TEE
- Mã: JC8004
- Giá: 1,050,000 VND
- Brand: Adidas
- Category: Áo thể thao
- Màu Trắng: Size S, L (số lượng: 5)
- Màu Vàng: Size M, S, L, XL (số lượng: 10)

**Dữ liệu Excel:**

| Product Code | Product Name                          | Description                | Category Name | Brand Name | Color | Size | Price   | Stock Quantity | Image URLs                                       |
| ------------ | ------------------------------------- | -------------------------- | ------------- | ---------- | ----- | ---- | ------- | -------------- | ------------------------------------------------ |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Trắng | S    | 1050000 | 5              | https://res.cloudinary.com/example/white.jpg     |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Trắng | L    | 1050000 | 5              | https://res.cloudinary.com/example/white.jpg     |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Vàng  | M    | 1050000 | 10             | https://res.cloudinary.com/example/yellow_m.jpg  |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Vàng  | S    | 1050000 | 10             | https://res.cloudinary.com/example/yellow_s.jpg  |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Vàng  | L    | 1050000 | 10             | https://res.cloudinary.com/example/yellow_l.jpg  |
| JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | Áo thể thao Adidas cao cấp | Áo thể thao   | Adidas     | Vàng  | XL   | 1050000 | 10             | https://res.cloudinary.com/example/yellow_xl.jpg |

### Ví dụ 2: Sản phẩm giày Nike với nhiều màu

```
SP001 | Nike Air Max 270 | Giày chạy bộ thoải mái | Running Shoes | Nike | Red | SIZE_42 | 1500000 | 10 | https://res.cloudinary.com/example/red.jpg
SP001 | Nike Air Max 270 | Giày chạy bộ thoải mái | Running Shoes | Nike | Blue | SIZE_42 | 1500000 | 5 | https://res.cloudinary.com/example/blue.jpg
```

### Ví dụ 3: Sản phẩm áo với nhiều size

```
SP002 | Adidas T-Shirt | Áo thun thể thao | T-Shirts | Adidas | Black | S | 500000 | 15 | https://res.cloudinary.com/example/black.jpg
SP002 | Adidas T-Shirt | Áo thun thể thao | T-Shirts | Adidas | Black | M | 500000 | 20 | https://res.cloudinary.com/example/black.jpg
SP002 | Adidas T-Shirt | Áo thun thể thao | T-Shirts | Adidas | Black | L | 500000 | 12 | https://res.cloudinary.com/example/black.jpg
```

## 📤 Response Examples

### Success Response:

```json
{
  "success": true,
  "message": "Import thành công",
  "data": {
    "successCount": 6,
    "errorCount": 0,
    "errors": []
  }
}
```

### Kết quả trong Database:

**Bảng `products`:**

```sql
| id | product_code | name                                | price    | category_id | brand_id | stock_quantity |
|----|--------------|-------------------------------------|----------|-------------|----------|----------------|
| 1  | JC8004       | ADIDAS HOUSE OF TIRO NATIONS PACK TEE | 1050000  | 1           | 1        | 60             |
```

**Bảng `product_variants`:**

```sql
| id | product_id | color | size | price    | stock_quantity |
|----|------------|-------|------|----------|----------------|
| 1  | 1          | Trắng | S    | 1050000  | 5              |
| 2  | 1          | Trắng | L    | 1050000  | 5              |
| 3  | 1          | Vàng  | M    | 1050000  | 10             |
| 4  | 1          | Vàng  | S    | 1050000  | 10             |
| 5  | 1          | Vàng  | L    | 1050000  | 10             |
| 6  | 1          | Vàng  | XL   | 1050000  | 10             |
```

**Bảng `product_images`:**

```sql
| id | product_id | image_url                                    | color |
|----|------------|----------------------------------------------|-------|
| 1  | 1          | https://res.cloudinary.com/example/white.jpg | Trắng |
| 2  | 1          | https://res.cloudinary.com/example/yellow_m.jpg | Vàng  |
| 3  | 1          | https://res.cloudinary.com/example/yellow_s.jpg | Vàng  |
| 4  | 1          | https://res.cloudinary.com/example/yellow_l.jpg | Vàng  |
| 5  | 1          | https://res.cloudinary.com/example/yellow_xl.jpg | Vàng  |
```

**Lưu ý:** Ảnh màu Trắng chỉ lưu 1 lần cho cả 2 variants (S, L), không duplicate.

### Partial Success (with errors):

```json
{
  "success": true,
  "message": "Import thành công",
  "data": {
    "successCount": 2,
    "errorCount": 1,
    "errors": [
      {
        "row": 3,
        "error": "Size không hợp lệ: INVALID_SIZE. Các size hợp lệ: XS, S, M, L, XL, XXL, XXXL, SIZE_36-SIZE_46, WAIST_28-WAIST_50"
      }
    ],
    "errorFile": "File lỗi đã được tạo"
  }
}
```

## 🔧 Tính năng nâng cao

### 1. Logic Nhóm Variants

- **Product Code duy nhất**: Mỗi sản phẩm có 1 Product Code riêng
- **Tự động nhóm variants**: Các dòng cùng Product Code → cùng 1 sản phẩm
- **Tính toán stock tổng**: Tổng stock của tất cả variants
- **Liên kết ảnh theo màu**: Ảnh được gán màu tương ứng với variant
- **Tránh duplicate ảnh**: Chỉ lưu ảnh 1 lần cho mỗi màu, không lưu lại cho các variants cùng màu

### 2. Excel Template Generation

- **Tự động tạo template** với dữ liệu mẫu
- **Hướng dẫn chi tiết** trong sheet thứ 2
- **Format chuyên nghiệp** với styling đẹp

### 2. Smart Data Processing

- **Auto-create categories/brands** nếu chưa tồn tại
- **Link images to colors** tự động
- **Calculate total stock** từ variants
- **Handle various cell types** (string, numeric, formula)

### 3. Advanced Error Handling

- **Row-by-row error reporting** với chi tiết cụ thể
- **Error file generation** với highlight đỏ
- **Comprehensive validation** cho tất cả fields
- **Size enum validation** với thông báo rõ ràng

### 4. File Management

- **Read only first sheet** để tránh nhầm lẫn
- **Support .xlsx format** hiện đại
- **Memory efficient** processing

## 🧪 Test Cases

### 1. Template Download Test

```bash
# Test download template
curl -X GET "http://localhost:8080/api/products/download-excel-template" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o test_template.xlsx
```

### 2. Valid Import Test

```bash
# Test import with valid data
curl -X POST "http://localhost:8080/api/products/import-products-from-excel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@valid_data.xlsx"
```

### 3. Error Handling Test

```bash
# Test import with invalid data
curl -X POST "http://localhost:8080/api/products/import-products-from-excel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@invalid_data.xlsx"
```

## ⚠️ Lưu ý quan trọng

### 1. Template Usage

- **Luôn sử dụng template** đã tải về để đảm bảo format đúng
- **Không xóa dòng header** (dòng 1)
- **Chỉ điền dữ liệu** vào Sheet 1
- **Product Code phải giống nhau** cho các variants của cùng 1 sản phẩm

### 2. Data Validation

- **Product Code, Product Name, Price, Stock Quantity** là bắt buộc
- **Product Code** phải duy nhất cho mỗi sản phẩm (các variants cùng sản phẩm dùng chung Product Code)
- **Size** phải sử dụng đúng enum values
- **Price** phải > 0 (VND)
- **Stock Quantity** phải >= 0

### 3. Image Management

- **Upload ảnh trước** lên Cloudinary
- **Sử dụng URL ảnh** trong Excel
- **Phân cách nhiều URL** bằng dấu phẩy

### 4. Error Handling

- **Kiểm tra response** để biết kết quả
- **Sử dụng file lỗi** được tạo tự động
- **Sửa lỗi và import lại** nếu cần

## 🐛 Troubleshooting

### Lỗi thường gặp:

| Lỗi                                | Nguyên nhân                 | Giải pháp                        |
| ---------------------------------- | --------------------------- | -------------------------------- |
| "Product code không được để trống" | Thiếu mã sản phẩm           | Điền mã sản phẩm vào cột A       |
| "Product name không được để trống" | Thiếu tên sản phẩm          | Điền tên sản phẩm vào cột B      |
| "Price phải lớn hơn 0"             | Giá <= 0 hoặc không phải số | Kiểm tra giá trong cột H         |
| "Size không hợp lệ"                | Size không đúng enum        | Sử dụng size từ danh sách hợp lệ |
| "File không đúng định dạng"        | File không phải .xlsx       | Chuyển đổi file sang .xlsx       |

### Debug Tips:

1. **Kiểm tra response error** để biết chi tiết lỗi
2. **Sử dụng file lỗi** được tạo tự động
3. **Test từng dòng** riêng lẻ
4. **Xem log server** để debug

## 📈 Performance

### Tối ưu hóa:

- **Batch processing** cho file lớn
- **Memory efficient** Excel reading
- **Transaction management** cho data consistency
- **Error isolation** (lỗi 1 dòng không ảnh hưởng dòng khác)

### Giới hạn:

- **File size**: Khuyến nghị < 10MB
- **Number of rows**: Khuyến nghị < 1000 rows
- **Image URLs**: Khuyến nghị < 10 URLs per row

## 🔮 Roadmap

### Tính năng sắp tới:

- [ ] **Export dữ liệu hiện tại** ra Excel
- [ ] **Dashboard thống kê** import
- [ ] **Validation nâng cao** (regex, format check)
- [ ] **Support CSV format**
- [ ] **Bulk update** sản phẩm hiện có
- [ ] **Template customization** theo category
- [ ] **Auto-generate Product Code** từ tên sản phẩm
- [ ] **Bulk variant management** (thêm/xóa variants cho sản phẩm hiện có)

### Cải tiến:

- [ ] **Performance optimization** cho file lớn
- [ ] **Real-time progress** tracking
- [ ] **Background processing** cho file lớn
- [ ] **Email notification** khi import hoàn thành

## 📞 Support

Nếu gặp vấn đề, vui lòng:

1. Kiểm tra **tài liệu hướng dẫn** này
2. Xem **file lỗi** được tạo tự động
3. Kiểm tra **log server** để debug
4. Liên hệ **development team** nếu cần hỗ trợ

---

**🎉 Chúc bạn sử dụng tính năng bulk import hiệu quả!**
