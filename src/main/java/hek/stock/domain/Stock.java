package hek.stock.domain;

import jakarta.persistence.*;

@Entity // 데이터베이스 테이블과 매핑되는 자바 클래스
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    @Version
    private Long version;

    public Stock() {}

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    // == 핵심 비즈니스 로직 == //
    // 엔티티 안에 비즈니스 로직 넣기
    // stock 감소 - 간단한 비즈니스 규칙은 엔티티에 포함시켜 응집성 높임
    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고는 0개 미만이 될 수 없습니다.");
        }

        this.quantity -= quantity;
    }
}
