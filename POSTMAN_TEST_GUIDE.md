# Hướng dẫn Test API trong Postman

## 1. Test Brand API

### 1.1 Lấy tất cả thương hiệu

```http
GET http://localhost:8080/api/v1/brands
```

### 1.2 Lấy thương hiệu theo ID

```http
GET http://localhost:8080/api/v1/brands/1
```

### 1.3 Tạo thương hiệu mới (không có logo)

```http
POST http://localhost:8080/api/v1/brands
Content-Type: application/json

{
  "name": "Nike",
  "description": "Thương hiệu thể thao hàng đầu thế giới"
}
```

### 1.4 Tạo thương hiệu với logo

```http
POST http://localhost:8080/api/v1/brands/with-logo
Content-Type: multipart/form-data

brand: {"name":"Adidas","description":"Thương hiệu thể thao Đức"}
logo: [file]
```

### 1.5 Cập nhật thương hiệu

```http
PUT http://localhost:8080/api/v1/brands/1
Content-Type: application/json

{
  "name": "Nike Updated",
  "description": "Mô tả cập nhật cho Nike"
}
```

### 1.6 Xóa thương hiệu

```http
DELETE http://localhost:8080/api/v1/brands/1
```

## 2. Test Category API

### 2.1 Lấy tất cả danh mục

```http
GET http://localhost:8080/api/v1/categories
```

### 2.2 Lấy danh mục theo ID

```http
GET http://localhost:8080/api/v1/categories/1
```

### 2.3 Tạo danh mục mới

```http
POST http://localhost:8080/api/v1/categories
Content-Type: application/json

{
  "name": "Áo thun",
  "description": "Danh mục áo thun thể thao và casual"
}
```

### 2.4 Cập nhật danh mục

```http
PUT http://localhost:8080/api/v1/categories/1
Content-Type: application/json

{
  "name": "Áo thun Updated",
  "description": "Mô tả cập nhật cho danh mục áo thun"
}
```

### 2.5 Xóa danh mục

```http
DELETE http://localhost:8080/api/v1/categories/1
```

## 3. Test Product API

### 3.1 Tạo sản phẩm cơ bản

```http
POST http://localhost:8080/api/v1/products
Content-Type: application/json

{
  "name": "Áo thun Nike Dri-FIT",
  "description": "Áo thun thể thao Nike Dri-FIT",
  "price": 450000,
  "sale": 15,
  "categoryId": 1,
  "brandId": 1,
  "imageUrl": "https://example.com/image.jpg",
  "additionalImages": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "variants": [
    {
      "size": "S",
      "stockQuantity": 25,
      "price": 450000
    },
    {
      "size": "M",
      "stockQuantity": 40,
      "price": 450000
    }
  ]
}
```

### 3.2 Tạo sản phẩm với upload ảnh

```http
POST http://localhost:8080/api/v1/products/with-images
Content-Type: multipart/form-data

product: {"name":"Áo thun Nike","price":450000,"categoryId":1,"brandId":1}
mainImage: [file]
additionalImages: [files]
```

## 4. Dữ liệu mẫu để test

### 4.1 Tạo Categories trước

```json
// Category 1
{
  "name": "Áo thun",
  "description": "Danh mục áo thun thể thao"
}

// Category 2
{
  "name": "Quần",
  "description": "Danh mục quần thể thao"
}

// Category 3
{
  "name": "Giày",
  "description": "Danh mục giày thể thao"
}
```

### 4.2 Tạo Brands trước

```json
// Brand 1
{
  "name": "Nike",
  "description": "Thương hiệu thể thao hàng đầu thế giới"
}

// Brand 2
{
  "name": "Adidas",
  "description": "Thương hiệu thể thao Đức"
}

// Brand 3
{
  "name": "Puma",
  "description": "Thương hiệu thể thao Đức"
}
```

## 5. Response mẫu

### 5.1 Success Response

```json
{
  "success": true,
  "message": "Tạo thương hiệu thành công",
  "data": {
    "id": 1,
    "name": "Nike",
    "description": "Thương hiệu thể thao hàng đầu thế giới",
    "logoUrl": null,
    "isActive": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 5.2 Error Response

```json
{
  "success": false,
  "message": "Tên thương hiệu không được để trống",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

## 6. Lưu ý khi test

1. **Thứ tự test**: Tạo Category và Brand trước, sau đó mới tạo Product
2. **Validation**: Tất cả API đều có validation, kiểm tra message lỗi
3. **File upload**: Sử dụng form-data cho upload ảnh
4. **Soft delete**: Brand sử dụng soft delete (isActive = false)
5. **Hard delete**: Category sử dụng hard delete
6. **Unique constraint**: Brand name phải unique
