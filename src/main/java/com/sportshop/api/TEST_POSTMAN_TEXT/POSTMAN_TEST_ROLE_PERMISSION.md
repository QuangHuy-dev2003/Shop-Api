# POSTMAN TEST - ROLE & PERMISSION API

## Base URL

```
http://localhost:8080/api/v1
```

## PERMISSIONS API

### 1. Tạo permission mới

**POST** `/permissions`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "name": "USER_READ",
  "description": "Quyền đọc thông tin user",
  "apiPath": "/api/v1/users",
  "method": "GET",
  "module": "User",
  "active": true
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Tạo quyền hạn thành công",
  "data": {
    "id": 1,
    "name": "USER_READ",
    "description": "Quyền đọc thông tin user",
    "apiPath": "/api/v1/users",
    "method": "GET",
    "module": "User",
    "active": true
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2. Lấy tất cả permissions

**GET** `/permissions`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy danh sách quyền hạn thành công",
  "data": [
    {
      "id": 1,
      "name": "USER_READ",
      "description": "Quyền đọc thông tin user",
      "apiPath": "/api/v1/users",
      "method": "GET",
      "module": "User",
      "active": true
    },
    {
      "id": 2,
      "name": "USER_WRITE",
      "description": "Quyền chỉnh sửa thông tin user",
      "apiPath": "/api/v1/users",
      "method": "PUT",
      "module": "User",
      "active": true
    }
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 3. Lấy permissions với phân trang

**GET** `/permissions/paginated?page=0&size=10`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy danh sách quyền hạn thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET",
        "module": "User",
        "active": true
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 4. Lấy permission theo ID

**GET** `/permissions/1`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy thông tin quyền hạn thành công",
  "data": {
    "id": 1,
    "name": "USER_READ",
    "description": "Quyền đọc thông tin user",
    "apiPath": "/api/v1/users",
    "method": "GET",
    "module": "User",
    "active": true
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 5. Tìm permissions theo tên

**GET** `/permissions/search?name=read`

**Response thành công:**

```json
{
  "success": true,
  "message": "Tìm kiếm quyền hạn thành công",
  "data": [
    {
      "id": 1,
      "name": "USER_READ",
      "description": "Quyền đọc thông tin user",
      "apiPath": "/api/v1/users",
      "method": "GET",
      "module": "User",
      "active": true
    }
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 6. Cập nhật permission

**PUT** `/permissions/1`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "name": "USER_READ_UPDATE",
  "description": "Quyền đọc và cập nhật thông tin user",
  "apiPath": "/api/v1/users",
  "method": "PUT",
  "module": "User",
  "active": false
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Cập nhật quyền hạn thành công",
  "data": {
    "id": 1,
    "name": "USER_READ_UPDATE",
    "description": "Quyền đọc và cập nhật thông tin user",
    "apiPath": "/api/v1/users",
    "method": "PUT",
    "module": "User",
    "active": false
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 7. Xóa permission

**DELETE** `/permissions/1`

**Response thành công:**

```json
{
  "success": true,
  "message": "Xóa quyền hạn thành công",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

## ROLES API

### 1. Tạo role mới

**POST** `/roles`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "name": "ADMIN",
  "description": "Vai trò quản trị viên",
  "permissionIds": [1, 2, 3]
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Tạo vai trò thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Vai trò quản trị viên",
    "permissions": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET"
      },
      {
        "id": 2,
        "name": "USER_WRITE",
        "description": "Quyền chỉnh sửa thông tin user",
        "apiPath": "/api/v1/users",
        "method": "PUT"
      }
    ]
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2. Lấy tất cả roles

**GET** `/roles`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy danh sách vai trò thành công",
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "description": "Vai trò quản trị viên",
      "permissions": [
        {
          "id": 1,
          "name": "USER_READ",
          "description": "Quyền đọc thông tin user",
          "apiPath": "/api/v1/users",
          "method": "GET"
        }
      ]
    }
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 3. Lấy roles với phân trang

**GET** `/roles/paginated?page=0&size=10`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy danh sách vai trò thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "ADMIN",
        "description": "Vai trò quản trị viên",
        "permissions": []
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 4. Lấy role theo ID

**GET** `/roles/1`

**Response thành công:**

```json
{
  "success": true,
  "message": "Lấy thông tin vai trò thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Vai trò quản trị viên",
    "permissions": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET"
      }
    ]
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 5. Tìm roles theo tên

**GET** `/roles/search?name=admin`

**Response thành công:**

```json
{
  "success": true,
  "message": "Tìm kiếm vai trò thành công",
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "description": "Vai trò quản trị viên",
      "permissions": []
    }
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 6. Cập nhật role

**PUT** `/roles/1`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "name": "SUPER_ADMIN",
  "description": "Vai trò siêu quản trị viên",
  "permissionIds": [1, 2, 3, 4]
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Cập nhật vai trò thành công",
  "data": {
    "id": 1,
    "name": "SUPER_ADMIN",
    "description": "Vai trò siêu quản trị viên",
    "permissions": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET"
      }
    ]
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 7. Xóa role

**DELETE** `/roles/1`

**Response thành công:**

```json
{
  "success": true,
  "message": "Xóa vai trò thành công",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

### 8. Gán permissions cho role

**POST** `/roles/1/permissions`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
[1, 2, 3]
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Gán quyền hạn cho vai trò thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Vai trò quản trị viên",
    "permissions": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET"
      },
      {
        "id": 2,
        "name": "USER_WRITE",
        "description": "Quyền chỉnh sửa thông tin user",
        "apiPath": "/api/v1/users",
        "method": "PUT"
      }
    ]
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 9. Xóa permissions khỏi role

**DELETE** `/roles/1/permissions`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
[2, 3]
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Xóa quyền hạn khỏi vai trò thành công",
  "data": {
    "id": 1,
    "name": "ADMIN",
    "description": "Vai trò quản trị viên",
    "permissions": [
      {
        "id": 1,
        "name": "USER_READ",
        "description": "Quyền đọc thông tin user",
        "apiPath": "/api/v1/users",
        "method": "GET"
      }
    ]
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## Lưu ý quan trọng:

### 1. Validation:

- **Name**: 2-100 ký tự, không được trùng
- **Description**: Tối đa 255 ký tự
- **PermissionIds**: Danh sách ID permissions hợp lệ

### 2. Quan hệ Role-Permission:

- Một role có thể có nhiều permissions
- Một permission có thể thuộc nhiều roles
- Quan hệ many-to-many

### 3. Error Responses:

```json
{
  "success": false,
  "message": "Thông báo lỗi cụ thể",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

### 4. Các lỗi thường gặp:

- Tên role/permission đã tồn tại
- Không tìm thấy role/permission với ID
- Permission ID không hợp lệ
- Validation errors

### 5. Thứ tự test:

1. Tạo permissions trước
2. Tạo roles với permissions
3. Test các chức năng CRUD
4. Test gán/xóa permissions cho roles

### 6. Dữ liệu mẫu:

**Permissions:**

- USER_READ: Quyền đọc thông tin user
- USER_WRITE: Quyền chỉnh sửa thông tin user
- USER_DELETE: Quyền xóa user
- PRODUCT_READ: Quyền đọc sản phẩm
- PRODUCT_WRITE: Quyền chỉnh sửa sản phẩm
- ORDER_READ: Quyền đọc đơn hàng
- ORDER_WRITE: Quyền chỉnh sửa đơn hàng

**Roles:**

- ADMIN: Vai trò quản trị viên
- USER: Vai trò người dùng thường
- MODERATOR: Vai trò điều hành
