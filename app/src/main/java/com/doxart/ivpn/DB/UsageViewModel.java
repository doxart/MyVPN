package com.doxart.ivpn.DB;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UsageViewModel extends AndroidViewModel {
    private final UsageRepository usageRepository;
    private final LiveData<List<Usage>> allUsages;

    public UsageViewModel(Application application) {
        super(application);
        usageRepository = new UsageRepository(application);
        allUsages = usageRepository.getAllUsages();
    }

    public UsageRepository getUsageRepository() {
        return usageRepository;
    }

    public LiveData<List<Usage>> getAllUsages() {
        return allUsages;
    }

    public void insertUsage(Usage usage) {
        usageRepository.insertUsage(usage);
    }
}
