package ntfur.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Gửi email xác thực tài khoản
    @Async
    public void sendVerificationEmail(String to, String username, String verificationUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Xác thực tài khoản - NTFurniture");
            message.setText(buildVerificationEmailContent(username, verificationUrl));
            mailSender.send(message);
            log.info("Đã gửi email xác thực đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác thực đến {}: {}", to, e.getMessage());
        }
    }

    // Gửi email đặt lại mật khẩu
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Đặt lại mật khẩu - NTFurniture");
            message.setText(buildPasswordResetEmailContent(username, resetUrl));
            mailSender.send(message);
            log.info("Đã gửi email đặt lại mật khẩu đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu đến {}: {}", to, e.getMessage());
        }
    }

    // Gửi email thông báo đăng nhập thành công
    @Async
    public void sendLoginNotification(String to, String username, String ipAddress, String device) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Thông báo đăng nhập - NTFurniture");
            message.setText(buildLoginNotificationContent(username, ipAddress, device));
            mailSender.send(message);
            log.info("Đã gửi thông báo đăng nhập đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo đăng nhập đến {}: {}", to, e.getMessage());
        }
    }

    // Nội dung email xác thực
    private String buildVerificationEmailContent(String username, String verificationUrl) {
        return """
            Xin chào %s,
            
            Cảm ơn bạn đã đăng ký tài khoản tại NTFurniture!
            
            Vui lòng nhấp vào liên kết bên dưới để xác thực tài khoản của bạn:
            
            %s
            
            Liên kết này sẽ hết hạn sau 1 giờ.
            
            Nếu bạn không thực hiện đăng ký, vui lòng bỏ qua email này.
            
            Trân trọng,
            NTFurniture Team
            """.formatted(username, verificationUrl);
    }

    // Nội dung email đặt lại mật khẩu
    private String buildPasswordResetEmailContent(String username, String resetUrl) {
        return """
            Xin chào %s,
            
            Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
            
            Vui lòng nhấp vào liên kết bên dưới để đặt lại mật khẩu:
            
            %s
            
            Liên kết này sẽ hết hạn sau 30 phút.
            
            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ với chúng tôi ngay.
            
            Trân trọng,
            NTFurniture Team
            """.formatted(username, resetUrl);
    }

    // Nội dung thông báo đăng nhập
    private String buildLoginNotificationContent(String username, String ipAddress, String device) {
        return """
            Xin chào %s,
            
            Chúng tôi phát hiện một đăng nhập mới vào tài khoản của bạn:
            
            - Thiết bị: %s
            - Địa chỉ IP: %s
            - Thời gian: %s
            
            Nếu đây là bạn, bạn có thể bỏ qua email này.
            
            Nếu bạn không đăng nhập, vui lòng liên hệ với chúng tôi ngay và đặt lại mật khẩu của bạn.
            
            Trân trọng,
            NTFurniture Team
            """.formatted(username, device, ipAddress, java.time.LocalDateTime.now());
    }
}
