package com.example.shopapp.controller;


import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.ProductResponse;
import com.example.shopapp.services.FileStorageService.FileStorageService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
public class ProductController extends TranslateMessages {
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ) throws DataNotFoundException, IOException {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.MESSAGE_ERROR_GET))
                    .payload(errorMessages)
                    .build());
        }
        Product newProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.CREATE_PRODUCT_SUCCESS))
                .payload(ProductResponse.fromProduct(newProduct))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //http://localhost:8080/api/v1/products/uploads/{id}
    public ResponseEntity<ApiResponse<?>> uploadImages(
            @PathVariable("id") Long productId,
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {
        Product existingProduct = productService.getProductById(productId);
        files = files == null ? new ArrayList<>() : files;

        if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.FILES_REQUIRED))
                    .build());
        }

        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.getSize() == 0) continue;

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.builder()
                        .success(false)
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.FILES_IMAGES_SIZE_FAILED))
                        .build());
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResponse.builder()
                        .success(false)
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.FILES_IMAGES_TYPE_FAILED))
                        .build());
            }

            String filename = fileStorageService.storeFile(file);
            ProductImage productImage = productService.createProductImage(
                    existingProduct.getId(),
                    ProductImageDTO.builder().imageUrl(filename).build()
            );
            productImages.add(productImage);
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.CREATE_PRODUCT_IMAGES_SUCCESS))
                .payload(productImages)
                .build());
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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductDTO productDTO
    ) throws DataNotFoundException, IOException {
        Product updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.UPDATE_PRODUCT_SUCCESS))
                .payload(ProductResponse.fromProduct(updatedProduct))
                .build());
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<ProductResponse> productPage = (categoryId == 0)
                ? productService.getAllProducts(keyword, null, pageRequest)
                : productService.getAllProducts(keyword, categoryId, pageRequest);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(productPage)
                .build());
    }

    @GetMapping("/by-ids")
    public ResponseEntity<ApiResponse<?>> getProductById(@RequestParam("ids") String ids) {
        List<Long> productIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        List<ProductResponse> productResponses = productService.findProductsByIds(productIds);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(productResponses)
                .build());
    }

    //    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_USER')")
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<?>> getProductDetailsById(@RequestParam("id") Long id) throws Exception {
        ProductResponse detail = productService.getDetailProducts(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(detail)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.MESSAGE_DELETE_SUCCESS, id))
                .payload(id)
                .build());
    }

//    @PostMapping("/generateFakeProducts")
//    public ResponseEntity<String> generateFakeProducts(){
//        Faker faker = new Faker();
//        for(int i = 0; i<100; i++){
//            String productName = faker.commerce().productName();
//            if(productService.existsByName(productName)){
//                continue;
//            }
//            ProductDTO productDTO = ProductDTO.builder()
//                    .name(productName)
//                    .price((float)faker.number().numberBetween(10000,10000000))
//                    .categoryId((long)faker.number().numberBetween(2, 4))
//                    .description(faker.lorem().sentence())
//                    .build();
//            try {
//                productService.createProduct(productDTO);
//            } catch (DataNotFoundException e) {
//                return ResponseEntity.badRequest().body(e.getMessage());
//            }
//        }
//        return ResponseEntity.ok("Fake Products created successfully");
//    }
}
