package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class NotifCoordinators implements Dumpable {
    private final List<Coordinator> mCoordinators;
    private final List<NotifSection> mOrderedSections;

    public NotifCoordinators(DumpManager dumpManager, FeatureFlags featureFlags, HideNotifsForOtherUsersCoordinator hideNotifsForOtherUsersCoordinator, KeyguardCoordinator keyguardCoordinator, RankingCoordinator rankingCoordinator, AppOpsCoordinator appOpsCoordinator, DeviceProvisionedCoordinator deviceProvisionedCoordinator, BubbleCoordinator bubbleCoordinator, HeadsUpCoordinator headsUpCoordinator, ConversationCoordinator conversationCoordinator, PreparationCoordinator preparationCoordinator, MediaCoordinator mediaCoordinator) {
        ArrayList<Coordinator> arrayList = new ArrayList();
        this.mCoordinators = arrayList;
        this.mOrderedSections = new ArrayList();
        dumpManager.registerDumpable("NotifCoordinators", this);
        arrayList.add(new HideLocallyDismissedNotifsCoordinator());
        arrayList.add(hideNotifsForOtherUsersCoordinator);
        arrayList.add(keyguardCoordinator);
        arrayList.add(rankingCoordinator);
        arrayList.add(appOpsCoordinator);
        arrayList.add(deviceProvisionedCoordinator);
        arrayList.add(bubbleCoordinator);
        if (featureFlags.isNewNotifPipelineRenderingEnabled()) {
            arrayList.add(conversationCoordinator);
            arrayList.add(headsUpCoordinator);
            arrayList.add(preparationCoordinator);
        }
        arrayList.add(mediaCoordinator);
        for (Coordinator coordinator : arrayList) {
            if (coordinator.getSection() != null) {
                this.mOrderedSections.add(coordinator.getSection());
            }
        }
    }

    public void attach(NotifPipeline notifPipeline) {
        Iterator<Coordinator> it = this.mCoordinators.iterator();
        while (it.hasNext()) {
            it.next().attach(notifPipeline);
        }
        notifPipeline.setSections(this.mOrderedSections);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println();
        printWriter.println("NotifCoordinators:");
        Iterator<Coordinator> it = this.mCoordinators.iterator();
        while (it.hasNext()) {
            printWriter.println("\t" + it.next().getClass());
        }
        Iterator<NotifSection> it2 = this.mOrderedSections.iterator();
        while (it2.hasNext()) {
            printWriter.println("\t" + it2.next().getName());
        }
    }
}
