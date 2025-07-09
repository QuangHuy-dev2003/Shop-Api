# Hướng dẫn Test VNPay Payment

## Cấu hình VNPay

### Thông tin cấu hình đã được thiết lập:

- **Terminal ID (vnp_TmnCode)**: PF5EE556
- **Secret Key (vnp_HashSecret)**: 1857VAOWOVCVOUBSBMQACRES1OBYR86X
- **URL thanh toán TEST**: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
- **Return URL**: http://localhost:8080/api/vnpay/payment-callback
- **Transaction Query URL**: https://sandbox.vnpayment.vn/merchant_webapi/api/transaction

## Các API Endpoints

### 1. Tạo URL thanh toán VNPay cho đơn hàng

**POST** `/api/vnpay/create-payment-url/{orderId}`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {your_jwt_token}
```

**Response:**

```json
{
  "success": true,
  "message": "Tạo URL thanh toán thành công",
  "data": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Version=2.1.0&vnp_Command=pay&vnp_TmnCode=PF5EE556&vnp_Amount=1000000&vnp_CurrCode=VND&vnp_BankCode=&vnp_TxnRef=VNSPXABC123&vnp_OrderInfo=Thanh%20toan%20don%20hang%20VNSPXABC123&vnp_OrderType=other&vnp_Locale=vn&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fvnpay%2Fpayment-callback&vnp_IpAddr=127.0.0.1&vnp_CreateDate=20241201120000&vnp_SecureHash=abc123...",
  "timestamp": "2024-12-01T12:00:00"
}
```

### 2. Callback từ VNPay (Tự động)

**GET** `/api/vnpay/payment-callback`

**Parameters từ VNPay:**

- `vnp_ResponseCode`: Mã phản hồi (00 = thành công)
- `vnp_TxnRef`: Mã đơn hàng
- `vnp_Amount`: Số tiền (đã nhân 100)
- `vnp_TransactionNo`: Mã giao dịch VNPay
- `vnp_SecureHash`: Chữ ký bảo mật

**Response:**

- Thành công: `Payment successful`
- Thất bại: `Payment failed`

### 3. Kiểm tra trạng thái giao dịch

**GET** `/api/vnpay/transaction-status/{txnRef}`

**Response:**

```json
{
  "success": true,
  "message": "Lấy trạng thái giao dịch thành công",
  "data": {
    "vnp_Version": "2.1.0",
    "vnp_Command": "querydr",
    "vnp_TmnCode": "PF5EE556",
    "vnp_TxnRef": "VNSPXABC123",
    "vnp_OrderInfo": "Kiem tra giao dich",
    "vnp_TransDate": "20241201120000"
  },
  "timestamp": "2024-12-01T12:00:00"
}
```

## Quy trình Test

### Bước 1: Tạo đơn hàng với phương thức thanh toán VNPay

1. Đặt hàng với `paymentMethod: "VNPAY"`
2. Lưu lại `orderId` từ response

### Bước 2: Tạo URL thanh toán

1. Gọi API `POST /api/vnpay/create-payment-url/{orderId}`
2. Copy URL thanh toán từ response

### Bước 3: Test thanh toán

1. Mở URL thanh toán trong browser
2. Chọn ngân hàng test (có thể chọn bất kỳ)
3. Nhập thông tin test:
   - **Số thẻ**: 9704000000000018
   - **Tên chủ thẻ**: NGUYEN VAN A
   - **Ngày phát hành**: 07/15
   - **OTP**: 123456

### Bước 4: Kiểm tra kết quả

1. Sau khi thanh toán, VNPay sẽ redirect về callback URL
2. Kiểm tra trạng thái đơn hàng đã được cập nhật chưa
3. Kiểm tra payment status trong database

## Lưu ý quan trọng

### Môi trường TEST

- Sử dụng URL sandbox: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- Không có phí giao dịch thực
- Có thể test nhiều lần

### Bảo mật

- Luôn verify signature từ VNPay callback
- Kiểm tra số tiền khớp với đơn hàng
- Log đầy đủ thông tin giao dịch

### Xử lý lỗi

- Nếu callback không được gọi: Kiểm tra Return URL
- Nếu signature không hợp lệ: Kiểm tra HashSecret
- Nếu số tiền không khớp: Kiểm tra format số tiền (VNPay yêu cầu nhân 100)

## Test Cases

### Test Case 1: Thanh toán thành công

1. Tạo đơn hàng 100,000 VND
2. Tạo URL thanh toán
3. Thanh toán với thẻ test
4. Verify callback thành công
5. Kiểm tra order status = PAID

### Test Case 2: Thanh toán thất bại

1. Tạo đơn hàng
2. Tạo URL thanh toán
3. Hủy thanh toán
4. Verify callback thất bại
5. Kiểm tra order status = UNPAID

### Test Case 3: Invalid signature

1. Giả mạo callback với signature sai
2. Verify callback bị từ chối
3. Order status không thay đổi
