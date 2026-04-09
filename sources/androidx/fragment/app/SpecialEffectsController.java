package androidx.fragment.app;

import android.view.ViewGroup;
import androidx.core.os.CancellationSignal;
import androidx.fragment.R$id;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
abstract class SpecialEffectsController {
    private final ViewGroup mContainer;
    final ArrayList<Operation> mPendingOperations = new ArrayList<>();
    final HashMap<Fragment, Operation> mAwaitingCompletionOperations = new HashMap<>();
    boolean mOperationDirectionIsPop = false;
    boolean mIsContainerPostponed = false;

    abstract void executeOperations(List<Operation> list, boolean z);

    static SpecialEffectsController getOrCreateController(ViewGroup viewGroup, FragmentManager fragmentManager) {
        return getOrCreateController(viewGroup, fragmentManager.getSpecialEffectsControllerFactory());
    }

    static SpecialEffectsController getOrCreateController(ViewGroup viewGroup, SpecialEffectsControllerFactory specialEffectsControllerFactory) {
        int i = R$id.special_effects_controller_view_tag;
        Object tag = viewGroup.getTag(i);
        if (tag instanceof SpecialEffectsController) {
            return (SpecialEffectsController) tag;
        }
        SpecialEffectsController specialEffectsControllerCreateController = specialEffectsControllerFactory.createController(viewGroup);
        viewGroup.setTag(i, specialEffectsControllerCreateController);
        return specialEffectsControllerCreateController;
    }

    SpecialEffectsController(ViewGroup viewGroup) {
        this.mContainer = viewGroup;
    }

    public ViewGroup getContainer() {
        return this.mContainer;
    }

    Operation.Type getAwaitingCompletionType(FragmentStateManager fragmentStateManager) {
        Operation operation = this.mAwaitingCompletionOperations.get(fragmentStateManager.getFragment());
        if (operation != null) {
            return operation.getType();
        }
        return null;
    }

    void enqueueAdd(FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
        enqueue(Operation.Type.ADD, fragmentStateManager, cancellationSignal);
    }

    void enqueueShow(FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
        enqueue(Operation.Type.SHOW, fragmentStateManager, cancellationSignal);
    }

    void enqueueHide(FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
        enqueue(Operation.Type.HIDE, fragmentStateManager, cancellationSignal);
    }

    void enqueueRemove(FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
        enqueue(Operation.Type.REMOVE, fragmentStateManager, cancellationSignal);
    }

    private void enqueue(Operation.Type type, FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
        if (cancellationSignal.isCanceled()) {
            return;
        }
        synchronized (this.mPendingOperations) {
            final CancellationSignal cancellationSignal2 = new CancellationSignal();
            final FragmentStateManagerOperation fragmentStateManagerOperation = new FragmentStateManagerOperation(type, fragmentStateManager, cancellationSignal2);
            this.mPendingOperations.add(fragmentStateManagerOperation);
            this.mAwaitingCompletionOperations.put(fragmentStateManagerOperation.getFragment(), fragmentStateManagerOperation);
            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.SpecialEffectsController.1
                @Override // androidx.core.os.CancellationSignal.OnCancelListener
                public void onCancel() {
                    synchronized (SpecialEffectsController.this.mPendingOperations) {
                        SpecialEffectsController.this.mPendingOperations.remove(fragmentStateManagerOperation);
                        SpecialEffectsController.this.mAwaitingCompletionOperations.remove(fragmentStateManagerOperation.getFragment());
                        cancellationSignal2.cancel();
                    }
                }
            });
            fragmentStateManagerOperation.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.SpecialEffectsController.2
                @Override // java.lang.Runnable
                public void run() {
                    SpecialEffectsController.this.mAwaitingCompletionOperations.remove(fragmentStateManagerOperation.getFragment());
                }
            });
        }
    }

    void updateOperationDirection(boolean z) {
        this.mOperationDirectionIsPop = z;
    }

    void markPostponedState() {
        synchronized (this.mPendingOperations) {
            this.mIsContainerPostponed = false;
            for (int size = this.mPendingOperations.size() - 1; size >= 0; size--) {
                Operation operation = this.mPendingOperations.get(size);
                if (operation.getType() != Operation.Type.ADD && operation.getType() != Operation.Type.SHOW) {
                }
                this.mIsContainerPostponed = operation.getFragment().isPostponed();
                break;
            }
        }
    }

    void forcePostponedExecutePendingOperations() {
        if (this.mIsContainerPostponed) {
            this.mIsContainerPostponed = false;
            executePendingOperations();
        }
    }

    void executePendingOperations() {
        if (this.mIsContainerPostponed) {
            return;
        }
        synchronized (this.mPendingOperations) {
            executeOperations(new ArrayList(this.mPendingOperations), this.mOperationDirectionIsPop);
            this.mPendingOperations.clear();
            this.mOperationDirectionIsPop = false;
        }
    }

    void cancelAllOperations() {
        synchronized (this.mPendingOperations) {
            Iterator<Operation> it = this.mAwaitingCompletionOperations.values().iterator();
            while (it.hasNext()) {
                it.next().getCancellationSignal().cancel();
            }
            this.mAwaitingCompletionOperations.clear();
            this.mPendingOperations.clear();
        }
    }

    static class Operation {
        private final CancellationSignal mCancellationSignal;
        private final List<Runnable> mCompletionListeners = new ArrayList();
        private final Fragment mFragment;
        private final Type mType;

        enum Type {
            ADD,
            REMOVE,
            SHOW,
            HIDE
        }

        Operation(Type type, Fragment fragment, CancellationSignal cancellationSignal) {
            this.mType = type;
            this.mFragment = fragment;
            this.mCancellationSignal = cancellationSignal;
        }

        public final Type getType() {
            return this.mType;
        }

        public final Fragment getFragment() {
            return this.mFragment;
        }

        public final CancellationSignal getCancellationSignal() {
            return this.mCancellationSignal;
        }

        final void addCompletionListener(Runnable runnable) {
            this.mCompletionListeners.add(runnable);
        }

        public void complete() {
            Iterator<Runnable> it = this.mCompletionListeners.iterator();
            while (it.hasNext()) {
                it.next().run();
            }
        }
    }

    private static class FragmentStateManagerOperation extends Operation {
        private final FragmentStateManager mFragmentStateManager;

        FragmentStateManagerOperation(Operation.Type type, FragmentStateManager fragmentStateManager, CancellationSignal cancellationSignal) {
            super(type, fragmentStateManager.getFragment(), cancellationSignal);
            this.mFragmentStateManager = fragmentStateManager;
        }

        @Override // androidx.fragment.app.SpecialEffectsController.Operation
        public void complete() {
            super.complete();
            this.mFragmentStateManager.moveToExpectedState();
        }
    }
}
