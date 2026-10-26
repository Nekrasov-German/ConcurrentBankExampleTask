import org.junit.jupiter.api.Test;
import ru.german.BankAccount;
import ru.german.ConcurrentBank;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentBankTest {

    @Test
    void testCreateAccountAndTransfer() {
        ConcurrentBank bank = new ConcurrentBank();

        BankAccount acc1 = bank.createAccount(100);
        BankAccount acc2 = bank.createAccount(50);

        assertEquals(100, acc1.getBalance());
        assertEquals(50, acc2.getBalance());

        bank.transfer(acc1, acc2, 30);

        assertEquals(70, acc1.getBalance());
        assertEquals(80, acc2.getBalance());
    }

    @Test
    void testTransferSameAccount() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount acc = bank.createAccount(100);

        int before = acc.getBalance();
        bank.transfer(acc, acc, 50);
        assertEquals(before, acc.getBalance());
    }

    @Test
    void testTransferNegativeAmount() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount acc1 = bank.createAccount(100);
        BankAccount acc2 = bank.createAccount(50);

        assertThrows(IllegalArgumentException.class, () -> bank.transfer(acc1, acc2, -10));
        assertThrows(IllegalArgumentException.class, () -> bank.transfer(acc1, acc2, 0));
    }

    @Test
    void testWithdrawThrowsOnInsufficientFunds() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount acc1 = bank.createAccount(10);
        BankAccount acc2 = bank.createAccount(0);

        assertThrows(IllegalArgumentException.class, () -> bank.transfer(acc1, acc2, 20));

        assertEquals(10, acc1.getBalance());
        assertEquals(0, acc2.getBalance());
    }

    @Test
    void testConcurrentTransfers() throws Exception {
        ConcurrentBank bank = new ConcurrentBank();
        int initialSum = 1_000_000;
        BankAccount acc1 = bank.createAccount(initialSum);
        BankAccount acc2 = bank.createAccount(0);

        int totalThreads = 20;
        int transfersPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads * transfersPerThread);

        Runnable task = () -> {
            for (int i = 0; i < transfersPerThread; i++) {
                try {
                    if (i % 2 == 0) {
                        bank.transfer(acc1, acc2, 100);
                    } else {
                        bank.transfer(acc2, acc1, 100);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Если временно не хватает средств — пропускаем этот перевод
                } finally {
                    latch.countDown();
                }
            }
        };

        for (int t = 0; t < totalThreads; t++) {
            executor.submit(task);
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long finalSum = (long) acc1.getBalance() + acc2.getBalance();
        assertEquals(initialSum, finalSum, "Общий баланс должен сохраниться");

        assertTrue(acc1.getBalance() >= 0, "Баланс acc1 не должен быть отрицательным");
        assertTrue(acc2.getBalance() >= 0, "Баланс acc2 не должен быть отрицательным");
    }

    @Test
    void testGetTotalBalanceConsistency() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount acc1 = bank.createAccount(300);
        BankAccount acc2 = bank.createAccount(200);

        int sumViaMethod = bank.getTotalBalance();
        int manualSum = acc1.getBalance() + acc2.getBalance();

        assertEquals(manualSum, sumViaMethod);
    }
}
