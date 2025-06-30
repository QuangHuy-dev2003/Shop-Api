# Hướng dẫn Test API User với Postman

## 1. Tạo User (POST /api/v1/users)

### 1.1. Tạo User không có địa chỉ và avatar (JSON)

**URL:** `POST http://localhost:8080/api/v1/users`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@gmail.com",
  "password": "123456",
  "phone": "0123456789"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Tạo user thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@gmail.com",
    "phone": "0123456789",
    "roleId": 1,
    "firstLogin": true,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": null,
    "shippingAddresses": []
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.2. Tạo User có avatar file (Multipart Form Data)

**URL:** `POST http://localhost:8080/api/v1/users`

**Headers:**

```
Content-Type: multipart/form-data
```

**Body (form-data):**

```
user: {
  "fullName": "Trần Thị B",
  "email": "tranthib@gmail.com",
  "password": "123456",
  "phone": "0987654321",
  "roleId": 1
}
avatar: [Chọn file ảnh từ máy tính]
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Tạo user thành công",
  "data": {
    "id": 2,
    "fullName": "Trần Thị B",
    "email": "tranthib@gmail.com",
    "phone": "0987654321",
    "roleId": 1,
    "firstLogin": true,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/avatars/abc123.jpg",
    "shippingAddresses": []
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.3. Tạo User có địa chỉ và avatar file (Multipart Form Data)

**URL:** `POST http://localhost:8080/api/v1/users`

**Headers:**

```
Content-Type: multipart/form-data
```

**Body (form-data):**

```
user: {
  "fullName": "Lê Văn C",
  "email": "levanc@gmail.com",
  "password": "123456",
  "phone": "0555666777",
  "roleId": 1,
  "addressLine": "123 Đường ABC",
  "ward": "Phường 1",
  "district": "Quận 1",
  "province": "TP.HCM"
}
avatar: [Chọn file ảnh từ máy tính]
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Tạo user thành công",
  "data": {
    "id": 3,
    "fullName": "Lê Văn C",
    "email": "levanc@gmail.com",
    "phone": "0555666777",
    "roleId": 1,
    "firstLogin": true,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/avatars/def456.jpg",
    "shippingAddresses": [
      {
        "id": 1,
        "addressLine": "123 Đường ABC",
        "ward": "Phường 1",
        "district": "Quận 1",
        "province": "TP.HCM",
        "isDefault": true
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.4. Tạo User có địa chỉ (không có avatar) - JSON

**URL:** `POST http://localhost:8080/api/v1/users`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "fullName": "Phạm Thị D",
  "email": "phamthid@gmail.com",
  "password": "123456",
  "phone": "0111222333",
  "roleId": 1,
  "addressLine": "456 Đường XYZ",
  "ward": "Phường 2",
  "district": "Quận 2",
  "province": "TP.HCM"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Tạo user thành công",
  "data": {
    "id": 4,
    "fullName": "Phạm Thị D",
    "email": "phamthid@gmail.com",
    "phone": "0111222333",
    "roleId": 1,
    "firstLogin": true,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": null,
    "shippingAddresses": [
      {
        "id": 2,
        "addressLine": "456 Đường XYZ",
        "ward": "Phường 2",
        "district": "Quận 2",
        "province": "TP.HCM",
        "isDefault": true
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## 2. Upload Avatar (POST /api/v1/users/{id}/avatar)

### 2.1. Upload Avatar

**URL:** `POST http://localhost:8080/api/v1/users/1/avatar`

**Headers:**

```
Content-Type: multipart/form-data
```

**Body (form-data):**

```
file: [Chọn file ảnh từ máy tính]
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Upload avatar thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@gmail.com",
    "phone": "0123456789",
    "roleId": 1,
    "firstLogin": true,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/avatars/abc123.jpg",
    "shippingAddresses": []
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## 3. Cập nhật User (PUT /api/v1/users/{id})

### 3.1. Cập nhật thông tin cơ bản

**URL:** `PUT http://localhost:8080/api/v1/users/1`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "fullName": "Nguyễn Văn A (Đã cập nhật)",
  "phone": "0111222333",
  "firstLogin": false
}
```

### 3.2. Cập nhật với avatar URL

**URL:** `PUT http://localhost:8080/api/v1/users/1`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "fullName": "Nguyễn Văn A",
  "avatar": "https://example.com/avatar.jpg"
}
```

### 3.3. Cập nhật với địa chỉ (TÍCH HỢP TỰ ĐỘNG)

**URL:** `PUT http://localhost:8080/api/v1/users/1`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "fullName": "Nguyễn Văn A",
  "addressLine": "456 Đường XYZ",
  "ward": "Phường 2",
  "district": "Quận 2",
  "province": "TP.HCM"
}
```

**Lưu ý:** Nếu user chưa có địa chỉ, sẽ tạo địa chỉ mới. Nếu đã có địa chỉ mặc định, sẽ cập nhật địa chỉ đó.

## 4. Lấy thông tin User (GET /api/v1/users/{id})

### 4.1. Lấy user theo ID

**URL:** `GET http://localhost:8080/api/v1/users/1`

**Expected Response:**

```json
{
  "success": true,
  "message": "Lấy thông tin user thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@gmail.com",
    "phone": "0123456789",
    "roleId": 1,
    "firstLogin": false,
    "createdAt": "2024-01-15T10:30:00",
    "provider": "DEFAULT",
    "avatar": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/avatars/abc123.jpg",
    "shippingAddresses": [
      {
        "id": 1,
        "addressLine": "456 Đường XYZ",
        "ward": "Phường 2",
        "district": "Quận 2",
        "province": "TP.HCM",
        "isDefault": true
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## 5. Quản lý Địa chỉ (ShippingAddressController)

### 5.1. Lấy danh sách địa chỉ

**URL:** `GET http://localhost:8080/api/v1/users/2/shipping-addresses`

### 5.2. Tạo địa chỉ mới

**URL:** `POST http://localhost:8080/api/v1/users/1/shipping-addresses`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "addressLine": "789 Đường DEF",
  "ward": "Phường 3",
  "district": "Quận 3",
  "province": "TP.HCM",
  "isDefault": true
}
```

### 5.3. Cập nhật địa chỉ

**URL:** `PUT http://localhost:8080/api/v1/users/1/shipping-addresses/1`

**Headers:**

```
Content-Type: application/json
```

**Body (JSON):**

```json
{
  "addressLine": "789 Đường DEF (Đã cập nhật)",
  "ward": "Phường 3",
  "district": "Quận 3",
  "province": "TP.HCM"
}
```

### 5.4. Đặt địa chỉ làm mặc định

**URL:** `PUT http://localhost:8080/api/v1/users/1/shipping-addresses/1/set-default`

### 5.5. Xóa địa chỉ

**URL:** `DELETE http://localhost:8080/api/v1/users/1/shipping-addresses/1`

## 6. Kiểm tra Email

### 6.1. Kiểm tra email tồn tại

**URL:** `GET http://localhost:8080/api/v1/users/check-email/nguyenvana@gmail.com`

**Expected Response:**

```json
{
  "success": true,
  "message": "Kiểm tra email thành công",
  "data": true,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 6.2. Tìm user theo email

**URL:** `GET http://localhost:8080/api/v1/users/email/nguyenvana@gmail.com`

## 7. Xóa User (DELETE /api/v1/users/{id})

### 7.1. Xóa user (TỰ ĐỘNG XÓA ĐỊA CHỈ)

**URL:** `DELETE http://localhost:8080/api/v1/users/1`

**Expected Response:**

```json
{
  "success": true,
  "message": "Xóa user thành công",
  "data": "Xóa user thành công",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Lưu ý:** Khi xóa user, hệ thống sẽ tự động:

- Xóa avatar trên Cloudinary (nếu có)
- Xóa tất cả địa chỉ của user
- Xóa user

## 8. Lấy danh sách User (GET /api/v1/users)

### 8.1. Lấy tất cả user

**URL:** `GET http://localhost:8080/api/v1/users`

## Các trường hợp lỗi cần test:

### 1. Email đã tồn tại

```json
{
  "success": false,
  "message": "Email đã tồn tại trong hệ thống",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. Số điện thoại đã tồn tại

```json
{
  "success": false,
  "message": "Số điện thoại đã tồn tại trong hệ thống",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. Validation lỗi

```json
{
  "success": false,
  "message": "fullName: Họ tên không được để trống",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 4. User không tồn tại

```json
{
  "success": false,
  "message": "Không tìm thấy user với ID: 999",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

## Lưu ý khi test:

1. **File upload**: Khi test upload avatar, chọn file ảnh có định dạng: JPG, PNG, GIF, WebP
2. **Kích thước file**: Tối đa 5MB
3. **Email unique**: Mỗi email chỉ được sử dụng một lần
4. **Phone unique**: Mỗi số điện thoại chỉ được sử dụng một lần (nếu có)
5. **Địa chỉ mặc định**: Chỉ có một địa chỉ mặc định per user
6. **Password**: Được mã hóa bằng BCrypt
7. **Avatar**: Tự động xóa ảnh cũ trên Cloudinary khi upload ảnh mới
8. **Tích hợp địa chỉ**: Khi tạo/cập nhật user, nếu có thông tin địa chỉ sẽ tự động tạo/cập nhật địa chỉ
9. **Xóa user**: Tự động xóa tất cả địa chỉ và avatar khi xóa user
