package com.neurofix.app.presentation.onboarding;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.neurofix.app.databinding.ItemInstalledAppBinding;
import com.neurofix.app.domain.model.InstalledApp;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Re-resolves each app's icon via PackageManager at bind time rather than
 * carrying a Drawable on the domain model — icons are a rendering concern,
 * not domain data. This is a deliberate, narrow exception: introducing a
 * Use Case just to fetch a decorative icon would be the over-abstraction
 * we were explicitly told to avoid.
 */
public class InstalledAppAdapter extends ListAdapter<InstalledApp, InstalledAppAdapter.ViewHolder> {

    private final PackageManager packageManager;
    private final Consumer<InstalledApp> onToggle;
    private Set<String> selectedPackageNames = java.util.Collections.emptySet();

    public InstalledAppAdapter(PackageManager packageManager, Consumer<InstalledApp> onToggle) {
        super(DIFF_CALLBACK);
        this.packageManager = packageManager;
        this.onToggle = onToggle;
    }

    public void setSelectedPackageNames(Set<String> selectedPackageNames) {
        this.selectedPackageNames = selectedPackageNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInstalledAppBinding binding = ItemInstalledAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstalledApp app = getItem(position);
        holder.bind(app, selectedPackageNames.contains(app.getPackageName()));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemInstalledAppBinding binding;

        ViewHolder(ItemInstalledAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(InstalledApp app, boolean isSelected) {
            binding.textAppName.setText(app.getDisplayName());
            binding.checkboxSelected.setOnCheckedChangeListener(null);
            binding.checkboxSelected.setChecked(isSelected);
            binding.checkboxSelected.setOnCheckedChangeListener((buttonView, checked) -> onToggle.accept(app));
            binding.getRoot().setOnClickListener(v -> onToggle.accept(app));

            try {
                Drawable icon = packageManager.getApplicationIcon(app.getPackageName());
                binding.imageAppIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                binding.imageAppIcon.setImageDrawable(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<InstalledApp> DIFF_CALLBACK = new DiffUtil.ItemCallback<InstalledApp>() {
        @Override
        public boolean areItemsTheSame(@NonNull InstalledApp oldItem, @NonNull InstalledApp newItem) {
            return oldItem.getPackageName().equals(newItem.getPackageName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull InstalledApp oldItem, @NonNull InstalledApp newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName());
        }
    };
}
