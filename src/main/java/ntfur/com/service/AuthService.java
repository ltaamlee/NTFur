package ntfur.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ntfur.com.entity.User;
import ntfur.com.entity.dto.*;
import ntfur.com.repository.UserRepository;
import ntfur.com.security.CustomUserDetails;
import ntfur.com.security.JwtTokenProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8386}")
    private String baseUrl;

    // Xử lý đăng nhập
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            // Tìm user theo username hoặc email
            User user = userRepository.findByUsername(request.getUsernameOrEmail())
                    .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                            .orElse(null));

            if (user == null) {
                return ApiResponse.error("Tên đăng nhập hoặc email không tồn tại");
            }

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ApiResponse.error("Mật khẩu không đúng");
            }

            // Kiểm tra trạng thái tài khoản
            if (user.getStatus() == User.UserStatus.PENDING) {
                return ApiResponse.error("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
            }

            if (user.getStatus() == User.UserStatus.SUSPENDED) {
                return ApiResponse.error("Tài khoản đã bị tạm khóa. Vui lòng liên hệ hỗ trợ.");
            }

            if (user.getStatus() == User.UserStatus.INACTIVE) {
                return ApiResponse.error("Tài khoản đã bị vô hiệu hóa");
            }

            // Xác thực và tạo token
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            UserDTO userDTO = convertToUserDTO(user);

            log.info("Đăng nhập thành công: {}", user.getUsername());
            return ApiResponse.success(new AuthResponse(accessToken, refreshToken, 86400000, userDTO));
        } catch (Exception e) {
            log.error("Lỗi đăng nhập: {}", e.getMessage());
            return ApiResponse.error("Đăng nhập thất bại: " + e.getMessage());
        }
    }

    // Xử lý đăng ký
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {
        try {
            // Kiểm tra password match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("Mật khẩu xác nhận không khớp");
            }

            // Kiểm tra username đã tồn tại
            if (userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.error("Tên đăng nhập đã được sử dụng");
            }

            // Kiểm tra email đã tồn tại
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error("Email đã được sử dụng");
            }

            // Tạo user mới
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setRole(User.UserRole.CUSTOMER);
            user.setStatus(User.UserStatus.PENDING);
            user.setCode(UUID.randomUUID().toString());

            user = userRepository.save(user);

            // Gửi email xác thực
            String verificationToken = jwtTokenProvider.generateEmailVerificationToken(user.getEmail());
            String verificationUrl = baseUrl + "/auth/verify?token=" + verificationToken;
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

            log.info("Đăng ký thành công: {}", user.getUsername());
            return ApiResponse.success("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.", null);
        } catch (Exception e) {
            log.error("Lỗi đăng ký: {}", e.getMessage());
            return ApiResponse.error("Đăng ký thất bại: " + e.getMessage());
        }
    }

    // Xác thực tài khoản
    @Transactional
    public ApiResponse<String> verifyAccount(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return ApiResponse.error("Token không hợp lệ hoặc đã hết hạn");
            }

            if (!jwtTokenProvider.isEmailVerificationToken(token)) {
                return ApiResponse.error("Token không hợp lệ cho xác thực email");
            }

            String email = jwtTokenProvider.extractUsername(token);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản với email này");
            }

            if (user.getStatus() != User.UserStatus.PENDING) {
                return ApiResponse.error("Tài khoản đã được xác thực hoặc không ở trạng thái chờ xác thực");
            }

            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);

            log.info("Xác thực tài khoản thành công: {}", user.getUsername());
            return ApiResponse.success("Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (Exception e) {
            log.error("Lỗi xác thực: {}", e.getMessage());
            return ApiResponse.error("Xác thực thất bại: " + e.getMessage());
        }
    }

    // Gửi lại email xác thực
    @Transactional
    public ApiResponse<String> resendVerificationEmail(String email) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản với email này");
            }

            if (user.getStatus() == User.UserStatus.ACTIVE) {
                return ApiResponse.error("Tài khoản đã được xác thực");
            }

            String verificationToken = jwtTokenProvider.generateEmailVerificationToken(user.getEmail());
            String verificationUrl = baseUrl + "/auth/verify?token=" + verificationToken;
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

            return ApiResponse.success("Đã gửi lại email xác thực thành công!");
        } catch (Exception e) {
            log.error("Lỗi gửi lại email xác thực: {}", e.getMessage());
            return ApiResponse.error("Gửi email thất bại: " + e.getMessage());
        }
    }

    // Quên mật khẩu - gửi email đặt lại
    @Transactional
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);

            // Luôn trả thành công để tránh user enumeration
            if (user == null) {
                return ApiResponse.success("Nếu email tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu.");
            }

            String resetToken = jwtTokenProvider.generatePasswordResetToken(user.getEmail());
            String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetUrl);

            return ApiResponse.success("Nếu email tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu.");
        } catch (Exception e) {
            log.error("Lỗi quên mật khẩu: {}", e.getMessage());
            return ApiResponse.error("Xử lý yêu cầu thất bại");
        }
    }

    // Đặt lại mật khẩu
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("Mật khẩu xác nhận không khớp");
            }

            if (!jwtTokenProvider.validateToken(request.getToken())) {
                return ApiResponse.error("Token không hợp lệ hoặc đã hết hạn");
            }

            if (!jwtTokenProvider.isPasswordResetToken(request.getToken())) {
                return ApiResponse.error("Token không hợp lệ cho đặt lại mật khẩu");
            }

            String email = jwtTokenProvider.extractUsername(request.getToken());
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("Đặt lại mật khẩu thành công: {}", user.getUsername());
            return ApiResponse.success("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (Exception e) {
            log.error("Lỗi đặt lại mật khẩu: {}", e.getMessage());
            return ApiResponse.error("Đặt lại mật khẩu thất bại: " + e.getMessage());
        }
    }

    // Kiểm tra token reset có hợp lệ không
    public boolean isValidResetToken(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return false;
            }
            if (!jwtTokenProvider.isPasswordResetToken(token)) {
                return false;
            }
            String email = jwtTokenProvider.extractUsername(token);
            return userRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    // Đổi mật khẩu (khi đã đăng nhập)
    @Transactional
    public ApiResponse<String> changePassword(ChangePasswordRequest request, Long userId) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("Mật khẩu xác nhận không khớp");
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error("Không tìm thấy tài khoản");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ApiResponse.error("Mật khẩu hiện tại không đúng");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("Đổi mật khẩu thành công: {}", user.getUsername());
            return ApiResponse.success("Đổi mật khẩu thành công!");
        } catch (Exception e) {
            log.error("Lỗi đổi mật khẩu: {}", e.getMessage());
            return ApiResponse.error("Đổi mật khẩu thất bại: " + e.getMessage());
        }
    }

    // Refresh token
    public ApiResponse<AuthResponse> refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ApiResponse.error("Refresh token không hợp lệ hoặc đã hết hạn");
            }

            String username = jwtTokenProvider.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                return ApiResponse.error("Không tìm thấy người dùng");
            }

            CustomUserDetails userDetails = new CustomUserDetails(user);
            String newAccessToken = jwtTokenProvider.generateToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            return ApiResponse.success(new AuthResponse(newAccessToken, newRefreshToken, 86400000, convertToUserDTO(user)));
        } catch (Exception e) {
            log.error("Lỗi refresh token: {}", e.getMessage());
            return ApiResponse.error("Refresh token thất bại: " + e.getMessage());
        }
    }

    // Chuyển đổi User sang UserDTO
    private UserDTO convertToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.getAvatarUrl()
        );
    }
}
