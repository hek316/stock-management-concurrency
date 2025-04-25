package hek.stock.facade;

import hek.stock.domain.Stock;
import hek.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OptimisticLockStockFacadeTest {
    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void setUp() {
        // 테스트 실행 전 DB 에 상품 1의 재고 100 인 값 insert
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCound = 100;
        // ExecutorService 는 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 자바 API
        // java 의 스레드 풀 기반 비동기 작업 처리를 위한 핵심 API ,멀티쓰레드용 도구 모음
        // 스레드 재사용으로 생성 비용을 줄이고 스레드 크기를 관리할 수 있어 메모리를 제어할 수 있다.
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch 은 다른 스레드에서 실행중인 작업이 완료될때 까지 기다릴 수 있도록 도와주는 클래스
        // 여기서는 100 개의 작업이 모드 끝날 때까지 메인 스레드를 대기 시키는데 사용
        CountDownLatch countDownLatch = new CountDownLatch(threadCound);


        for (int i = 0; i < threadCound; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decreaseStock(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        // count 값이 0 보다 크면 , 현재 스레드를 wating 상태로 전환시키고 LockSupport.park()로 진입
        // 즉 메인 스레드가 여기서 멈춰서 잠시 대기
        countDownLatch.await();
        // 다른 스레드들이 countDown()을 호출해서 count가 0이 되면, 내부적으로 AQS.releaseShared() → LockSupport.unpark(대기 중인 스레드) 호출
        // 그제서야 메인 스레드가 다시 runnable 상태로 깨어나 다음 줄 실행

        // await() 는 불럭킹
        //  countDown() × N번 → count가 0 → 대기 스레드 깨움

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - (1*100)) = 0
        assertEquals(0, stock.getQuantity());
    }

}