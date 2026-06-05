package com.example.qlgear.controller;

import com.example.qlgear.entity.Category;
import com.example.qlgear.entity.Product;
import com.example.qlgear.entity.User;
import com.example.qlgear.service.OrderService;
import com.example.qlgear.service.ProductService;
import com.example.qlgear.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.qlgear.entity.OrderEntity;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    private final ProductService service;
    private final CategoryService serviceCate;
    private final OrderService serviceOrder;
    public ProductController(OrderService serviceOrder,ProductService service,CategoryService serverCate){
        this.service=service;
        this.serviceCate=serverCate;
        this.serviceOrder=serviceOrder;
    }
    @GetMapping("/home")
    public String home(HttpSession session, Model model){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        long totalProducts = service.getAllProducts().size();
        long totalCategories = serviceCate.getAllCategory().size();
        List<OrderEntity> orders = serviceOrder.getAllOrders();
        long totalOrders = orders.size();
        
        // Thống kê doanh thu
        double totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        LocalDate today = LocalDate.now();
        double revenueToday = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(today))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        // Tổng số lượng sản phẩm đã bán
        long totalProductsSold = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .flatMap(o -> o.getOrderItems().stream())
                .mapToLong(oi -> oi.getQuantity() != null ? oi.getQuantity() : 0)
                .sum();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        
        model.addAttribute("revenueToday", revenueToday);
        model.addAttribute("totalProductsSold", totalProductsSold);

        return "home";
    }
    @GetMapping("/")
    public String customerHome(Model model){

        model.addAttribute(
                "products",
                service.getAllProducts()
        );

        model.addAttribute(
                "categories",
                serviceCate.getAllCategory()
        );

        return "kh_home";
    }
    //view list sp
    @GetMapping("/products")
    public String list(Model model, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";
        model.addAttribute("listsp",service.getAllProducts());
        return "products";
    }
    //them sp
    @GetMapping("/add")
    public String add(Model model, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/products";
        model.addAttribute("product",new Product());
        model.addAttribute("categories", serviceCate.getAllCategory());
        return "/add";
    }
    //helper lưu ảnh
    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            // Lấy tên tệp một cách an toàn (loại bỏ đường dẫn nếu trình duyệt gửi lên cả đường dẫn)
            String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            
            // Lưu vào thư mục target/classes/static/images/ để hiển thị ngay lập tức
            Path targetPath = Paths.get(System.getProperty("user.dir"), "target", "classes", "static", "images", fileName);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            // Lưu vào thư mục src/main/resources/static/images/ để lưu giữ lâu dài trong dự án
            Path srcPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images", fileName);
            Files.createDirectories(srcPath.getParent());
            Files.write(srcPath, file.getBytes());

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //save sp
    @PostMapping(value = "/add/save", consumes = "multipart/form-data")
    public String savesp(@ModelAttribute Product product, @RequestParam("imageFile") MultipartFile imageFile, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/products";
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            product.setImageUrl(fileName);
        }
        service.addProduct(product);
        return "redirect:/products";
    }
    //edit form
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/products";
        model.addAttribute("sp",service.getProductById(id));
        model.addAttribute("categories",serviceCate.getAllCategory());
        return "edit-sp";
    }
    //update edit
    @PostMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public String upd_sp(@ModelAttribute Product sp, @PathVariable Long id, @RequestParam("imageFile") MultipartFile imageFile, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/products";
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            sp.setImageUrl(fileName);
        } else {
            // Giữ lại ảnh cũ nếu không tải ảnh mới lên
            Product oldProduct = service.getProductById(id);
            sp.setImageUrl(oldProduct.getImageUrl());
        }
        service.updateProduct(id,sp);
        return "redirect:/products";
    }
    //del
    @GetMapping("/del/{id}")
    public String del(@PathVariable Long id, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/products";
        service.deleteProduct(id);
        return "redirect:/products";
    }
    //buy pd
    @GetMapping("/buy/{id}")
    public String buy(
            @PathVariable Long id,
            HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Product product = service.getProductById(id);
        try {
            serviceOrder.buy_SP(user, product);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xác nhận mua thành công: " + product.getProductName()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Mua hàng thất bại: Sản phẩm " + product.getProductName() + " đã hết hàng hoặc không đủ tồn kho!"
            );
        }
        return "redirect:/";
    }
    //tim kiem bằng tên
    @GetMapping("/find")
    public String findName(@RequestParam("name") String name,Model model){
        model.addAttribute("products",service.find_NameSP(name));
        model.addAttribute("categories",serviceCate.getAllCategory());
        return "kh_home";
    }
    @GetMapping("/shop/category")
    public String filterCategory(@RequestParam(name = "id", required = false) String id, Model model) {
        if (id == null || id.trim().isEmpty()) {
            model.addAttribute("products", service.getAllProducts());
        } else {
            try {
                Long categoryId = Long.parseLong(id);
                model.addAttribute("products", service.find_byCate(categoryId));
            } catch (NumberFormatException e) {
                model.addAttribute("products", service.getAllProducts());
            }
        }
        model.addAttribute("categories", serviceCate.getAllCategory());
        return "kh_home";
    }
    // xem chi tiết sản phẩm
    @GetMapping("/product/{id}")
    public String detail(@PathVariable Long id, Model model){
        model.addAttribute("product", service.getProductById(id));
        return "product-detail";
    }
}
