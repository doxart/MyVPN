package com.doxart.myvpn.DB;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class UsageRepository {
    private final UsageDao usageDao;
    private final LiveData<List<Usage>>allUsages;

    public UsageRepository(Application application) {
        UsageDatabase database = UsageDatabase.getInstance(application);
        usageDao = database.usageDao();
        allUsages = usageDao.getAllUsage();
    }

    public LiveData<List<Usage>> getAllUsages() {
        return allUsages;
    }

    public Usage getUsage(String date) {
        return usageDao.getUsage(date);
    }

    public void insertUsage(Usage usage) {
        UsageDatabase.databaseWriteExecutor.execute(() -> usageDao.insertUsage(usage));
    }
}
