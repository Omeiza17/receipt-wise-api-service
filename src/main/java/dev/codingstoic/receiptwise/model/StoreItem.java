package dev.codingstoic.receiptwise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString(exclude = {"store", "item"})
@Table(name = "store_items", schema = "public")
public class StoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            columnDefinition = "UUID DEFAULT gen_random_uuid()",
            updatable = false,
            insertable = false
    )
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

}
