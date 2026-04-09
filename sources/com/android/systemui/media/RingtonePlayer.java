package com.android.systemui.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.IAudioService;
import android.media.IRingtonePlayer;
import android.media.Ringtone;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.systemui.SystemUI;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/* loaded from: classes.dex */
public class RingtonePlayer extends SystemUI {
    private final NotificationPlayer mAsyncPlayer;
    private IAudioService mAudioService;
    private IRingtonePlayer mCallback;
    private final HashMap<IBinder, Client> mClients;

    public RingtonePlayer(Context context) {
        super(context);
        this.mAsyncPlayer = new NotificationPlayer("RingtonePlayer");
        this.mClients = new HashMap<>();
        this.mCallback = new IRingtonePlayer.Stub() { // from class: com.android.systemui.media.RingtonePlayer.1
            public void play(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z) throws RemoteException {
                playWithVolumeShaping(iBinder, uri, audioAttributes, f, z, null);
            }

            public void playWithVolumeShaping(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z, VolumeShaper.Configuration configuration) throws RemoteException {
                Client client;
                synchronized (RingtonePlayer.this.mClients) {
                    client = (Client) RingtonePlayer.this.mClients.get(iBinder);
                    if (client == null) {
                        client = RingtonePlayer.this.new Client(iBinder, uri, Binder.getCallingUserHandle(), audioAttributes, configuration);
                        iBinder.linkToDeath(client, 0);
                        RingtonePlayer.this.mClients.put(iBinder, client);
                    }
                }
                client.mRingtone.setLooping(z);
                client.mRingtone.setVolume(f);
                client.mRingtone.play();
            }

            public void stop(IBinder iBinder) {
                Client client;
                synchronized (RingtonePlayer.this.mClients) {
                    client = (Client) RingtonePlayer.this.mClients.remove(iBinder);
                }
                if (client != null) {
                    client.mToken.unlinkToDeath(client, 0);
                    client.mRingtone.stop();
                }
            }

            public boolean isPlaying(IBinder iBinder) {
                Client client;
                synchronized (RingtonePlayer.this.mClients) {
                    client = (Client) RingtonePlayer.this.mClients.get(iBinder);
                }
                if (client != null) {
                    return client.mRingtone.isPlaying();
                }
                return false;
            }

            public void setPlaybackProperties(IBinder iBinder, float f, boolean z) {
                Client client;
                synchronized (RingtonePlayer.this.mClients) {
                    client = (Client) RingtonePlayer.this.mClients.get(iBinder);
                }
                if (client != null) {
                    client.mRingtone.setVolume(f);
                    client.mRingtone.setLooping(z);
                }
            }

            public void playAsync(Uri uri, UserHandle userHandle, boolean z, AudioAttributes audioAttributes) {
                if (Binder.getCallingUid() != 1000) {
                    throw new SecurityException("Async playback only available from system UID.");
                }
                if (UserHandle.ALL.equals(userHandle)) {
                    userHandle = UserHandle.SYSTEM;
                }
                RingtonePlayer.this.mAsyncPlayer.play(RingtonePlayer.this.getContextForUser(userHandle), uri, z, audioAttributes);
            }

            public void stopAsync() {
                if (Binder.getCallingUid() == 1000) {
                    RingtonePlayer.this.mAsyncPlayer.stop();
                    return;
                }
                throw new SecurityException("Async playback only available from system UID.");
            }

            public String getTitle(Uri uri) {
                return Ringtone.getTitle(RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()), uri, false, false);
            }

            public ParcelFileDescriptor openRingtone(Uri uri) {
                ContentResolver contentResolver = RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()).getContentResolver();
                if (uri.toString().startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                    Cursor cursorQuery = contentResolver.query(uri, new String[]{"is_ringtone", "is_alarm", "is_notification"}, null, null, null);
                    try {
                        if (cursorQuery.moveToFirst() && (cursorQuery.getInt(0) != 0 || cursorQuery.getInt(1) != 0 || cursorQuery.getInt(2) != 0)) {
                            try {
                                ParcelFileDescriptor parcelFileDescriptorOpenFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
                                cursorQuery.close();
                                return parcelFileDescriptorOpenFileDescriptor;
                            } catch (IOException e) {
                                throw new SecurityException(e);
                            }
                        }
                        cursorQuery.close();
                    } catch (Throwable th) {
                        if (cursorQuery != null) {
                            try {
                                cursorQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                }
                throw new SecurityException("Uri is not ringtone, alarm, or notification: " + uri);
            }
        };
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mAsyncPlayer.setUsesWakeLock(this.mContext);
        IAudioService iAudioServiceAsInterface = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        this.mAudioService = iAudioServiceAsInterface;
        try {
            iAudioServiceAsInterface.setRingtonePlayer(this.mCallback);
        } catch (RemoteException e) {
            Log.e("RingtonePlayer", "Problem registering RingtonePlayer: " + e);
        }
    }

    private class Client implements IBinder.DeathRecipient {
        private final Ringtone mRingtone;
        private final IBinder mToken;

        Client(IBinder iBinder, Uri uri, UserHandle userHandle, AudioAttributes audioAttributes, VolumeShaper.Configuration configuration) throws IllegalArgumentException {
            this.mToken = iBinder;
            Ringtone ringtone = new Ringtone(RingtonePlayer.this.getContextForUser(userHandle), false);
            this.mRingtone = ringtone;
            ringtone.setAudioAttributes(audioAttributes);
            ringtone.setUri(uri, configuration);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (RingtonePlayer.this.mClients) {
                RingtonePlayer.this.mClients.remove(this.mToken);
            }
            this.mRingtone.stop();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Context getContextForUser(UserHandle userHandle) {
        try {
            Context context = this.mContext;
            return context.createPackageContextAsUser(context.getPackageName(), 0, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Clients:");
        synchronized (this.mClients) {
            for (Client client : this.mClients.values()) {
                printWriter.print("  mToken=");
                printWriter.print(client.mToken);
                printWriter.print(" mUri=");
                printWriter.println(client.mRingtone.getUri());
            }
        }
    }
}
