package net.technochronicle.technochronicleapi.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class DistExecutor {

    private static final Logger LOGGER = LogManager.getLogger();

    private DistExecutor() {}

    public static <T> T callWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
        return unsafeCallWhenOn(dist, toRun);
    }

    public static <T> T unsafeCallWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
        if (dist == FMLEnvironment.dist) {
            try {
                return toRun.get().call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static <T> T safeCallWhenOn(Dist dist, Supplier<SafeCallable<T>> toRun) {
        validateSafeReferent(toRun);
        return callWhenOn(dist, toRun::get);
    }

    public static void runWhenOn(Dist dist, Supplier<Runnable> toRun) {
        unsafeRunWhenOn(dist, toRun);
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == FMLEnvironment.dist) {
            toRun.get().run();
        }
    }

    public static void safeRunWhenOn(Dist dist, Supplier<SafeRunnable> toRun) {
        validateSafeReferent(toRun);
        if (dist == FMLEnvironment.dist) {
            toRun.get().run();
        }
    }

    public static <T> T runForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return unsafeRunForDist(clientTarget, serverTarget);
    }

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> clientTarget.get().get();
            case DEDICATED_SERVER -> serverTarget.get().get();
            default -> throw new IllegalArgumentException("UNSIDED?");
        };
    }

    public static <T> T safeRunForDist(Supplier<SafeSupplier<T>> clientTarget, Supplier<SafeSupplier<T>> serverTarget) {
        validateSafeReferent(clientTarget);
        validateSafeReferent(serverTarget);
        return switch (FMLEnvironment.dist) {
            case CLIENT -> clientTarget.get().get();
            case DEDICATED_SERVER -> serverTarget.get().get();
            default -> throw new IllegalArgumentException("UNSIDED?");
        };
    }

    public interface SafeReferent {}

    public interface SafeCallable<T> extends SafeReferent, Callable<T>, Serializable {}

    public interface SafeSupplier<T> extends SafeReferent, Supplier<T>, Serializable {}

    public interface SafeRunnable extends SafeReferent, Runnable, Serializable {}

    private static void validateSafeReferent(Supplier<? extends SafeReferent> safeReferentSupplier) {
        if (FMLEnvironment.production) return;
        final SafeReferent setter;
        try {
            setter = safeReferentSupplier.get();
        } catch (Exception e) {
            // Typically a class cast exception, just return out, expected.
            return;
        }

        for (Class<?> cl = setter.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                Method m = cl.getDeclaredMethod("writeReplace");
                m.setAccessible(true);
                Object replacement = m.invoke(setter);
                if (!(replacement instanceof SerializedLambda l))
                    break;// custom interface implementation
                if (Objects.equals(l.getCapturingClass(), l.getImplClass())) {
                    LOGGER.fatal("Detected unsafe referent usage, please view the code at {}", Thread.currentThread().getStackTrace()[3]);
                    throw new RuntimeException("Unsafe Referent usage found in safe referent method");
                }
            } catch (NoSuchMethodException ignored) {} catch (IllegalAccessException | InvocationTargetException e) {
                break;
            }
        }
    }
}
