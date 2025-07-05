package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.sportshop.api.Domain.Users;
import com.sportshop.api.Domain.RefreshToken;
import com.sportshop.api.Domain.Request.Auth.RegisterRequest;
import com.sportshop.api.Domain.Request.Auth.LoginRequest;
import com.sportshop.api.Domain.Request.Auth.VerifyOTPRequest;
import com.sportshop.api.Domain.Reponse.Auth.AuthResponse;
import com.sportshop.api.Domain.Reponse.Auth.RegisterResponse;
import com.sportshop.api.Repository.UserRepository;
import com.sportshop.api.Repository.RefreshTokenRepository;
import com.sportshop.api.Config.JwtConfig;
import com.sportshop.api.Config.JwtUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailOTPService emailOTPService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JwtConfig jwtConfig;

    public AuthService(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailOTPService emailOTPService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailOTPService = emailOTPService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.jwtConfig = jwtConfig;
    }

    /**
     * Đăng ký tài khoản mới
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }

        // Tạo user mới với trạng thái chưa kích hoạt
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGender(Users.Gender.valueOf(request.getGender()));
        user.setRoleId(Long.valueOf(2)); // Role user thường
        user.setFirstLogin(true);
        user.setProvider(Users.Provider.DEFAULT);

        // Lưu user
        Users savedUser = userRepository.save(user);

        // Tạo và gửi OTP
        String otpCode = emailOTPService.generateOTP(savedUser);
        sendOTPEmail(savedUser, otpCode);

        return new RegisterResponse(
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                savedUser.getEmail(),
                savedUser.getId());
    }

    /**
     * Đăng nhập
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Tìm user theo email
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không đúng"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
        }

        // Kiểm tra tài khoản đã được kích hoạt chưa
        if (!user.getActive()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt. Vui lòng xác thực email trước.");
        }

        // Tạo tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Lưu refresh token
        saveRefreshToken(user, refreshToken);

        // Tạo response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender().toString(),
                user.getAvatar(),
                user.getFirstLogin());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtConfig.getAccessTokenExpirationMs(),
                userInfo);
    }

    /**
     * Xác thực OTP
     */
    @Transactional
    public RegisterResponse verifyOTP(VerifyOTPRequest request) {
        // Tìm user theo email
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        // Xác thực OTP
        boolean isValidOTP = emailOTPService.verifyOTP(user.getId(), request.getOtpCode());
        if (!isValidOTP) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Kích hoạt tài khoản (chỉ set active = true, giữ firstLogin = true)
        user.setActive(true);
        userRepository.save(user);

        return new RegisterResponse(
                "Xác thực tài khoản thành công! Bạn có thể đăng nhập.",
                user.getEmail(),
                user.getId());
    }

    /**
     * Gửi lại OTP
     */
    @Transactional
    public RegisterResponse resendOTP(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        // Kiểm tra tài khoản đã kích hoạt chưa
        if (!user.getFirstLogin()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt");
        }

        // Tạo và gửi OTP mới
        String otpCode = emailOTPService.generateOTP(user);
        sendOTPEmail(user, otpCode);

        return new RegisterResponse(
                "Đã gửi lại mã OTP! Vui lòng kiểm tra email.",
                user.getEmail(),
                user.getId());
    }

    /**
     * Refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Xác thực refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        // Tìm refresh token trong database
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        // Kiểm tra token đã hết hạn chưa
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        // Lấy thông tin user
        String email = jwtUtil.extractSubject(refreshToken);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Tạo tokens mới
        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        // Xóa refresh token cũ và lưu mới
        refreshTokenRepository.delete(token);
        saveRefreshToken(user, newRefreshToken);

        // Tạo response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender().toString(),
                user.getAvatar(),
                user.getFirstLogin());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtConfig.getAccessTokenExpirationMs(),
                userInfo);
    }

    /**
     * Đăng xuất
     */
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElse(null);
        if (token != null) {
            refreshTokenRepository.delete(token);
        }
    }

    /**
     * Lưu refresh token
     */
    private void saveRefreshToken(Users user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(java.time.LocalDateTime.now().plusDays(7)); // 7 ngày
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Gửi email OTP
     */
    private void sendOTPEmail(Users user, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Xác thực tài khoản - SportX");
            helper.setFrom("noreply@sportx.com");

            // Tạo context cho template
            Context context = new Context();
            context.setVariable("fullName", user.getFullName());
            context.setVariable("otpCode", otpCode);

            // Render template
            String htmlContent = templateEngine.process("email-otp", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Gửi OTP quên mật khẩu
     */
    @Transactional
    public RegisterResponse forgotPasswordRequest(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));
        String otpCode = emailOTPService.generateOTP(user);
        sendResetPasswordOTPEmail(user, otpCode);
        return new RegisterResponse(
                "Đã gửi mã OTP đặt lại mật khẩu! Vui lòng kiểm tra email.",
                user.getEmail(),
                user.getId());
    }

    /**
     * Gửi email OTP quên mật khẩu với template reset-password.html
     */
    private void sendResetPasswordOTPEmail(Users user, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Khôi phục mật khẩu - SportX");
            helper.setFrom("noreply@sportx.com");

            // Tạo context cho template
            Context context = new Context();
            context.setVariable("fullName", user.getFullName());
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", user.getEmail());
            context.setVariable("requestTime", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
                    .format(java.time.LocalDateTime.now()));
            context.setVariable("ipAddress", ""); // Có thể lấy từ request nếu cần
            context.setVariable("deviceInfo", ""); // Có thể lấy từ request nếu cần

            // Render template reset-password.html
            String htmlContent = templateEngine.process("reset-password", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Xác thực OTP và đổi mật khẩu mới
     */
    @Transactional
    public RegisterResponse forgotPasswordVerify(String email, String otpCode, String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));
        boolean isValidOTP = emailOTPService.verifyOTP(user.getId(), otpCode);
        if (!isValidOTP) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new RegisterResponse(
                "Đổi mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.",
                user.getEmail(),
                user.getId());
    }

    /**
     * Kích hoạt tài khoản admin (đặc biệt cho tài khoản được tạo trực tiếp trong
     * DB)
     */
    @Transactional
    public RegisterResponse activateAdminAccount(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        // Kiểm tra xem có phải admin không (roleId = 1)
        if (user.getRoleId() == null || user.getRoleId() != 1L) {
            throw new RuntimeException("Chỉ có thể kích hoạt tài khoản admin");
        }

        // Kiểm tra tài khoản đã kích hoạt chưa
        if (user.getActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt");
        }

        // Kích hoạt tài khoản
        user.setActive(true);
        userRepository.save(user);

        return new RegisterResponse(
                "Kích hoạt tài khoản admin thành công! Bạn có thể đăng nhập.",
                user.getEmail(),
                user.getId());
    }

}