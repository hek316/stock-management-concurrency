package hek.stock.facade;

import hek.stock.repository.LockRepository;
import hek.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class NamedLockStockFacade {
    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    public void decreaseStock(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decreaseStock(id, quantity);
        } finally {
          lockRepository.releaseLock(id.toString());
        }
    }
}
