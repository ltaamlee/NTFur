package ntfur.com.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.category.CategoryDTO;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.service.CategoryService;
import ntfur.com.service.ProductService;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String home(Model model) {
        List<ProductDTO> featuredProducts = productService.getFeaturedProducts();
        List<CategoryDTO> categories = categoryService.getActiveCategoriesWithHierarchy();
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("content", "home/pages/landing");
        model.addAttribute("title", "Trang chủ");
        return "home/layout";
    }

    @GetMapping("products")
    public String products(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        List<CategoryDTO> categories = categoryService.getActiveCategoriesWithHierarchy();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("content", "home/pages/products");
        model.addAttribute("title", "Sản phẩm");
        return "home/layout";
    }

    @GetMapping("about")
    public String about() {
        return "about";
    }

    @GetMapping("contact")
    public String contact() {
        return "contact";
    }

    // API endpoints for frontend
    @GetMapping("api/products")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", products));
    }

    @GetMapping("api/products/featured")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFeaturedProducts() {
        List<ProductDTO> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm nổi bật thành công", products));
    }

    @GetMapping("api/products/search")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm sản phẩm thành công", products));
    }

    @GetMapping("api/categories")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {
        List<CategoryDTO> categories = categoryService.getActiveCategoriesWithHierarchy();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục thành công", categories));
    }
}
