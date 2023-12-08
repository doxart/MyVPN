package com.doxart.myvpn.Util;

import android.app.Activity;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.doxart.myvpn.DB.UsageDatabase;
import com.doxart.myvpn.DB.UsageViewModel;

public class ViewModelHolder {
    private static final ViewModelHolder instance = new ViewModelHolder();

    public static ViewModelHolder getInstance() {
        return instance;
    }

    private UsageDatabase usageDatabase;
    private UsageViewModel usageViewModel;

    public void createViewModels(ViewModelStoreOwner viewModelStoreOwner, Activity activity) {
        usageDatabase = UsageDatabase.getInstance(activity);
        usageViewModel = new ViewModelProvider(viewModelStoreOwner).get(UsageViewModel.class);

        setUsageDatabase(usageDatabase);
        setUsageViewModel(usageViewModel);
    }

    public UsageDatabase getUsageDatabase() {
        return usageDatabase;
    }

    public void setUsageDatabase(UsageDatabase usageDatabase) {
        this.usageDatabase = usageDatabase;
    }

    public UsageViewModel getUsageViewModel() {
        return usageViewModel;
    }

    public void setUsageViewModel(UsageViewModel usageViewModel) {
        this.usageViewModel = usageViewModel;
    }

    public void onDestroy() {
        if (getUsageDatabase() != null) getUsageDatabase().close();
    }
}
