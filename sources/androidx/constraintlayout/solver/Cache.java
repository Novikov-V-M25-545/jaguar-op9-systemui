package androidx.constraintlayout.solver;

import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class Cache {
    Pools$Pool<ArrayRow> arrayRowPool;
    Pools$Pool<?> goalVariablePool;
    SolverVariable[] mIndexedVariables;
    Pools$Pool<SolverVariable> solverVariablePool;

    public Cache() {
        final int i = LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT;
        this.arrayRowPool = new Pools$Pool<T>(i) { // from class: androidx.constraintlayout.solver.Pools$SimplePool
            private final Object[] mPool;
            private int mPoolSize;

            {
                if (i <= 0) {
                    throw new IllegalArgumentException("The max pool size must be > 0");
                }
                this.mPool = new Object[i];
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public T acquire() {
                int i2 = this.mPoolSize;
                if (i2 <= 0) {
                    return null;
                }
                int i3 = i2 - 1;
                Object[] objArr = this.mPool;
                T t = (T) objArr[i3];
                objArr[i3] = null;
                this.mPoolSize = i2 - 1;
                return t;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public boolean release(T t) {
                int i2 = this.mPoolSize;
                Object[] objArr = this.mPool;
                if (i2 >= objArr.length) {
                    return false;
                }
                objArr[i2] = t;
                this.mPoolSize = i2 + 1;
                return true;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public void releaseAll(T[] tArr, int i2) {
                if (i2 > tArr.length) {
                    i2 = tArr.length;
                }
                for (int i3 = 0; i3 < i2; i3++) {
                    T t = tArr[i3];
                    int i4 = this.mPoolSize;
                    Object[] objArr = this.mPool;
                    if (i4 < objArr.length) {
                        objArr[i4] = t;
                        this.mPoolSize = i4 + 1;
                    }
                }
            }
        };
        this.solverVariablePool = new Pools$Pool<T>(i) { // from class: androidx.constraintlayout.solver.Pools$SimplePool
            private final Object[] mPool;
            private int mPoolSize;

            {
                if (i <= 0) {
                    throw new IllegalArgumentException("The max pool size must be > 0");
                }
                this.mPool = new Object[i];
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public T acquire() {
                int i2 = this.mPoolSize;
                if (i2 <= 0) {
                    return null;
                }
                int i3 = i2 - 1;
                Object[] objArr = this.mPool;
                T t = (T) objArr[i3];
                objArr[i3] = null;
                this.mPoolSize = i2 - 1;
                return t;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public boolean release(T t) {
                int i2 = this.mPoolSize;
                Object[] objArr = this.mPool;
                if (i2 >= objArr.length) {
                    return false;
                }
                objArr[i2] = t;
                this.mPoolSize = i2 + 1;
                return true;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public void releaseAll(T[] tArr, int i2) {
                if (i2 > tArr.length) {
                    i2 = tArr.length;
                }
                for (int i3 = 0; i3 < i2; i3++) {
                    T t = tArr[i3];
                    int i4 = this.mPoolSize;
                    Object[] objArr = this.mPool;
                    if (i4 < objArr.length) {
                        objArr[i4] = t;
                        this.mPoolSize = i4 + 1;
                    }
                }
            }
        };
        this.mIndexedVariables = new SolverVariable[32];
        final int i2 = 64;
        this.goalVariablePool = new Pools$Pool<T>(i2) { // from class: androidx.constraintlayout.solver.Pools$SimplePool
            private final Object[] mPool;
            private int mPoolSize;

            {
                if (i2 <= 0) {
                    throw new IllegalArgumentException("The max pool size must be > 0");
                }
                this.mPool = new Object[i2];
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public T acquire() {
                int i22 = this.mPoolSize;
                if (i22 <= 0) {
                    return null;
                }
                int i3 = i22 - 1;
                Object[] objArr = this.mPool;
                T t = (T) objArr[i3];
                objArr[i3] = null;
                this.mPoolSize = i22 - 1;
                return t;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public boolean release(T t) {
                int i22 = this.mPoolSize;
                Object[] objArr = this.mPool;
                if (i22 >= objArr.length) {
                    return false;
                }
                objArr[i22] = t;
                this.mPoolSize = i22 + 1;
                return true;
            }

            @Override // androidx.constraintlayout.solver.Pools$Pool
            public void releaseAll(T[] tArr, int i22) {
                if (i22 > tArr.length) {
                    i22 = tArr.length;
                }
                for (int i3 = 0; i3 < i22; i3++) {
                    T t = tArr[i3];
                    int i4 = this.mPoolSize;
                    Object[] objArr = this.mPool;
                    if (i4 < objArr.length) {
                        objArr[i4] = t;
                        this.mPoolSize = i4 + 1;
                    }
                }
            }
        };
    }
}
