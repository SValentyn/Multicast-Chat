package logic;

import javafx.application.Platform;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class is taken from here: http://www.guigarage.com/2013/01/invokeandwait-for-javafx
 */
class FXUtilities {

    /**
     * Simple helper class.
     */
    private static class ThrowableWrapper {
        Throwable throwable;
    }

    /**
     * Invokes a Runnable in JFX Thread and waits while it's finished.
     * Like SwingUtilities.invokeAndWait does for EDT.
     */
    static void runAndWait(final Runnable run) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            try {
                run.run();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        } else {
            final Lock lock = new ReentrantLock();
            final Condition condition = lock.newCondition();
            final ThrowableWrapper throwableWrapper = new ThrowableWrapper();

            lock.lock();
            try {
                Platform.runLater(() -> {
                    lock.lock();
                    try {
                        run.run();
                    } catch (Throwable throwable) {
                        throwableWrapper.throwable = throwable;
                    } finally {
                        try {
                            condition.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                });

                condition.await();
                if (throwableWrapper.throwable != null) {
                    throw new ExecutionException(throwableWrapper.throwable);
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
