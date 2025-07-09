package com.sportshop.api.Controller;

import com.sportshop.api.Service.VNPayService;
import com.sportshop.api.Service.OrderService;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Domain.Request.VNPay.CreateVNPayPaymentRequest;
import com.sportshop.api.Domain.Reponse.VNPay.VNPayPaymentResponse;
import com.sportshop.api.Domain.Reponse.VNPay.VNPayCallbackResponse;
import com.sportshop.api.Domain.Reponse.VNPay.VNPayTransactionStatusResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin(origins = "*")
public class VNPayController {

    private final VNPayService vnPayService;
    private final OrderService orderService;

    public VNPayController(VNPayService vnPayService, OrderService orderService) {
        this.vnPayService = vnPayService;
        this.orderService = orderService;
    }

    /**
     * Tạo URL thanh toán VNPay
     * 
     * @param request Thông tin thanh toán
     * @return URL thanh toán VNPay
     */
    @PostMapping("/create-payment")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createPayment(
            @Valid @RequestBody CreateVNPayPaymentRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(
                    request.getOrderId().toString(),
                    request.getAmount(),
                    request.getOrderInfo(),
                    request.getBankCode(),
                    request.getLocale());

            VNPayPaymentResponse response = new VNPayPaymentResponse(
                    paymentUrl,
                    request.getOrderId().toString(),
                    request.getAmount(),
                    request.getOrderInfo(),
                    LocalDateTime.now(),
                    "TXN" + System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.success(response, "Tạo URL thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi tạo URL thanh toán: " + e.getMessage()));
        }
    }

    /**
     * Xử lý callback từ VNPay
     * 
     * @param params Các tham số từ VNPay
     * @return Kết quả xử lý
     */
    @GetMapping("/payment-callback")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> paymentCallback(
            @RequestParam Map<String, String> params) {
        try {
            // Xác thực callback
            if (!vnPayService.verifyPaymentResponse(params)) {
                VNPayCallbackResponse errorResponse = VNPayCallbackResponse.error("99", "INVALID_SIGNATURE",
                        "Chữ ký không hợp lệ");
                return ResponseEntity.badRequest().body(ApiResponse.error("Chữ ký không hợp lệ"));
            }

            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");
            String vnp_BankCode = params.get("vnp_BankCode");
            String vnp_BankTranNo = params.get("vnp_BankTranNo");
            String vnp_CardType = params.get("vnp_CardType");
            String vnp_PayDate = params.get("vnp_PayDate");

            // Xử lý callback và cập nhật trạng thái đơn hàng
            boolean success = orderService.processVNPayCallback(params);

            if (success) {
                // Parse pay date
                LocalDateTime payDate = null;
                if (vnp_PayDate != null && vnp_PayDate.length() >= 14) {
                    try {
                        payDate = LocalDateTime.parse(vnp_PayDate.substring(0, 8) + "T" + vnp_PayDate.substring(8, 12)
                                + ":" + vnp_PayDate.substring(12, 14) + ":00");
                    } catch (Exception e) {
                        payDate = LocalDateTime.now();
                    }
                }

                VNPayCallbackResponse response = VNPayCallbackResponse.success(
                        vnp_ResponseCode,
                        vnp_TxnRef,
                        "TXN" + System.currentTimeMillis(),
                        vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : 0L,
                        vnp_BankCode,
                        vnp_BankTranNo,
                        vnp_CardType,
                        payDate != null ? payDate : LocalDateTime.now(),
                        vnp_TransactionNo);

                return ResponseEntity.ok(ApiResponse.success(response, "Thanh toán thành công"));
            } else {
                String errorMessage = vnPayService.getResponseMessage(vnp_ResponseCode);
                VNPayCallbackResponse response = VNPayCallbackResponse.error(vnp_ResponseCode, vnp_ResponseCode,
                        errorMessage);
                return ResponseEntity.ok(ApiResponse.success(response, "Thanh toán thất bại"));
            }
        } catch (Exception e) {
            VNPayCallbackResponse errorResponse = VNPayCallbackResponse.error("99", "PROCESSING_ERROR", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi xử lý callback: " + e.getMessage()));
        }
    }

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * 
     * @param orderId ID đơn hàng
     * @return URL thanh toán VNPay
     */
    @PostMapping("/create-payment-url/{orderId}")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createPaymentUrlForOrder(@PathVariable Long orderId) {
        try {
            String paymentUrl = orderService.createVNPayPaymentUrl(orderId);

            VNPayPaymentResponse response = new VNPayPaymentResponse(
                    paymentUrl,
                    orderId.toString(),
                    null, // amount sẽ được lấy từ order
                    "Thanh toan don hang " + orderId,
                    LocalDateTime.now(),
                    "TXN" + System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.success(response, "Tạo URL thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi tạo URL thanh toán: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra trạng thái giao dịch
     * 
     * @param txnRef Mã giao dịch
     * @return Thông tin trạng thái giao dịch
     */
    @GetMapping("/transaction-status/{txnRef}")
    public ResponseEntity<ApiResponse<VNPayTransactionStatusResponse>> getTransactionStatus(
            @PathVariable String txnRef) {
        try {
            Map<String, String> status = vnPayService.queryTransactionStatus(txnRef);

            VNPayTransactionStatusResponse response = new VNPayTransactionStatusResponse(
                    txnRef,
                    status.get("vnp_TxnRef"),
                    status.get("vnp_ResponseCode"),
                    vnPayService.getResponseMessage(status.get("vnp_ResponseCode")),
                    0L, // amount sẽ được parse từ status
                    status.get("vnp_BankCode"),
                    status.get("vnp_BankTranNo"),
                    status.get("vnp_CardType"),
                    LocalDateTime.now(), // payDate sẽ được parse từ status
                    status.get("vnp_TransactionNo"),
                    status.get("vnp_ResponseCode"),
                    LocalDateTime.now(),
                    null, // errorCode
                    null // errorMessage
            );

            return ResponseEntity.ok(ApiResponse.success(response, "Lấy trạng thái giao dịch thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy trạng thái giao dịch: " + e.getMessage()));
        }
    }

}