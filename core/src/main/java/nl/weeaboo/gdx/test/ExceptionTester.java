package nl.weeaboo.gdx.test;

import java.util.concurrent.Callable;

/**
 * Convenience class for testing that certain method calls throw a particular kind of exception. The standard
 * ways to test for an exception in JUnit don't catch the exception, so only one exception per test may be
 * thrown.
 */
public class ExceptionTester {

    public void expect(Class<? extends Exception> expected, final Runnable runnable) {
        expect(expected, new Callable<Void>() {
            @Override
            public Void call() {
                runnable.run();
                return null;
            }
        });
    }

    public void expect(Class<? extends Exception> expected, Callable<?> callable) {
        try {
            callable.call();
            throw new AssertionError("Expected an exception");
        } catch (Exception e) {
            if (!expected.isInstance(e)) {
                throw new AssertionError(e);
            }
        }
    }

}
