# Test Cases cho VNPay - Các Trường Hợp Lỗi

## Tổng quan về Response Codes

VNPay trả về các mã phản hồi khác nhau để thông báo kết quả giao dịch. Dưới đây là các trường hợp chính:

### ✅ Thành công

- **00**: Giao dịch thành công

### ❌ Các lỗi thẻ

- **09**: Thẻ/Tài khoản bị khóa
- **65**: Tài khoản không đủ số dư
- **79**: Nhập sai mật khẩu thanh toán quá số lần quy định

### ❌ Các lỗi khác

- **01**: Giao dịch chưa hoàn tất
- **02**: Giao dịch bị lỗi
- **04**: Giao dịch đảo (đã trừ tiền nhưng chưa thành công)
- **05**: VNPAY đang xử lý
- **06**: VNPAY đã gửi yêu cầu hoàn tiền
- **07**: Giao dịch bị nghi ngờ gian lận
- **13**: Nhập sai OTP
- **75**: Ngân hàng thanh toán đang bảo trì
- **99**: Các lỗi khác

## Test Cases Chi Tiết

### Test Case 1: Thanh toán thành công

**Mục tiêu**: Kiểm tra quy trình thanh toán thành công

**Bước thực hiện**:

1. Tạo đơn hàng với `paymentMethod: "VNPAY"`
2. Gọi API tạo URL thanh toán
3. Thanh toán với thông tin hợp lệ
4. Verify callback trả về `responseCode: "00"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "responseCode": "00",
    "responseMessage": "Giao dịch thành công",
    "success": true,
    "orderId": "VNSPXABC123",
    "amount": 100000,
    "bankCode": "NCB",
    "transactionNo": "12345678"
  }
}
```

### Test Case 2: Thẻ không đủ số dư

**Mục tiêu**: Kiểm tra xử lý khi thẻ không đủ tiền

**Bước thực hiện**:

1. Tạo đơn hàng với số tiền lớn
2. Thanh toán với thẻ test có số dư thấp
3. Verify callback trả về `responseCode: "65"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thất bại",
  "data": {
    "responseCode": "65",
    "responseMessage": "Giao dịch không thành công do: Tài khoản không đủ số dư",
    "success": false,
    "errorCode": "65",
    "errorMessage": "Giao dịch không thành công do: Tài khoản không đủ số dư"
  }
}
```

### Test Case 3: Thẻ bị khóa

**Mục tiêu**: Kiểm tra xử lý khi thẻ bị khóa

**Bước thực hiện**:

1. Sử dụng thẻ test bị khóa
2. Verify callback trả về `responseCode: "09"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thất bại",
  "data": {
    "responseCode": "09",
    "responseMessage": "Giao dịch không thành công do: Thẻ/Tài khoản bị khóa",
    "success": false,
    "errorCode": "09",
    "errorMessage": "Giao dịch không thành công do: Thẻ/Tài khoản bị khóa"
  }
}
```

### Test Case 4: Thẻ chưa kích hoạt

**Mục tiêu**: Kiểm tra xử lý khi thẻ chưa kích hoạt

**Bước thực hiện**:

1. Sử dụng thẻ test chưa kích hoạt
2. Verify callback trả về `responseCode: "09"`

**Expected Result**: Tương tự Test Case 3

### Test Case 5: Thẻ bị hết hạn

**Mục tiêu**: Kiểm tra xử lý khi thẻ hết hạn

**Bước thực hiện**:

1. Sử dụng thẻ test hết hạn
2. Verify callback trả về `responseCode: "09"`

**Expected Result**: Tương tự Test Case 3

### Test Case 6: Nhập sai OTP

**Mục tiêu**: Kiểm tra xử lý khi nhập sai OTP

**Bước thực hiện**:

1. Nhập sai OTP nhiều lần
2. Verify callback trả về `responseCode: "13"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thất bại",
  "data": {
    "responseCode": "13",
    "responseMessage": "Giao dịch không thành công do: Nhập sai mật khẩu xác thực giao dịch (OTP)",
    "success": false,
    "errorCode": "13",
    "errorMessage": "Giao dịch không thành công do: Nhập sai mật khẩu xác thực giao dịch (OTP)"
  }
}
```

### Test Case 7: Nhập sai mật khẩu quá số lần

**Mục tiêu**: Kiểm tra xử lý khi nhập sai mật khẩu quá số lần

**Bước thực hiện**:

1. Nhập sai mật khẩu nhiều lần
2. Verify callback trả về `responseCode: "79"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thất bại",
  "data": {
    "responseCode": "79",
    "responseMessage": "Giao dịch không thành công do: Nhập sai mật khẩu thanh toán quá số lần quy định",
    "success": false,
    "errorCode": "79",
    "errorMessage": "Giao dịch không thành công do: Nhập sai mật khẩu thanh toán quá số lần quy định"
  }
}
```

### Test Case 8: Ngân hàng bảo trì

**Mục tiêu**: Kiểm tra xử lý khi ngân hàng bảo trì

**Bước thực hiện**:

1. Chọn ngân hàng đang bảo trì
2. Verify callback trả về `responseCode: "75"`

**Expected Result**:

```json
{
  "success": true,
  "message": "Thanh toán thất bại",
  "data": {
    "responseCode": "75",
    "responseMessage": "Ngân hàng thanh toán đang bảo trì",
    "success": false,
    "errorCode": "75",
    "errorMessage": "Ngân hàng thanh toán đang bảo trì"
  }
}
```

### Test Case 9: Invalid Signature

**Mục tiêu**: Kiểm tra xử lý khi chữ ký không hợp lệ

**Bước thực hiện**:

1. Giả mạo callback với signature sai
2. Verify callback bị từ chối

**Expected Result**:

```json
{
  "success": false,
  "message": "Chữ ký không hợp lệ",
  "data": null
}
```

### Test Case 10: Amount Mismatch

**Mục tiêu**: Kiểm tra xử lý khi số tiền không khớp

**Bước thực hiện**:

1. Giả mạo callback với số tiền khác
2. Verify callback bị từ chối

**Expected Result**:

```json
{
  "success": false,
  "message": "Lỗi xử lý callback: Số tiền không khớp!",
  "data": null
}
```

## Cách Test trong Postman

### 1. Tạo đơn hàng với VNPay

```http
POST /api/orders/place-order
Content-Type: application/json
Authorization: Bearer {token}

{
  "userId": 1,
  "paymentMethod": "VNPAY",
  "shippingMethod": "STANDARD",
  "discountCodes": []
}
```

### 2. Tạo URL thanh toán

```http
POST /api/vnpay/create-payment-url/{orderId}
Authorization: Bearer {token}
```

### 3. Test callback (Simulate)

```http
GET /api/vnpay/payment-callback?vnp_ResponseCode=65&vnp_TxnRef=VNSPXABC123&vnp_Amount=1000000&vnp_TransactionNo=12345678&vnp_SecureHash=abc123
```

## Monitoring và Logging

### Logs cần theo dõi:

1. **Payment URL Creation**: Log khi tạo URL thanh toán
2. **Callback Processing**: Log khi xử lý callback
3. **Signature Verification**: Log kết quả verify signature
4. **Order Status Update**: Log khi cập nhật trạng thái đơn hàng
5. **Error Handling**: Log các lỗi xảy ra

### Metrics cần track:

- Tỷ lệ thanh toán thành công
- Tỷ lệ các loại lỗi
- Thời gian xử lý callback
- Số lượng giao dịch theo ngân hàng

## Troubleshooting

### Lỗi thường gặp:

1. **Callback không được gọi**: Kiểm tra Return URL
2. **Invalid signature**: Kiểm tra HashSecret
3. **Amount mismatch**: Kiểm tra format số tiền
4. **Order not found**: Kiểm tra orderCode

### Debug steps:

1. Kiểm tra logs
2. Verify cấu hình VNPay
3. Test với sandbox
4. Kiểm tra database
