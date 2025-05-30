package com.example.shopapp.services.Product;

import com.example.shopapp.response.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductRedisService {
    // clear cache data in redis
    void clear();

    List<ProductResponse> getAllProducts(
      String keyword,
      Long categoryId,
      PageRequest pageRequest,
      String sortField,
      String sortDirection
    ) throws JsonProcessingException;

    void saveAllProducts(List<ProductResponse> productResponses,
                         String keyword,
                         Long categoryId,
                         PageRequest pageRequest,
                         String sortField,
                         String sortDirection) throws JsonProcessingException;
}
