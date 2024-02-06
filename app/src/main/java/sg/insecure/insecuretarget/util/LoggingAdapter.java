package sg.insecure.insecuretarget.util;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoggingAdapter extends RecyclerView.Adapter<LoggingAdapter.LogViewHolder> {

    private final List<String> logData;
    private final Context context;

    public LoggingAdapter(Context context, List<String> logData) {
        this.context = context;
        this.logData = logData;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.logText.setText(logData.get(position));
    }

    @Override
    public int getItemCount() {
        return logData.size();
    }

    public void addLogEntry(String logEntry) {
        logData.add(logEntry);
        notifyItemInserted(logData.size() -  1);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView logText;

        LogViewHolder(View itemView) {
            super(itemView);
            logText = itemView.findViewById(android.R.id.text1);
        }
    }
}