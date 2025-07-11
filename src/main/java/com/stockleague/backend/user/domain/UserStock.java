package com.stockleague.backend.user.domain;

import com.stockleague.backend.stock.domain.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_stocks")
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "locked_quantity", nullable = false)
    private BigDecimal lockedQuantity;

    /**
     * 매도 주문 생성 시 동결: 보유 수량에서 주문 수량만큼 차감 후 locked로 이동
     *
     * @param amount 주문 수량
     */
    public void lockQuantity(BigDecimal amount) {
        if (this.quantity.compareTo(amount) < 0) {
            throw new IllegalArgumentException("보유 수량보다 많은 수량을 동결할 수 없습니다.");
        }
        this.quantity = this.quantity.subtract(amount);
        this.lockedQuantity = this.lockedQuantity.add(amount);
    }

    /**
     * 주문 취소 시: 동결된 수량을 다시 보유 수량으로 되돌림
     *
     * @param amount 취소 수량
     */
    public void unlockQuantity(BigDecimal amount) {
        this.lockedQuantity = this.lockedQuantity.subtract(amount);
        this.quantity = this.quantity.add(amount);
    }

    /**
     * 실제 체결 완료 시: locked 수량에서 최종 체결된 수량만큼 차감 (완전 소멸)
     *
     * @param amount 최종 체결 수량
     */
    public void executeSell(BigDecimal amount) {
        this.lockedQuantity = this.lockedQuantity.subtract(amount);
    }

    /**
     * 주어진 수량만큼 매도 가능한지 확인합니다.
     * <ul>
     *     <li>보유한 주식 수량(quantity)이 매도 요청 수량 이상인지 여부를 반환합니다.</li>
     *     <li>lockedQuantity는 고려하지 않습니다.</li>
     * </ul>
     *
     * @param amount 매도 요청 수량
     * @return 매도 가능 여부 (true: 보유 수량 이상, false: 부족)
     */
    public boolean hasEnoughForSell(BigDecimal amount) {
        return this.quantity.compareTo(amount) >= 0;
    }

    /**
     * 주어진 수량만큼 보유 주식 수량을 증가시킵니다.
     *
     * @param amount 증가할 수량
     */
    public void increaseQuantity(BigDecimal amount) {
        this.quantity = this.quantity.add(amount);
    }
}
