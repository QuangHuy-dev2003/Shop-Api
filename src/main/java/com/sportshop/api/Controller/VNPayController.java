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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
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
    @PostMapping("vnpay/create-payment")
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
     * Xử lý callback từ VNPay (dạng truyền thống: BE redirect về FE)
     * 
     * @param params   Các tham số từ VNPay
     * @param response HttpServletResponse để redirect
     */
    @GetMapping("vnpay/payment-callback")
    public void paymentCallback(@RequestParam Map<String, String> params, HttpServletResponse response)
            throws IOException {
        try {
            boolean isValid = vnPayService.verifyPaymentResponse(params);
            if (!isValid) {
                String url = "http://localhost:5174/thankyou/failure?message=INVALID_SIGNATURE";
                response.sendRedirect(url);
                return;
            }

            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef"); // orderId hoặc mã giao dịch

            // Xử lý callback và cập nhật trạng thái đơn hàng
            boolean success = orderService.processVNPayCallback(params);

            String url;
            if (success && "00".equals(vnp_ResponseCode)) {
                url = "http://localhost:5174/thankyou/" + vnp_TxnRef + "?status=success";
            } else {
                url = "http://localhost:5174/thankyou/" + vnp_TxnRef + "?status=failure&code=" + vnp_ResponseCode;
            }
            response.sendRedirect(url);
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5174/thankyou/failure?message=SYSTEM_ERROR");
        }
    }

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * 
     * @param orderId ID đơn hàng
     * @return URL thanh toán VNPay
     */
    @PostMapping("vnpay/create-payment-url/{orderId}")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createPaymentUrlForOrder(
            @PathVariable("orderId") Long orderId) {
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
    @GetMapping("vnpay/transaction-status/{txnRef}")
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