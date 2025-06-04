package com.example.shopapp.repositories;

import com.example.shopapp.models.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Page<Product> findAll(Pageable pageable);// phân trang

    @Query("SELECT p FROM Product p WHERE " +
            "((:categoryId IS NULL OR :categoryId = 0) OR p.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("categoryId") Long categoryId,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    // Method mới - search với rating
    @Query("""
        SELECT p,
               COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) as avgRating,
               COUNT(CASE WHEN r.status = 'APPROVED' THEN r.id END) as totalReviews
        FROM Product p
        LEFT JOIN p.reviews r
        WHERE ((:categoryId IS NULL OR :categoryId = 0) OR p.category.id = :categoryId)
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY p.id
        HAVING (:minRating IS NULL OR :minRating = 0 OR 
                COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) >= :minRating)
        ORDER BY 
            CASE WHEN :sortBy = 'rating' THEN COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) END DESC,
            CASE WHEN :sortBy = 'price_asc' THEN p.price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN p.price END DESC,
            CASE WHEN :sortBy = 'name' THEN p.name END ASC,
            p.createdAt DESC
        """)
    List<Object[]> searchProductsWithRating(@Param("categoryId") Long categoryId,
                                            @Param("keyword") String keyword,
                                            @Param("minRating") Double minRating,
                                            @Param("sortBy") String sortBy);

    // Method search với rating và pagination (native query cho performance tốt hơn)
    @Query(value = """
        SELECT p.*, 
               COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) as avg_rating,
               COUNT(CASE WHEN r.status = 'APPROVED' THEN r.id END) as total_reviews
        FROM products p 
        LEFT JOIN reviews r ON p.id = r.product_id 
        WHERE ((:categoryId IS NULL OR :categoryId = 0) OR p.category_id = :categoryId) 
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY p.id, p.name, p.price, p.thumbnail, p.description, p.category_id, p.created_at, p.updated_at
        HAVING (:minRating IS NULL OR :minRating = 0 OR 
                COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) >= :minRating)
        ORDER BY 
            CASE WHEN :sortBy = 'rating' THEN COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) END DESC,
            CASE WHEN :sortBy = 'price_asc' THEN p.price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN p.price END DESC,
            CASE WHEN :sortBy = 'name' THEN p.name END ASC,
            p.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> searchProductsWithRatingPaged(@Param("categoryId") Long categoryId,
                                                 @Param("keyword") String keyword,
                                                 @Param("minRating") Double minRating,
                                                 @Param("sortBy") String sortBy,
                                                 @Param("limit") int limit,
                                                 @Param("offset") int offset);

    // Count query cho pagination
    @Query(value = """
        SELECT COUNT(DISTINCT p.id)
        FROM products p 
        LEFT JOIN reviews r ON p.id = r.product_id 
        WHERE ((:categoryId IS NULL OR :categoryId = 0) OR p.category_id = :categoryId) 
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY p.id
        HAVING (:minRating IS NULL OR :minRating = 0 OR 
                COALESCE(AVG(CASE WHEN r.status = 'APPROVED' THEN r.rating END), 0.0) >= :minRating)
        """, nativeQuery = true)
    int countSearchProductsWithRating(@Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword,
                                      @Param("minRating") Double minRating);

    // Method để lấy rating của 1 product cụ thể
    @Query("""
        SELECT COALESCE(AVG(r.rating), 0.0) 
        FROM Review r 
        WHERE r.product.id = :productId AND r.status = 'APPROVED'
        """)
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT COUNT(r) 
        FROM Review r 
        WHERE r.product.id = :productId AND r.status = 'APPROVED'
        """)
    Long getTotalReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages where p.id = :productId")
    Optional<Product> getDetailProducts(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);
}
