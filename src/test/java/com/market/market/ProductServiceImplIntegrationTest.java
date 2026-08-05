package com.market.market;

import com.market.market.model.Product;
import com.market.market.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Levanta el contexto real de Spring Boot e inyecta dependencias reales
@Transactional   // Hace rollback automático en Postgres al terminar cada test para no dejar basura
class ProductServiceImplIntegrationTest {

    @Autowired
    private ProductService productService; // Inyección del servicio real sin simulaciones

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Creamos un producto base real para las pruebas cotidianas
        testProduct = new Product();
        testProduct.setEan("1234567890123");
        testProduct.setPrice(45.99);
        testProduct.setDescription("Real Integration Product");
        testProduct.setBrand("RealBrand");
        testProduct.setCategory("Beverages");
        testProduct.setDimension("500ml");
        testProduct.setExpirationDate(LocalDate.now().plusMonths(3));
        testProduct.setStock(50);
    }

    @Test
    @DisplayName("Integration - Create and Find Product by EAN in Real DB")
    void createAndGet_ShouldPersistAndRetrieveFromPostgres() {
        // Act - Guardamos de verdad el producto en la base de datos de Podman
        Product savedProduct = productService.create(testProduct);

        // Assert - Validamos que la base de datos le haya asignado un ID autoincremental real
        assertNotNull(savedProduct.getId());

        // Act 2 - Buscamos el producto ejecutando la query real de Postgres por EAN
        Optional<Product> foundProductOpt = productService.getByEan("1234567890123");

        // Assert 2 - Verificamos que los datos se recuperen de forma idéntica
        assertTrue(foundProductOpt.isPresent());
        Product foundProduct = foundProductOpt.get();
        assertEquals("Real Integration Product", foundProduct.getDescription());
        assertEquals(45.99, foundProduct.getPrice());
    }

    @Test
    @DisplayName("Integration - Create Duplicate EAN Should Throw Exception")
    void create_ShouldThrowException_WhenEanAlreadyExistsInRealDB() {
        // Arrange - Insertamos el primer producto real
        productService.create(testProduct);

        // Creamos un segundo producto con el mismo EAN para forzar la colisión
        Product duplicateProduct = new Product();
        duplicateProduct.setEan("1234567890123"); // Mismo EAN
        duplicateProduct.setDescription("I am a duplicate");
        duplicateProduct.setPrice(10.00);

        // Act & Assert - Comprobamos que nuestra regla de negocio valide contra la BD real
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.create(duplicateProduct);
        });

        assertEquals("The EAN code 1234567890123 already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("Integration - Get All Paginated from Real DB")
    void getAll_ShouldReturnRealPagination() {
        // Arrange - Insertamos el producto de prueba
        productService.create(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // Act - Pedimos la página real de la base de datos
        Page<Product> pageResult = productService.getAll(pageable);

        // Assert - Verificamos que devuelva la estructura de paginación con contenido real
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotalElements() >= 1);
    }

    @Test
    @DisplayName("Integration - Update Product fields by EAN")
    void updateByEan_ShouldModifyDatabaseRecord() {
        // Arrange - Guardamos el registro inicial
        productService.create(testProduct);

        // Preparamos los datos nuevos
        Product updatedData = new Product();
        updatedData.setPrice(55.00);
        updatedData.setStock(100);
        updatedData.setDescription("Updated Via Integration Test");

        // Act - Ejecutamos la actualización real
        Product result = productService.updateByEan("1234567890123", updatedData);

        // Assert - Validamos que el cambio haya impactado
        assertEquals(55.00, result.getPrice());
        assertEquals(100, result.getStock());
        assertEquals("Updated Via Integration Test", result.getDescription());
    }
}
