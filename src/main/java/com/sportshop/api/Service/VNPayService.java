package com.sportshop.api.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.sportshop.api.Config.VNPayConfig;

@Service
public class VNPayService {

    private final VNPayConfig vnpayConfig;

    public VNPayService(VNPayConfig vnpayConfig) {
        this.vnpayConfig = vnpayConfig;
    }

    /**
     * Tạo URL thanh toán VNPay
     * 
     * @param orderId   Mã đơn hàng
     * @param amount    Số tiền thanh toán (VND)
     * @param orderInfo Thông tin đơn hàng
     * @param bankCode  Mã ngân hàng (để trống = cho phép chọn)
     * @param locale    Ngôn ngữ (vn/en)
     * @return URL thanh toán VNPay
     */
    public String createPaymentUrl(String orderId, Long amount, String orderInfo, String bankCode, String locale) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderId;
        String vnp_IpAddr = "127.0.0.1";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", bankCode != null ? bankCode : "");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", locale != null ? locale : "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnpayConfig.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnpayConfig.getPayUrl() + "?" + queryUrl;
    }

    /**
     * Xác thực callback từ VNPay
     * 
     * @param params Các tham số từ callback
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public boolean verifyPaymentResponse(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        // Tạo bản copy để không mutate map gốc
        Map<String, String> paramsCopy = new HashMap<>(params);
        paramsCopy.remove("vnp_SecureHash");
        paramsCopy.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(paramsCopy.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsCopy.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        String calculatedHash = vnpayConfig.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
    }

    /**
     * Kiểm tra trạng thái giao dịch
     * 
     * @param txnRef Mã giao dịch
     * @return Map chứa thông tin trạng thái giao dịch
     */
    public Map<String, String> queryTransactionStatus(String txnRef) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = vnpayConfig.getTmnCode();
        String vnp_TxnRef = txnRef;
        String vnp_OrderInfo = "Kiem tra giao dich";
        String vnp_TransDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_TransDate", vnp_TransDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnpayConfig.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // Trong thực tế, bạn sẽ gọi API này để kiểm tra trạng thái
        // String fullUrl = vnpTransactionQueryUrl + "?" + queryUrl;
        // Thực hiện HTTP request đến VNPay

        return vnp_Params; // Trả về params để demo
    }

    /**
     * Xử lý response code từ VNPay và trả về thông báo lỗi tương ứng
     * 
     * @param responseCode Mã phản hồi từ VNPay
     * @return Thông báo lỗi
     */
    public String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "01":
                return "Giao dịch chưa hoàn tất";
            case "02":
                return "Giao dịch bị lỗi";
            case "04":
                return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05":
                return "VNPAY đang xử lý (giao dịch này đã được ghi nhận, VNPAY đang xử lý)";
            case "06":
                return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD bị lỗi, VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng, VNPAY đang chờ ngân hàng hoàn tiền)";
            case "07":
                return "Giao dịch bị nghi ngờ gian lận";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản bị khóa";
            case "13":
                return "Giao dịch không thành công do: Nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "65":
                return "Giao dịch không thành công do: Tài khoản không đủ số dư";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: Nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Mã lỗi không xác định: " + responseCode;
        }
    }

    /**
     * Kiểm tra xem response code có phải là thành công không
     * 
     * @param responseCode Mã phản hồi từ VNPay
     * @return true nếu thành công
     */
    public boolean isSuccessResponse(String responseCode) {
        return "00".equals(responseCode);
    }

    /**
     * Kiểm tra xem response code có phải là lỗi thẻ không
     * 
     * @param responseCode Mã phản hồi từ VNPay
     * @return true nếu là lỗi thẻ
     */
    public boolean isCardError(String responseCode) {
        return "09".equals(responseCode) || "65".equals(responseCode) || "79".equals(responseCode);
    }

    /**
     * Tạo HMAC-SHA512 hash
     * 
     * @param key  Secret key
     * @param data Data to hash
     * @return Hashed string
     */
    private String hmacSHA512(String key, String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hash = md.digest((key + data).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error creating hash", e);
        }
    }
}