package com.neurofix.app.presentation.vault;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.neurofix.app.R;
import com.neurofix.app.databinding.ItemVaultedAppBinding;
import com.neurofix.app.domain.model.VaultedApp;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Display only: icon, name, active/inactive status, switch, remove button.
 * No Room access, no repository calls, no business logic, no logging —
 * every interaction is delegated to the caller-supplied callbacks, which
 * ManageVaultFragment wires to the ViewModel.
 */
public class VaultedAppAdapter extends ListAdapter<VaultedApp, VaultedAppAdapter.ViewHolder> {

    private final PackageManager packageManager;
    private final BiConsumer<VaultedApp, Boolean> onToggleActive;
    private final Consumer<VaultedApp> onRemove;

    public VaultedAppAdapter(PackageManager packageManager,
                              BiConsumer<VaultedApp, Boolean> onToggleActive,
                              Consumer<VaultedApp> onRemove) {
        super(DIFF_CALLBACK);
        this.packageManager = packageManager;
        this.onToggleActive = onToggleActive;
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVaultedAppBinding binding = ItemVaultedAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVaultedAppBinding binding;

        ViewHolder(ItemVaultedAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VaultedApp app) {
            binding.textAppName.setText(app.getDisplayName());
            binding.textAppStatus.setText(app.isActive()
                    ? binding.getRoot().getContext().getString(R.string.vaulted_app_status_active)
                    : binding.getRoot().getContext().getString(R.string.vaulted_app_status_inactive));

            binding.switchActive.setOnCheckedChangeListener(null);
            binding.switchActive.setChecked(app.isActive());
            binding.switchActive.setOnCheckedChangeListener((buttonView, checked) ->
                    onToggleActive.accept(app, checked));

            binding.buttonRemove.setOnClickListener(v -> onRemove.accept(app));

            try {
                Drawable icon = packageManager.getApplicationIcon(app.getPackageName());
                binding.imageAppIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                binding.imageAppIcon.setImageDrawable(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<VaultedApp> DIFF_CALLBACK = new DiffUtil.ItemCallback<VaultedApp>() {
        @Override
        public boolean areItemsTheSame(@NonNull VaultedApp oldItem, @NonNull VaultedApp newItem) {
            return oldItem.getPackageName().equals(newItem.getPackageName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull VaultedApp oldItem, @NonNull VaultedApp newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName())
                    && oldItem.isActive() == newItem.isActive();
        }
    };
}
