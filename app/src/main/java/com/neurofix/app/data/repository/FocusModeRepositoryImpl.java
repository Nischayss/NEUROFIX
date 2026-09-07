package com.neurofix.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.neurofix.app.database.dao.FocusModeDao;
import com.neurofix.app.database.entity.FocusModeAppCrossRefEntity;
import com.neurofix.app.database.entity.FocusModeEntity;
import com.neurofix.app.domain.model.FocusMode;
import com.neurofix.app.domain.model.FocusModeApp;
import com.neurofix.app.domain.repository.FocusModeRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FocusModeRepositoryImpl implements FocusModeRepository {

    private final FocusModeDao focusModeDao;

    @Inject
    public FocusModeRepositoryImpl(FocusModeDao focusModeDao) {
        this.focusModeDao = focusModeDao;
    }

    @Override
    public LiveData<List<FocusMode>> observeAllFocusModes() {
        return Transformations.map(focusModeDao.observeAllFocusModes(), this::mapModeList);
    }

    @Override
    public LiveData<List<String>> observeActiveModePackageNames() {
        return focusModeDao.observeActiveModePackageNames();
    }

    @Override
    public LiveData<List<FocusModeApp>> observeAppsForMode(long modeId) {
        return Transformations.map(focusModeDao.observeAppsForMode(modeId), this::mapAppList);
    }

    @Override
    public int getFocusModeCount() {
        return focusModeDao.countFocusModes();
    }

    @Override
    public long createMode(String name, boolean isPreset) {
        return focusModeDao.insertFocusMode(new FocusModeEntity(0L, name, isPreset, false));
    }

    @Override
    public long createCustomMode(String name) {
        return createMode(name, false);
    }

    @Override
    public void deleteCustomMode(FocusMode mode) {
        if (mode.isPreset()) {
            throw new IllegalStateException("Preset Focus Modes cannot be deleted: " + mode.getName());
        }
        focusModeDao.deleteFocusMode(new FocusModeEntity(mode.getId(), mode.getName(), false, mode.isActive()));
    }

    @Override
    public void addAppToMode(long modeId, String packageName, String displayName) {
        focusModeDao.insertAppToMode(new FocusModeAppCrossRefEntity(modeId, packageName, displayName));
    }

    @Override
    public void removeAppFromMode(long modeId, String packageName) {
        focusModeDao.removeAppFromMode(modeId, packageName);
    }

    @Override
    public void setActiveMode(Long modeId) {
        focusModeDao.setActiveMode(modeId);
    }

    private List<FocusMode> mapModeList(List<FocusModeEntity> entities) {
        List<FocusMode> result = new ArrayList<>();
        for (FocusModeEntity entity : entities) {
            result.add(new FocusMode(entity.getId(), entity.getName(), entity.isPreset(), entity.isActive()));
        }
        return result;
    }

    private List<FocusModeApp> mapAppList(List<FocusModeAppCrossRefEntity> entities) {
        List<FocusModeApp> result = new ArrayList<>();
        for (FocusModeAppCrossRefEntity entity : entities) {
            result.add(new FocusModeApp(entity.getPackageName(), entity.getDisplayName()));
        }
        return result;
    }
}
