# Test Google OAuth2 Flow

## 1. Kiểm tra cấu hình

### 1.1. Environment Variables

Đảm bảo các biến môi trường đã được cấu hình:

```bash
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
JWT_SECRET=your_jwt_secret
```

### 1.2. Google Cloud Console

- Redirect URI phải là: `http://localhost:8080/oauth2/authorization/google`
- Authorized JavaScript origins: `http://localhost:3000`

## 2. Test Flow

### 2.1. Lấy Google OAuth2 URL

```http
GET http://localhost:8080/api/v1/auth/google/login
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Redirect to Google OAuth2",
  "data": "http://localhost:8080/oauth2/authorization/google",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2.2. Đăng nhập Google

1. Mở URL: `http://localhost:8080/oauth2/authorization/google`
2. Đăng nhập Google và cho phép truy cập
3. Hệ thống sẽ tự động:
   - Lưu/cập nhật user vào database
   - Tạo access token và refresh token
   - Redirect về: `http://localhost:3000/oauth-callback?response=...`

### 2.3. Kiểm tra Database

Sau khi đăng nhập thành công, kiểm tra:

```sql
-- Kiểm tra user đã được tạo
SELECT * FROM users WHERE email = 'your_google_email@gmail.com';

-- Kiểm tra refresh token
SELECT * FROM refresh_tokens WHERE user_id = (SELECT id FROM users WHERE email = 'your_google_email@gmail.com');
```

## 3. Debug Logs

Khi chạy ứng dụng, theo dõi console logs:

### 3.1. CustomOAuth2UserService

```
=== CustomOAuth2UserService.loadUser ===
Email: user@gmail.com
Name: User Name
Picture: https://lh3.googleusercontent.com/...
Creating new user for Google OAuth2
New user created with ID: 1
```

### 3.2. CustomOAuth2SuccessHandler

```
=== OAuth2 Success Handler ===
Email: user@gmail.com
Name: User Name
Avatar: https://lh3.googleusercontent.com/...
User found: ID=1, Active=true
Auth response created successfully
Refresh token saved to database
Redirecting to: http://localhost:3000/oauth-callback?response=...
```

## 4. Troubleshooting

### 4.1. User không được tạo trong database

- Kiểm tra logs của `CustomOAuth2UserService`
- Kiểm tra kết nối database
- Kiểm tra quyền ghi vào database

### 4.2. Lỗi redirect

- Kiểm tra URL redirect trong Google Cloud Console
- Kiểm tra CORS configuration
- Kiểm tra frontend có handle `/oauth-callback` route không

### 4.3. Token không được tạo

- Kiểm tra logs của `CustomOAuth2SuccessHandler`
- Kiểm tra JWT configuration
- Kiểm tra RefreshTokenRepository

## 5. Frontend Integration

### 5.1. React Example

```javascript
// 1. Lấy Google OAuth2 URL
const getGoogleLoginUrl = async () => {
  const response = await fetch("/api/v1/auth/google/login");
  const data = await response.json();
  return data.data;
};

// 2. Redirect to Google
const loginWithGoogle = async () => {
  const googleUrl = await getGoogleLoginUrl();
  window.location.href = googleUrl;
};

// 3. Handle callback (trong component hoặc route)
const handleOAuthCallback = () => {
  const urlParams = new URLSearchParams(window.location.search);
  const response = urlParams.get("response");

  if (response) {
    try {
      const authData = JSON.parse(decodeURIComponent(response));
      if (authData.success) {
        // Lưu tokens
        localStorage.setItem("accessToken", authData.data.accessToken);
        localStorage.setItem("refreshToken", authData.data.refreshToken);

        // Lưu user info
        localStorage.setItem(
          "userInfo",
          JSON.stringify(authData.data.userInfo)
        );

        // Redirect to dashboard
        window.location.href = "/dashboard";
      }
    } catch (error) {
      console.error("Error parsing OAuth response:", error);
    }
  }
};
```

### 5.2. Vue Example

```javascript
// Tương tự React, nhưng sử dụng Vue router
// Trong route /oauth-callback
mounted() {
  this.handleOAuthCallback();
}
```

## 6. Expected Database State

Sau khi đăng nhập thành công:

### 6.1. Users Table

```sql
SELECT id, email, full_name, provider, active, first_login, avatar
FROM users
WHERE email = 'user@gmail.com';
```

**Expected:**

- `provider = 'GOOGLE'`
- `active = true`
- `first_login = false`
- `avatar` có URL từ Google

### 6.2. Refresh Tokens Table

```sql
SELECT id, user_id, token, expires_at, is_revoked, created_at
FROM refresh_tokens
WHERE user_id = (SELECT id FROM users WHERE email = 'user@gmail.com');
```

**Expected:**

- Có ít nhất 1 record
- `is_revoked = false`
- `expires_at` > current time
