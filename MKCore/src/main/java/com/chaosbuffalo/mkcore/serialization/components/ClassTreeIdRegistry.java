package com.chaosbuffalo.mkcore.serialization.components;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;

public class ClassTreeIdRegistry {
    public static final int NO_ID_VALUE = -1;
    private final Object2IntMap<Class<?>> classToLastIdCache = Util.make(
            new Object2IntOpenHashMap<>(), m -> m.defaultReturnValue(NO_ID_VALUE)
    );

    public int getLastIdFor(Class<?> class_) {
        int i = this.classToLastIdCache.getInt(class_);
        if (i != NO_ID_VALUE) {
            return i;
        } else {
            Class<?> class2 = class_;

            while((class2 = class2.getSuperclass()) != Object.class) {
                int j = this.classToLastIdCache.getInt(class2);
                if (j != NO_ID_VALUE) {
                    return j;
                }
            }

            return NO_ID_VALUE;
        }
    }

    public int getCount(Class<?> class_) {
        return this.getLastIdFor(class_) + 1;
    }

    public int define(Class<?> class_) {
        int i = this.getLastIdFor(class_);
        int j = i == NO_ID_VALUE ? 0 : i + 1;
        this.classToLastIdCache.put(class_, j);
        return j;
    }
}
