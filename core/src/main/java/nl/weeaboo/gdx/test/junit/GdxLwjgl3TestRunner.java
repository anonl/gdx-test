package nl.weeaboo.gdx.test.junit;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

public class GdxLwjgl3TestRunner extends BlockJUnit4ClassRunner {

    private static final int MAX_STARTUP_TIME_SEC = 30;
    private static final int MAX_RUN_TIME_SEC = 900; // 15 minutes

    // Audio is unnecessary and may crash when on a build server
    private static final boolean DISABLE_AUDIO = true;

    public GdxLwjgl3TestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
        if (GraphicsEnvironment.isHeadless()) {
            // Integration tests require a GL context, so they don't work in a headless env
            testNotifier.fireTestIgnored();
        } else {
            try {
                runInRenderThread(new Runnable() {
                    @Override
                    public void run() {
                        GdxLwjgl3TestRunner.super.run(notifier);
                    }
                });
            } catch (InterruptedException e) {
                testNotifier.addFailure(e);
            }
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        // Clear backbuffer between tests
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

        super.runChild(method, notifier);
    }

    private void runInRenderThread(final Runnable runner) throws InterruptedException {
        final Semaphore initLock = new Semaphore(0);
        final Semaphore runLock = new Semaphore(0);

        /*
         * Workaround for libGDX issue; Lwjgl3Application constructor contains an infinite loop (lolwut), so
         * we have to create it in a background thread.
         */
        final Thread initThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.disableAudio(DISABLE_AUDIO);
                config.setInitialVisible(false);

                @SuppressWarnings("unused")
                Lwjgl3Application app = new Lwjgl3Application(new ApplicationAdapter() {
                    @Override
                    public void create() {
                        initLock.release();
                    }

                    @Override
                    public void render() {
                        try {
                            runner.run();
                        } finally {
                            runLock.release();
                        }
                    }
                }, config);

                GLFW.nglfwSetErrorCallback(0L);
                GLFW.glfwInit();
                GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
            }
        });
        initThread.start();

        try {
            // Wait for init
            Assert.assertTrue(initLock.tryAcquire(1, MAX_STARTUP_TIME_SEC, TimeUnit.SECONDS));

            // Wait for tests to run
            Assert.assertTrue(runLock.tryAcquire(1, MAX_RUN_TIME_SEC, TimeUnit.SECONDS));
        } finally {
            final Application app = Gdx.app;
            if (app != null) {
                app.exit();
            }

            initThread.join(30000);
        }
    }

}
