package com.neurofix.app.presentation.focusmode;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.neurofix.app.databinding.ItemFocusModeBinding;
import com.neurofix.app.domain.model.FocusMode;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Display only, same rule as VaultedAppAdapter: icon-less (a mode isn't an
 * app), name + active switch + delete button visible only for custom
 * (non-preset) modes. Row tap (outside the switch/delete button) opens the
 * mode's detail screen via onOpenDetail.
 */
public class FocusModeAdapter extends ListAdapter<FocusMode, FocusModeAdapter.ViewHolder> {

    private final BiConsumer<FocusMode, Boolean> onToggleActive;
    private final Consumer<FocusMode> onDelete;
    private final Consumer<FocusMode> onOpenDetail;

    public FocusModeAdapter(BiConsumer<FocusMode, Boolean> onToggleActive,
                             Consumer<FocusMode> onDelete,
                             Consumer<FocusMode> onOpenDetail) {
        super(DIFF_CALLBACK);
        this.onToggleActive = onToggleActive;
        this.onDelete = onDelete;
        this.onOpenDetail = onOpenDetail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFocusModeBinding binding = ItemFocusModeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFocusModeBinding binding;

        ViewHolder(ItemFocusModeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FocusMode mode) {
            binding.textModeName.setText(mode.getName());

            binding.switchActive.setOnCheckedChangeListener(null);
            binding.switchActive.setChecked(mode.isActive());
            binding.switchActive.setOnCheckedChangeListener((buttonView, checked) ->
                    onToggleActive.accept(mode, checked));

            binding.buttonDeleteMode.setVisibility(mode.isPreset() ? android.view.View.GONE : android.view.View.VISIBLE);
            binding.buttonDeleteMode.setOnClickListener(v -> onDelete.accept(mode));

            binding.getRoot().setOnClickListener(v -> onOpenDetail.accept(mode));
        }
    }

    private static final DiffUtil.ItemCallback<FocusMode> DIFF_CALLBACK = new DiffUtil.ItemCallback<FocusMode>() {
        @Override
        public boolean areItemsTheSame(@NonNull FocusMode oldItem, @NonNull FocusMode newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FocusMode oldItem, @NonNull FocusMode newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.isActive() == newItem.isActive()
                    && oldItem.isPreset() == newItem.isPreset();
        }
    };
}
