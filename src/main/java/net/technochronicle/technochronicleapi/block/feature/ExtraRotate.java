package net.technochronicle.technochronicleapi.block.feature;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/// 额外的方向旋转枚举
public enum ExtraRotate implements Predicate<ExtraRotate>, StringRepresentable {
    ORIGINAL("none"),
    OPPOSITE("opposite"),
    ANTICLOCKWISE("anticlockwise"),
    CLOCKWISE("clockwise");
    private final String name;

    ExtraRotate(String name) {
        this.name = name;
    }

    @Override
    public boolean test(ExtraRotate extraRotate) {
        return this.equals(extraRotate);
    }

    private static final ThreadLocal<ExtraRotate> ROTATE = new ThreadLocal<>();

    public static ExtraRotate get() {
        return ROTATE.get();
    }

    public static void set(ExtraRotate extraRotate) {
        ROTATE.set(extraRotate);
    }

    public static void clear() {
        ROTATE.remove();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static ExtraRotate transform(Direction direction) {
        return switch (direction) {
            case SOUTH -> CLOCKWISE;
            case EAST -> OPPOSITE;
            case WEST -> ANTICLOCKWISE;
            default -> ORIGINAL;
        };
    }
}