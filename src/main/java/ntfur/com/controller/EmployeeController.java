package ntfur.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Tổng quan - NTFurniture");
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "employee/pages/dashboard");
        return "employee/layout";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Sản phẩm - NTFurniture");
        model.addAttribute("pageTitle", "Sản phẩm");
        model.addAttribute("activePage", "products");
        model.addAttribute("content", "employee/pages/products");
        return "employee/layout";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("title", "Nhà cung cấp - NTFurniture");
        model.addAttribute("pageTitle", "Nhà cung cấp");
        model.addAttribute("activePage", "suppliers");
        model.addAttribute("content", "employee/pages/suppliers");
        return "employee/layout";
    }

    @GetMapping("/import")
    public String importPage(Model model) {
        model.addAttribute("title", "Nhập hàng - NTFurniture");
        model.addAttribute("pageTitle", "Nhập hàng");
        model.addAttribute("activePage", "import");
        model.addAttribute("content", "employee/pages/import");
        return "employee/layout";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Đơn hàng - NTFurniture");
        model.addAttribute("pageTitle", "Đơn hàng");
        model.addAttribute("activePage", "orders");
        model.addAttribute("content", "employee/pages/orders");
        return "employee/layout";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("title", "Vận chuyển - NTFurniture");
        model.addAttribute("pageTitle", "Vận chuyển");
        model.addAttribute("activePage", "shipping");
        model.addAttribute("content", "employee/pages/shipping");
        return "employee/layout";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("title", "Khách hàng - NTFurniture");
        model.addAttribute("pageTitle", "Khách hàng");
        model.addAttribute("activePage", "customers");
        model.addAttribute("content", "employee/pages/customers");
        return "employee/layout";
    }
}
