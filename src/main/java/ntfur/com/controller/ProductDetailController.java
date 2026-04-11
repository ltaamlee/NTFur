package ntfur.com.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.category.CategoryDTO;
import ntfur.com.entity.dto.product.ProductDTO;
import ntfur.com.service.CategoryService;
import ntfur.com.service.ProductService;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            ProductDTO product = productService.getProductById(id);
            List<ProductDTO> relatedProducts = productService.getProductsByCategory(product.getCategoryId());
            List<CategoryDTO> categories = categoryService.getActiveCategoriesWithHierarchy();
            
            // Remove current product from related list
            relatedProducts.removeIf(p -> p.getId().equals(id));
            
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("categories", categories);
            model.addAttribute("content", "home/pages/product-detail");
            model.addAttribute("title", product.getName());
            return "home/layout";
        } catch (Exception e) {
            return "redirect:/products";
        }
    }
}
