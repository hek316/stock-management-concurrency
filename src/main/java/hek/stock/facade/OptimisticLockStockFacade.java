package hek.stock.facade;

import hek.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Service;


@Service
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decreaseStock(Long id, Long quantity) throws InterruptedException {
        while(true) {
            try {
                optimisticLockStockService.decreaseStock(id, quantity);
                break;

            }catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
