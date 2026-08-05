package com.market.market;

import com.market.market.dao.OrderDaoNoSQL;
import com.market.market.dao.ProductDaoSQL;
import com.market.market.model.Order;
import com.market.market.model.Product;
import com.market.market.service.ShoppingCartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest // Inicializa el contexto de Spring Boot con conexiones reales a Podman
@ActiveProfiles("test")
class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ProductDaoSQL productDao; // Lo usamos para inyectar datos de prueba en Postgres

    @Autowired
    private OrderDaoNoSQL orderDao;   // Lo usamos para validar y limpiar MongoDB

    private Product barcodeProduct;

    @BeforeEach
    void setUp() {
        // Aseguramos un entorno limpio antes de arrancar cada test
        cleanupDatabases();

        // 1. Insertamos un producto real en el catálogo de PostgreSQL (Simulando stock previo)
        barcodeProduct = new Product();
        barcodeProduct.setEan("7501055301234");
        barcodeProduct.setPrice(120.00);
        barcodeProduct.setDescription("Soda Bottle 1.5L");
        barcodeProduct.setBrand("SodaCorp");
        barcodeProduct.setCategory("Beverages");
        barcodeProduct.setDimension("1.5L");
        barcodeProduct.setExpirationDate(LocalDate.now().plusMonths(12));
        barcodeProduct.setStock(100);

        productDao.save(barcodeProduct);
    }

    @AfterEach
    void tearDown() {
        // Dejamos las bases de datos limpias al terminar el test
        cleanupDatabases();
    }

    private void cleanupDatabases() {
        shoppingCartService.clearShoppingCart();
        orderDao.deleteAll(); // Vacía la colección 'orders' de MongoDB
        productDao.deleteAll(); // Vacía la tabla 'products' de PostgreSQL
    }

    // ===================================================================
    // INTEGRATION TESTS
    // ===================================================================

    @Test
    @DisplayName("Integration - Scan same product multiple times updates memory cart correctly")
    void scanProduct_ShouldIncrementQuantityAndCalculateSubtotals_WhenProductExists() {
        // Act - Simulamos que el cajero pasa el mismo producto 2 veces por el escáner
        shoppingCartService.scanProduct("7501055301234");
        shoppingCartService.scanProduct("7501055301234");

        // Assert - Obtenemos el estado actual del carrito en memoria
        Order currentCart = shoppingCartService.getActiveShoppingCart();

        assertNotNull(currentCart);
        assertEquals(1, currentCart.getItems().size()); // Solo un tipo de ítem en la lista

        Order.ShoppingCartItem cartItem = currentCart.getItems().get(0);
        assertEquals("7501055301234", cartItem.getEan());
        assertEquals(2, cartItem.getQuantity()); // Cantidad debe ser 2
        assertEquals(240.00, cartItem.getSubtotal()); // 120.00 * 2
        assertEquals(240.00, currentCart.getTotalAmount()); // Total general del carrito
    }

    @Test
    @DisplayName("Integration - Scan non-existent EAN should throw exception from Postgres check")
    void scanProduct_ShouldThrowException_WhenBarcodeDoesNotExistInPostgres() {
        // Act & Assert - Si el escáner lee un código inventado, debe saltar la validación
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.scanProduct("9999999999999");
        });

        assertEquals("Product with EAN 9999999999999 not found in store catalog.", exception.getMessage());
    }

    @Test
    @DisplayName("Integration - Full Flow: Scan items and complete checkout storing document in MongoDB")
    void checkout_ShouldSaveFinalOrderInMongoDBAndResetActiveCart() {
        // 1. Escaneamos un producto válido
        shoppingCartService.scanProduct("7501055301234");

        // 2. Procesamos el cobro final (Checkout)
        Order savedOrder = shoppingCartService.checkout();

        // 3. Validaciones del documento retornado
        assertNotNull(savedOrder.getId()); // MongoDB debe haberle asignado su ID alfanumérico único
        assertEquals(120.00, savedOrder.getTotalAmount());

        // 4. Validación de persistencia real en MongoDB NoSQL
        Optional<Order> mongoDatabaseRecord = orderDao.findById(savedOrder.getId());
        assertTrue(mongoDatabaseRecord.isPresent());
        assertEquals(1, mongoDatabaseRecord.get().getItems().size());
        assertEquals("Soda Bottle 1.5L", mongoDatabaseRecord.get().getItems().get(0).getDescription());

        // 5. Validación de reinicio: El carrito activo del servidor debe haber quedado totalmente vacío
        Order postCheckoutCart = shoppingCartService.getActiveShoppingCart();
        assertTrue(postCheckoutCart.getItems().isEmpty());
        assertEquals(0.0, postCheckoutCart.getTotalAmount());
    }

    @Test
    @DisplayName("Integration - Checkout on empty cart should throw IllegalStateException")
    void checkout_ShouldThrowException_WhenCartIsEmpty() {
        // Act & Assert - No se puede cerrar una venta de 0 artículos
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            shoppingCartService.checkout();
        });

        assertEquals("Cannot process checkout. Shopping cart is empty.", exception.getMessage());
    }
}
