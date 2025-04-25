package hek.stock.service;

import hek.stock.domain.Stock;
import hek.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 부모의 트랜잭션과 별도로 실행되어야 함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void decreaseStock(Long id, Long quantity) {
        // Stock 조회

        Stock stock = stockRepository.findById(id).orElseThrow();
        // 재고를 감소한뒤
        stock.decrease(quantity);
        // 갱신된 값을 저장

        // 영속성 컨텍스트에 저장하고, 즉시 DB 에 반영 (쓰기 지연 (flush) 강제 수행)
        stockRepository.saveAndFlush(stock);
    }
}
