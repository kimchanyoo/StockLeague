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

    private static final BigDecimal ZERO = BigDecimal.ZERO;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "session", nullable = false)
    private OrderSession session;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderExecution> orderExecutions = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ReservedCash reservedCash;

    /**
     * 이번 배치의 체결 결과(증분)를 누적으로 반영한다.
     * @param deltaExecutedAmount 이번 배치 체결 수량 (Σ matched)
     * @param deltaExecutedValue  이번 배치 체결 금액 합 (Σ price*matched)
     */
    public void applyExecutionDelta(BigDecimal deltaExecutedAmount, BigDecimal deltaExecutedValue) {
        if (deltaExecutedAmount == null || deltaExecutedAmount.signum() <= 0) {
            return;
        }

        BigDecimal prevAmt   = nz(this.executedAmount);
        BigDecimal prevValue = (prevAmt.signum() > 0 && this.averageExecutedPrice != null)
                ? this.averageExecutedPrice.multiply(prevAmt)
                : ZERO;

        BigDecimal newAmt    = prevAmt.add(deltaExecutedAmount);
        BigDecimal newValue  = prevValue.add(nz(deltaExecutedValue)); // 누적 체결 금액 합

        this.executedAmount  = newAmt.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rem = nz(this.orderAmount).subtract(newAmt);
        if (rem.signum() < 0) rem = ZERO; // 하한 보정
        this.remainingAmount = rem.setScale(2, RoundingMode.HALF_UP);

        if (newAmt.signum() > 0) {
            this.averageExecutedPrice = newValue.divide(newAmt, 2, RoundingMode.HALF_UP);
        }

        if (this.remainingAmount.signum() == 0) {
            this.status = OrderStatus.EXECUTED;
            this.executedAt = LocalDateTime.now();
        } else if (newAmt.signum() > 0) {
            this.status = OrderStatus.PARTIALLY_EXECUTED;
            this.executedAt = LocalDateTime.now();
        }
    }

    public void markAsCanceled() {
        if (this.status == OrderStatus.PARTIALLY_EXECUTED) {
            this.status = OrderStatus.CANCELED_AFTER_PARTIAL;
        } else {
            this.status = OrderStatus.CANCELED;
        }
    }

    public boolean isCompletedOrCanceled() {
        return this.status == OrderStatus.EXECUTED
                || this.status == OrderStatus.CANCELED
                || this.status == OrderStatus.CANCELED_AFTER_PARTIAL;
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? ZERO : v; }

    public void cancelBySystem() {
        this.setStatus(OrderStatus.CANCELED);
    }
}
