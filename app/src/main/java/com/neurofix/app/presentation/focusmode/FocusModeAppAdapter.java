package com.neurofix.app.presentation.focusmode;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.neurofix.app.databinding.ItemFocusModeAppBinding;
import com.neurofix.app.domain.model.FocusModeApp;

import java.util.function.Consumer;

/**
 * Display only, mirrors VaultedAppAdapter minus the active/inactive switch
 * — membership in a mode's list IS the state, there's no separate
 * active/inactive sub-state the way base-Vault apps have.
 */
public class FocusModeAppAdapter extends ListAdapter<FocusModeApp, FocusModeAppAdapter.ViewHolder> {

    private final PackageManager packageManager;
    private final Consumer<FocusModeApp> onRemove;

    public FocusModeAppAdapter(PackageManager packageManager, Consumer<FocusModeApp> onRemove) {
        super(DIFF_CALLBACK);
        this.packageManager = packageManager;
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFocusModeAppBinding binding = ItemFocusModeAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFocusModeAppBinding binding;

        ViewHolder(ItemFocusModeAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FocusModeApp app) {
            binding.textAppName.setText(app.getDisplayName());
            binding.buttonRemove.setOnClickListener(v -> onRemove.accept(app));

            try {
                Drawable icon = packageManager.getApplicationIcon(app.getPackageName());
                binding.imageAppIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                binding.imageAppIcon.setImageDrawable(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<FocusModeApp> DIFF_CALLBACK = new DiffUtil.ItemCallback<FocusModeApp>() {
        @Override
        public boolean areItemsTheSame(@NonNull FocusModeApp oldItem, @NonNull FocusModeApp newItem) {
            return oldItem.getPackageName().equals(newItem.getPackageName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FocusModeApp oldItem, @NonNull FocusModeApp newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName());
        }
    };
}
