package com.example.shopapp.services.Product;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Category;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.repositories.ProductImageRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.ReviewRepository;
import com.example.shopapp.response.ProductResponse;
import com.example.shopapp.services.FileStorageService.FileStorageService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException, IOException {
        // 1. Kiểm tra category
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find category with id: " + productDTO.getCategoryId()));

        // 2. Lưu thumbnail nếu có
        String thumbnail = null;
        if (productDTO.getThumbnail() != null && !productDTO.getThumbnail().isEmpty()) {
            thumbnail = fileStorageService.storeFile(productDTO.getThumbnail());
        }

        // 3. Tạo sản phẩm
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(thumbnail)
                .category(existingCategory)
                .description(productDTO.getDescription())
                .build();
        productRepository.save(newProduct);

        // 4. Xử lý và lưu các product images
        List<MultipartFile> imageFiles = productDTO.getProductImages();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            int existingImageCount = 0;
            for (MultipartFile image : imageFiles) {
                if (existingImageCount >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) break;

                String imageFilename = fileStorageService.storeFile(image);
                ProductImage productImage = ProductImage.builder()
                        .product(newProduct)
                        .imageUrl(imageFilename)
                        .build();
                productImageRepository.save(productImage);
                existingImageCount++;
            }
        }
        return newProduct;
    }

    @Override
    public ProductResponse getDetailProducts(long productId) throws DataNotFoundException {
        Optional<Product> optionalProduct = productRepository.getDetailProducts(productId);
        if (optionalProduct.isPresent()) {
            Double averageRating = reviewRepository.getAverageRatingByProductId(optionalProduct.get().getId());
            Long totalReviews = reviewRepository.countReviewsByProductId(optionalProduct.get().getId());
            return ProductResponse.fromProductWithRating(optionalProduct.get(), averageRating, totalReviews);
        }
        throw new DataNotFoundException(MessageKeys.PRODUCT_NOT_FOUND);
    }

    @Override
    public Product getProductById(Long id) throws DataNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id "+ id));
    }

    @Override
    public List<ProductResponse> findProductsByIds(List<Long> productIds){
        List<Product> listProduct = productRepository.findProductsByIds(productIds);
        return listProduct.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public Page<ProductResponse> getAllProducts(
            String keyword,
            Long categoryId,
            PageRequest pageRequest) {
        // lấy danh sách sản phầm theo trang và giới hạn
        Page<Product> productsPage;
        productsPage = productRepository.searchProducts(categoryId, keyword, pageRequest);

        return productsPage.map(product -> {
            // Lấy thông tin rating cho từng product
            Double averageRating = reviewRepository.getAverageRatingByProductId(product.getId());
            Long totalReviews = reviewRepository.countReviewsByProductId(product.getId());
            return ProductResponse.fromProductWithRating(product, averageRating, totalReviews);
        });
    }



    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) throws DataNotFoundException, IOException {
        Product existingProduct = getProductById(id);
        Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find category with id: "+productDTO.getCategoryId()));

        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(existingCategory);
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDescription(productDTO.getDescription());
        if (productDTO.getThumbnail() != null && !productDTO.getThumbnail().isEmpty()) {
            String thumbnailUrl = fileStorageService.storeFile(productDTO.getThumbnail());
            existingProduct.setThumbnail(thumbnailUrl);
        }

        List<MultipartFile> newImages = productDTO.getProductImages();
        if (newImages != null && !newImages.isEmpty()) {
            productImageRepository.deleteByProductId(existingProduct.getId());

            int count = 0;
            for (MultipartFile image : newImages) {
                if (count >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) break;
                String imageName = fileStorageService.storeFile(image);
                ProductImage productImage = ProductImage.builder()
                        .product(existingProduct)
                        .imageUrl(imageName)
                        .build();
                productImageRepository.save(productImage);
                count++;
            }
        }

        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws DataNotFoundException, InvalidParamException {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find product with id: "+productImageDTO.getProductId()));

        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        // ko cho insert quá 5 ảnh cho 1 sản phẩm
        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new InvalidParamException("Number of images must be <= "+ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        if (existingProduct.getThumbnail() == null) {
            existingProduct.setThumbnail(newProductImage.getImageUrl());
        }
        productRepository.save(existingProduct);
        return productImageRepository.save(newProductImage);
    }
}
