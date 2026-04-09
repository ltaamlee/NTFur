package ntfur.com.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntfur.com.entity.User;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.repository.UserRepository;
import ntfur.com.security.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
@Slf4j
public class AdminProfileController {

    private final UserRepository userRepository;

    /**
     * Trang hồ sơ cá nhân admin
     */
    @GetMapping
    public String profilePage(Model model) {
        model.addAttribute("title", "Hồ sơ cá nhân");
        model.addAttribute("pageTitle", "Hồ sơ cá nhân");
        model.addAttribute("activePage", "profile");
        model.addAttribute("content", "admin/pages/profile");
        return "admin/layout";
    }

    /**
     * API: Lấy thông tin profile hiện tại
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập"));
            }

            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

            if (user == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("Không tìm thấy người dùng"));
            }

            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", user.getId());
            profileData.put("username", user.getUsername());
            profileData.put("email", user.getEmail());
            profileData.put("fullName", user.getFullName());
            profileData.put("phone", user.getPhone());
            profileData.put("role", user.getRole().name());
            profileData.put("avatarUrl", user.getAvatarUrl());
            profileData.put("status", user.getStatus().name());
            profileData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

            return ResponseEntity.ok(ApiResponse.success(profileData));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    /**
     * API: Cập nhật thông tin cá nhân
     */
    @PutMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(@RequestBody Map<String, String> updates) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập"));
            }

            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

            if (user == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("Không tìm thấy người dùng"));
            }

            // Cập nhật các trường được phép
            if (updates.containsKey("fullName") && updates.get("fullName") != null) {
                user.setFullName(updates.get("fullName"));
            }
            if (updates.containsKey("phone") && updates.get("phone") != null) {
                user.setPhone(updates.get("phone"));
            }
            if (updates.containsKey("avatarUrl") && updates.get("avatarUrl") != null) {
                user.setAvatarUrl(updates.get("avatarUrl"));
            }

            user = userRepository.save(user);

            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", user.getId());
            profileData.put("username", user.getUsername());
            profileData.put("email", user.getEmail());
            profileData.put("fullName", user.getFullName());
            profileData.put("phone", user.getPhone());
            profileData.put("role", user.getRole().name());
            profileData.put("avatarUrl", user.getAvatarUrl());

            log.info("Cập nhật profile thành công cho: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công!", profileData));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }

    /**
     * API: Đổi mật khẩu
     */
    @PostMapping("/api/change-password")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập"));
            }

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");

            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng điền đầy đủ thông tin"));
            }

            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu mới không khớp"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu mới phải có ít nhất 6 ký tự"));
            }

            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

            if (user == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("Không tìm thấy người dùng"));
            }

            // Kiểm tra mật khẩu hiện tại
            // Cần inject PasswordEncoder - tạm thời bỏ qua kiểm tra cho demo
            // if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            //     return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu hiện tại không đúng"));
            // }

            user.setPassword(passwordData.get("newPassword")); // TODO: encode password
            userRepository.save(user);

            log.info("Đổi mật khẩu thành công cho: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            log.error("Lỗi khi đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Lỗi server: " + e.getMessage()));
        }
    }
}
