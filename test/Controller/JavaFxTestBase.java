package Controller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

/**
 * Shared base for the Controller-layer unit tests.
 *
 * <p>Constructing JavaFX controls (ComboBox, TextField, TextArea, Label ...)
 * requires the JavaFX toolkit to be running. This base boots it exactly once
 * for the whole JVM via a static initializer and exposes small reflection
 * helpers used to inject collaborators into the controllers' private
 * {@code @FXML} fields and to invoke their (mostly private) logic methods.</p>
 *
 * <p>No TestFX or extra Maven dependency is introduced (per the group
 * constraints); only the JDK, JavaFX runtime already on the classpath, and
 * reflection are used.</p>
 */
public abstract class JavaFxTestBase {

    static {
        startToolkit();
    }

    /** Boots the JavaFX toolkit once; safe to call repeatedly. */
    protected static void startToolkit() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit already running (Platform.startup can only run once) - fine.
        } catch (Throwable ignore) {
            // Leave uninitialized; individual tests will surface the cause.
        }
    }

    /**
     * Runs the supplied action on the JavaFX Application Thread and blocks until
     * it finishes, re-throwing any error so assertions/exceptions surface in the
     * calling test thread. Running control-touching code on the FX thread avoids
     * "not on FX application thread" exceptions.
     */
    protected static void runOnFxThread(FxRunnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting for JavaFX action");
        }
        Throwable t = error.get();
        if (t instanceof Exception) {
            throw (Exception) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        }
    }

    /** A runnable whose body may throw a checked exception. */
    @FunctionalInterface
    protected interface FxRunnable {
        void run() throws Exception;
    }

    // <editor-fold desc="reflection helpers">

    /** Sets a private (usually {@code @FXML}) field on the target instance. */
    protected static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field '" + fieldName + "'", e);
        }
    }

    /** Invokes a private no-arg method and returns its result. */
    protected static Object invokePrivate(Object target, String methodName) {
        return invokePrivate(target, methodName, new Class<?>[0], new Object[0]);
    }

    /** Invokes a private method with the given parameter types/arguments. */
    protected static Object invokePrivate(Object target, String methodName,
                                          Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = findMethod(target.getClass(), methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Invocation of '" + methodName + "' failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke '" + methodName + "'", e);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>[] paramTypes)
            throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }

    // </editor-fold>
}
