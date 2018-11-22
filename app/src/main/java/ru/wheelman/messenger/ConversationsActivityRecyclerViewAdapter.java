package ru.wheelman.messenger;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import ru.wheelman.messenger.databinding.ActivityConversationsRecyclerViewItemBinding;

public class ConversationsActivityRecyclerViewAdapter extends RecyclerView.Adapter<ConversationsActivityRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = ConversationsActivityRecyclerViewAdapter.class.getSimpleName();

    private ArrayList<String> dataSet;


    public ConversationsActivityRecyclerViewAdapter(ArrayList<String> dataSet) {
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ActivityConversationsRecyclerViewItemBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.activity_conversations_recycler_view_item, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.setChatId("+" + dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ActivityConversationsRecyclerViewItemBinding binding;

        public ViewHolder(ru.wheelman.messenger.databinding.ActivityConversationsRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
