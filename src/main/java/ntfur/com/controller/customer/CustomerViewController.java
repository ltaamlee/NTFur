package ntfur.com.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerViewController {

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("title", "Tài khoản của tôi");
        model.addAttribute("pageTitle", "Hồ sơ của tôi");
        model.addAttribute("activePage", "profile");
        model.addAttribute("content", "customer/pages/profile");
        return "customer/layout";
    }

    @GetMapping("/cart")
    public String cartPage(Model model) {
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("activePage", "cart");
        model.addAttribute("content", "customer/pages/cart");
        return "customer/layout";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("pageTitle", "Thanh toán");
        model.addAttribute("activePage", "checkout");
        model.addAttribute("content", "customer/pages/checkout");
        return "customer/layout";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        model.addAttribute("title", "Đơn hàng của tôi");
        model.addAttribute("pageTitle", "Đơn hàng");
        model.addAttribute("activePage", "orders");
        model.addAttribute("content", "customer/pages/orders");
        return "customer/layout";
    }
}
