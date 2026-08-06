package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.OrderState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @ElementCollection
    @CollectionTable(name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private OrderState state;

    @Column(name = "delivery_weight")
    private Double deliveryWeight;

    @Column(name = "delivery_volume")
    private Double deliveryVolume;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "delivery_price")
    private Double deliveryPrice;

    @Column(name = "product_price")
    private Double productPrice;

    @Column(name = "username")
    private String username;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "delivery_address_country")),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city")),
            @AttributeOverride(name = "street", column = @Column(name = "delivery_address_street")),
            @AttributeOverride(name = "house", column = @Column(name = "delivery_address_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "delivery_address_flat"))
    })
    private AddressDto deliveryAddress;
}