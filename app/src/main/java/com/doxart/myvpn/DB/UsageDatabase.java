package com.doxart.myvpn.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Usage.class}, version = 1)
public abstract class UsageDatabase extends RoomDatabase {
    public abstract UsageDao usageDao();
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile UsageDatabase INSTANCE;

    public static UsageDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (UsageDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UsageDatabase.class, "usage_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
