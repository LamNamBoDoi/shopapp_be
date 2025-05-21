package com.example.shopapp.services.Product;

import com.example.shopapp.dtos.ProductDTO;
import com.example.shopapp.dtos.ProductImageDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidParamException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.ProductImage;
import com.example.shopapp.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws DataNotFoundException;
    Product getProductById(Long id) throws DataNotFoundException;
    Page<ProductResponse> getAllProducts(  String keyword, Long categoryId,PageRequest pageRequest);
    Product updateProduct(Long id, ProductDTO productDTO) throws DataNotFoundException;
    void deleteProduct(Long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO
    ) throws Exception;
    Product getDetailProducts(long productId) throws Exception;
    List<Product> findProductsByIds(List<Long> productIds);
}
