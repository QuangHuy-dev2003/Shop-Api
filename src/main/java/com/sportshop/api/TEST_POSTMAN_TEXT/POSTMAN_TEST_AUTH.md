# POSTMAN TEST - AUTHENTICATION API

## Base URL

```
http://localhost:8080/api/auth
```

## 1. Đăng ký tài khoản mới

**POST** `/register`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@gmail.com",
  "password": "123456",
  "phone": "0123456789",
  "gender": "MALE"
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "message": "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
    "email": "nguyenvana@gmail.com",
    "userId": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 2. Đăng nhập

**POST** `/login`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "email": "nguyenvana@gmail.com",
  "password": "123456"
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "nguyenvana@gmail.com",
      "phone": "0123456789",
      "gender": "MALE",
      "avatar": null,
      "firstLogin": false
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 3. Xác thực OTP

**POST** `/verify-otp`

**Headers:**

```
Content-Type: application/json
```

**Body:**

```json
{
  "email": "nguyenvana@gmail.com",
  "otpCode": "123456"
}
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Xác thực thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "nguyenvana@gmail.com",
      "phone": "0123456789",
      "gender": "MALE",
      "avatar": null,
      "firstLogin": false
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 4. Gửi lại OTP

**POST** `/resend-otp?email=nguyenvana@gmail.com`

**Headers:**

```
Content-Type: application/json
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đã gửi lại mã OTP",
  "data": {
    "message": "Đã gửi lại mã OTP! Vui lòng kiểm tra email.",
    "email": "nguyenvana@gmail.com",
    "userId": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 5. Refresh Token

**POST** `/refresh-token?refreshToken=eyJhbGciOiJIUzI1NiJ9...`

**Headers:**

```
Content-Type: application/json
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Refresh token thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "nguyenvana@gmail.com",
      "phone": "0123456789",
      "gender": "MALE",
      "avatar": null,
      "firstLogin": false
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 6. Đăng xuất

**POST** `/logout?refreshToken=eyJhbGciOiJIUzI1NiJ9...`

**Headers:**

```
Content-Type: application/json
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

## 7. Kiểm tra trạng thái API

**GET** `/check-status`

**Response thành công:**

```json
{
  "success": true,
  "message": "API hoạt động bình thường",
  "data": "Auth service is running",
  "timestamp": "2024-01-01T10:00:00"
}
```

## 8. Quên mật khẩu (Forgot Password)

### 8.1. Gửi OTP đặt lại mật khẩu

**POST** `/forgot-password/request`

**Headers:**

```
Content-Type: application/x-www-form-urlencoded
```

**Body (form-data hoặc x-www-form-urlencoded):**

- `email`: Email đã đăng ký

**Ví dụ:**

```
email=nguyenvana@gmail.com
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đã gửi mã OTP đặt lại mật khẩu",
  "data": {
    "message": "Đã gửi mã OTP đặt lại mật khẩu! Vui lòng kiểm tra email.",
    "email": "nguyenvana@gmail.com",
    "userId": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 8.2. Xác thực OTP và đổi mật khẩu mới

**POST** `/forgot-password/verify`

**Headers:**

```
Content-Type: application/x-www-form-urlencoded
```

**Body (form-data hoặc x-www-form-urlencoded):**

- `email`: Email đã đăng ký
- `otpCode`: Mã OTP nhận được qua email
- `newPassword`: Mật khẩu mới

**Ví dụ:**

```
email=nguyenvana@gmail.com&otpCode=123456&newPassword=matkhaumoi123
```

**Response thành công:**

```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": {
    "message": "Đổi mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.",
    "email": "nguyenvana@gmail.com",
    "userId": 1
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

**Lưu ý:**

- OTP dùng cho xác thực quên mật khẩu có hiệu lực như OTP đăng ký.
- Sau khi đổi mật khẩu thành công, bạn có thể đăng nhập lại bằng mật khẩu mới.

## Lưu ý quan trọng:

### 1. Quy trình đăng ký và kích hoạt:

1. Gọi API `/register` để đăng ký
2. Kiểm tra email để lấy mã OTP
3. Gọi API `/verify-otp` để xác thực và kích hoạt tài khoản
4. Sau khi kích hoạt thành công, có thể đăng nhập bằng API `/login`

### 2. Validation:

- **Email**: Phải là email hợp lệ
- **Password**: Tối thiểu 6 ký tự
- **Phone**: 10-11 số
- **Gender**: MALE, FEMALE, hoặc OTHER
- **FullName**: 2-100 ký tự

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

- Email đã tồn tại
- Số điện thoại đã tồn tại
- Mã OTP không hợp lệ hoặc hết hạn
- Tài khoản chưa được kích hoạt
- Email hoặc mật khẩu không đúng
- Refresh token không hợp lệ hoặc hết hạn

### 5. Environment Variables cần thiết:

```
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
JWT_SECRET=your-jwt-secret-key
```

### 6. Sử dụng Access Token:

Sau khi đăng nhập thành công, sử dụng access token trong header Authorization:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
