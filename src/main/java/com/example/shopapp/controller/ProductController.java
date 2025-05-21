package com.example.shopapp.controller;


import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.response.ProductListResponse;
import com.example.shopapp.response.ProductResponse;
import com.example.shopapp.services.Product.IProductService;
import com.example.shopapp.utils.MessageKeys;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    //http://localhost:8080/api/v1/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ){
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Product newProduct = productService.createProduct(productDTO);


            return ResponseEntity.ok(newProduct);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//    {
//            "name": "macbook air 15 inch 2025",
//            "price": 100000,
//            "thumbnail": "",
//            "description": "This is a test product",
//            "category_id": 1
//    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //http://localhost:8080/api/v1/products/uploads/{id}
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @RequestParam ("files") List<MultipartFile> files
    ) throws Exception {
        Product existingProduct = productService.getProductById(productId);
        files = files == null ? new ArrayList<MultipartFile>() : files;
        if(files.size()>ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
             return ResponseEntity.badRequest().body(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_UPLOAD_IMAGE_ERROR_OVER));
        }
        List<ProductImage> productImages = new ArrayList<>();
        for(MultipartFile file : files){
            if(file.getSize()==0){
                continue;
            }
            //Kiểm tra kích thước file và định dạng
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_UPLOAD_IMAGE_FILED_LARGE));
            }

            //Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_UPLOAD_IMAGE_FILED_MUST_BE_IMAGE));
            }
            // Lưu file và cập nhật thumbnail trong DTO
            String filename = storeFile(file);
            // Lưu đối tượng product trong DB
            ProductImage productImage = productService.createProductImage(
                    existingProduct.getId(), ProductImageDTO.builder()
                    .imageUrl(filename)
                    .build());
            productImages.add(productImage);
        }
        return ResponseEntity.ok().body(productImages);
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName){
        try{
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if(resource.exists()){
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }else{
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound.png").toUri()));
            }
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    private String storeFile(MultipartFile file) throws IOException{
        if(!isImageFile(file)||file.getOriginalFilename()==null){
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString()+"_"+filename;
        java.nio.file.Path uploadDir = Paths.get("uploads");
        //Kiểm tra và tạo thư mục nếu nó không tồn tại
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        // đường dẫn đầy đủ đê file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    private boolean isImageFile(MultipartFile file){
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO
    ){
        try{
            Product updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(ProductResponse.fromProduct(updatedProduct));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "")String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        // Tạo pageble từ thông tin page và limit
        PageRequest pageRequest = PageRequest.of(
                page, limit,
//                Sort.by("createdAt").descending()
                Sort.by("id").ascending()
                );
        Page<ProductResponse> productPage = productService.getAllProducts(keyword,categoryId,pageRequest);
        int totalPages = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();
//        return ResponseEntity.ok(ProductListResponse.builder()
//                        .products(products)
//                        .totalPages(totalPages)
//                .build());
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductById(@RequestParam("ids") String ids){
        try {
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Product> products = productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_USER')")
    @GetMapping("/details")
    public ResponseEntity<?> getProductDetailsById(@RequestParam("id") Long id) {
        try {
            Product existsProducts = productService.getDetailProducts(id);
            return ResponseEntity.ok(existsProducts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        try {
           productService.deleteProduct(id);
           return ResponseEntity.ok(String.format("Product with id = %d deleted successfully", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts(){
        Faker faker = new Faker();
        for(int i = 0; i<100; i++){
            String productName = faker.commerce().productName();
            if(productService.existsByName(productName)){
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10000,10000000))
                    .categoryId((long)faker.number().numberBetween(2, 4))
                    .description(faker.lorem().sentence())
                    .build();
            try {
                productService.createProduct(productDTO);
            } catch (DataNotFoundException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }
}
