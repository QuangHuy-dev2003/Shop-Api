# Hệ Thống Phân Quyền API

## Tổng Quan

Hệ thống sử dụng JWT token để xác thực và phân quyền dựa trên Role và Permission.

## Các Role Hiện Tại

1. **SUPER_ADMIN** (ID: 1) - Vai trò siêu quản trị viên

   - Có tất cả quyền trong hệ thống
   - Không cần kiểm tra permission cụ thể

2. **USER** (ID: 2) - Vai trò người dùng
   - Quyền hạn chế, chỉ được thực hiện các thao tác cơ bản
   - Cần có permission cụ thể để truy cập API

## Cách Sử Dụng

### 1. Đăng Nhập và Lấy Token

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "roleId": 1
    }
  }
}
```

### 2. Sử Dụng Token Cho API Cần Quyền

```bash
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Phân Loại API

### 🔓 API Public (Không Cần Token)

- **Auth APIs**: `/api/v1/auth/**`

  - Đăng ký, đăng nhập, refresh token, quên mật khẩu

- **OTP APIs**: `/api/v1/otp/**`

  - Tạo, xác minh OTP

- **Product APIs** (Chỉ xem):

  - `GET /api/v1/products` - Danh sách sản phẩm
  - `GET /api/v1/products/{id}` - Chi tiết sản phẩm
  - `GET /api/v1/download-excel-template` - Tải template Excel

- **Category APIs** (Chỉ xem):

  - `GET /api/v1/categories` - Danh sách danh mục
  - `GET /api/v1/categories/{id}` - Chi tiết danh mục

- **Brand APIs** (Chỉ xem):

  - `GET /api/v1/brands` - Danh sách thương hiệu
  - `GET /api/v1/brands/{id}` - Chi tiết thương hiệu

- **Discount APIs** (Chỉ xem):
  - `GET /api/v1/discounts` - Danh sách mã giảm giá
  - `GET /api/v1/discounts/{id}` - Chi tiết mã giảm giá
  - `GET /api/v1/discounts/code/{code}` - Tìm theo mã
  - `POST /api/v1/discounts/validate` - Validate mã giảm giá

### 🔒 API Cần Token và Phân Quyền

#### Chỉ SUPER_ADMIN (roleId = 1)

- **User Management**:

  - `GET /api/v1/users` - Xem danh sách user
  - `POST /api/v1/users` - Tạo user mới
  - `PUT /api/v1/users/{id}` - Cập nhật user
  - `DELETE /api/v1/users/{id}` - Xóa user

- **Product Management**:

  - `POST /api/v1/products` - Tạo sản phẩm mới
  - `PUT /api/v1/products/{id}` - Cập nhật sản phẩm
  - `DELETE /api/v1/products/{id}` - Xóa sản phẩm
  - `POST /api/v1/import-products-from-excel` - Import Excel
  - `POST /api/v1/upload-images` - Upload ảnh

- **Category Management**:

  - `POST /api/v1/categories` - Tạo danh mục
  - `PUT /api/v1/categories/{id}` - Cập nhật danh mục
  - `DELETE /api/v1/categories/{id}` - Xóa danh mục

- **Brand Management**:

  - `POST /api/v1/brands` - Tạo thương hiệu
  - `PUT /api/v1/brands/{id}` - Cập nhật thương hiệu
  - `DELETE /api/v1/brands/{id}` - Xóa thương hiệu

- **Order Management**:

  - `GET /api/v1/orders` - Xem tất cả đơn hàng (admin)
  - `PUT /api/v1/orders/{id}` - Cập nhật đơn hàng
  - `DELETE /api/v1/orders/{id}` - Xóa đơn hàng
  - `POST /api/v1/orders/{id}/cancel/admin` - Hủy đơn hàng (admin)

- **Discount Management**:

  - `POST /api/v1/discounts` - Tạo mã giảm giá
  - `PUT /api/v1/discounts/{id}` - Cập nhật mã giảm giá
  - `DELETE /api/v1/discounts/{id}` - Xóa mã giảm giá
  - `PATCH /api/v1/discounts/{id}/toggle` - Bật/tắt mã giảm giá

- **Role Management**:
  - Tất cả API `/api/v1/roles/**`

#### USER (roleId = 2) - Quyền Hạn Chế

- **Cart Operations**:

  - `GET /api/v1/cart/{userId}` - Xem giỏ hàng của mình
  - `POST /api/v1/cart/{userId}/items` - Thêm sản phẩm vào giỏ
  - `PUT /api/v1/cart/{userId}/items` - Cập nhật giỏ hàng
  - `DELETE /api/v1/cart/{userId}/items` - Xóa sản phẩm khỏi giỏ

- **Order Operations**:
  - `POST /api/v1/orders/place-order` - Đặt hàng
  - `GET /api/v1/orders/user/{userId}` - Xem đơn hàng của mình
  - `POST /api/v1/orders/{id}/cancel/user` - Hủy đơn hàng của mình

## Cấu Trúc Permission

Hệ thống sử dụng các permission sau:

- `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`
- `PRODUCT_CREATE`, `PRODUCT_UPDATE`, `PRODUCT_DELETE`, `PRODUCT_UPLOAD`
- `CATEGORY_CREATE`, `CATEGORY_UPDATE`, `CATEGORY_DELETE`
- `BRAND_CREATE`, `BRAND_UPDATE`, `BRAND_DELETE`
- `ORDER_READ_ALL`, `ORDER_MANAGE`, `ORDER_CANCEL_ADMIN`
- `DISCOUNT_CREATE`, `DISCOUNT_UPDATE`, `DISCOUNT_DELETE`, `DISCOUNT_TOGGLE`
- `ROLE_MANAGE`
- `CART_ACCESS`

## Lỗi Thường Gặp

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Token không hợp lệ",
  "data": null
}
```

**Nguyên nhân**: Token không tồn tại, sai format, hoặc hết hạn
**Giải pháp**: Đăng nhập lại để lấy token mới

### 403 Forbidden

```json
{
  "success": false,
  "message": "Không có quyền truy cập",
  "data": null
}
```

**Nguyên nhân**: User không có quyền truy cập API
**Giải pháp**: Kiểm tra role và permission của user

## Ví Dụ Sử Dụng

### 1. SUPER_ADMIN Tạo Sản Phẩm Mới

```bash
POST /api/v1/products
Authorization: Bearer <super_admin_token>
Content-Type: multipart/form-data

product: {"name": "Sản phẩm mới", "price": 100000}
images: [file1.jpg, file2.jpg]
```

### 2. USER Đặt Hàng

```bash
POST /api/v1/orders/place-order
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "userId": 2,
  "shippingAddressId": 1,
  "paymentMethod": "CASH"
}
```

### 3. USER Xem Giỏ Hàng Của Mình

```bash
GET /api/v1/cart/2
Authorization: Bearer <user_token>
```

## Lưu Ý

1. **Token phải được gửi trong header**: `Authorization: Bearer <token>`
2. **SUPER_ADMIN có tất cả quyền** - không cần kiểm tra permission cụ thể
3. **USER chỉ có quyền hạn chế** - cần có permission phù hợp
4. **API public không cần token** - có thể truy cập trực tiếp
5. **Token có thời hạn** - cần refresh khi hết hạn
