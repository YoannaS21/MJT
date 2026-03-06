package threads;

public class BankAccount {

    // You can test removing explicit synchronization and use AtomicInteger here
    private static int opCount = 0;

    // Holder's name
    protected String name;

    // Balance
    protected double balance = 0;

    public BankAccount(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public BankAccount(String name) {
        this.name = name;
    }

    public synchronized void deposit(double amount) {
        this.balance += amount;
        incrementOpCount();
    }

    public synchronized void withdraw(double amount) {
        this.balance -= amount;
        incrementOpCount();
    }

    public String getName() {
        return name;
    }

    // is this thread-safe?
    public static void incrementOpCount() {
        opCount++;
    }

    // is this thread-safe?
    public static int getOpCount() {
        return opCount;
    }

    @Override
    public String toString() {
        return name + "'s balance is " + balance;
    }

    @SuppressWarnings("checkstyle:MethodLength")
    static void main() throws InterruptedException {

        BankAccount acc = new BankAccount("Alice", 0);

        int threadsCount = 10;
        int operationsPerThread = 100_000;

        Thread[] threads = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    acc.deposit(1);
                }
            });
        }

        // стартираме нишките
        for (Thread t : threads) {
            t.start();
        }

        // изчакваме всички да приключат
        for (Thread t : threads) {
            t.join();
        }

        // Очакван резултат:
        // balance = threadsCount * operationsPerThread
        // opCount = threadsCount * operationsPerThread

        System.out.println("Balance: " + acc.balance);
        System.out.println("Operation count: " + BankAccount.getOpCount());
    }
}
