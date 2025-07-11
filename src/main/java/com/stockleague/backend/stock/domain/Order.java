package com.stockleague.backend.stock.domain;

import com.stockleague.backend.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_stock"))
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.WAITING;

    @Column(name = "order_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal orderPrice;

    @Column(name = "order_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal orderAmount;

    @Builder.Default
    @Column(name = "executed_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal executedAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "average_executed_price", precision = 20, scale = 2)
    private BigDecimal averageExecutedPrice;

    @CreatedDate
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderExecution> orderExecutions = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ReservedCash reservedCash;

    public void updateExecutionInfo(BigDecimal executedAmount, BigDecimal totalExecutedPrice) {
        this.executedAmount = executedAmount;
        this.remainingAmount = this.orderAmount.subtract(executedAmount);

        if (executedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.averageExecutedPrice = totalExecutedPrice.divide(executedAmount, 2, RoundingMode.HALF_UP);
        }

        if (this.remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = OrderStatus.EXECUTED;
            this.executedAt = LocalDateTime.now();
        } else if (this.remainingAmount.compareTo(this.orderAmount) < 0) {
            this.status = OrderStatus.PARTIALLY_EXECUTED;
            this.executedAt = LocalDateTime.now();
        }
    }
}
