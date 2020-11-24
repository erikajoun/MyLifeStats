package com.example.mystats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.R;
import com.example.mystats.database.Task;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class CompletedTaskListAdapter extends RecyclerView.Adapter<CompletedTaskListAdapter.CompletedTaskViewHolder> {
    private final LayoutInflater mInflater;
    private List<Task> mTasks;
    private Context mContext;

    public CompletedTaskListAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public CompletedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.completed_list_item, parent, false);
        return new CompletedTaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedTaskViewHolder holder, int position) {
        if (mTasks != null) {
            Task current = mTasks.get(position);

            holder.mDescriptionView.setVisibility(View.VISIBLE);
            holder.mExplanationView.setVisibility(View.VISIBLE);
            holder.mDateRemovedView.setVisibility(View.VISIBLE);
            holder.mDeadlineView.setVisibility(View.VISIBLE);

            if (current.getDescription().isEmpty()) {
                holder.mDescriptionView.setVisibility(View.GONE);
            } else {
                holder.mDescriptionView.setText(current.getDescription());
            }

            if (current.getExplanation().isEmpty()) {
                holder.mExplanationView.setVisibility(View.GONE);
            } else {
                holder.mExplanationView.setText(current.getExplanation());
            }

            String titleText = "";
            String removedDateString = current.getDatetimeRemoved().substring(0, current.getDatetimeRemoved().indexOf("T"));
            if (current.getStatus().contentEquals("Complete")) {
                titleText += "Complete: ";
                holder.mResultView.setText("Result: +" + current.getValue() + " " + current.getStat());
                holder.mDateRemovedView.setText("Completed on: " + removedDateString);
            } else if (current.getStatus().contentEquals("Cancelled")) {
                titleText += "Cancelled: ";
                if (current.getOptional()) {
                    holder.mResultView.setText("Result: +0 " + current.getStat());
                } else {
                    holder.mResultView.setText("Result: -0 " + current.getStat());
                }
                holder.mDateRemovedView.setText("Cancelled on: " + removedDateString);
            } else if (current.getStatus().contentEquals("Missed")) {
                titleText += "Missed: ";
                if (current.getOptional()) {
                    holder.mResultView.setText("Result: -0 " + current.getStat());
                } else {
                    holder.mResultView.setText("Result: -" + current.getPenalty() + " " + current.getStat());
                }
                holder.mDateRemovedView.setVisibility(View.GONE);
            }
            titleText += current.getTitle();
            holder.mTitleView.setText(titleText);

            if (current.getDeadline().isEmpty()) {
                holder.mDeadlineView.setVisibility(View.GONE);
            } else {
                String deadlineText = "Deadline: " + current.getDeadline();
                if (current.getRepeatInterval() >= 1) {
                    if (current.getRepeatInterval() == 1) {
                        deadlineText += "\n\nDaily";
                    } else if (current.getRepeatInterval() == 7) {
                        deadlineText += "\n\nWeekly";
                    } else {
                        deadlineText += "\n\nEvery " + current.getRepeatInterval() + " days";
                    }
                }
                holder.mDeadlineView.setText(deadlineText);
            }

            if (!current.getStatus().contentEquals("Missed") && !(current.getOptional() && current.getStatus().contentEquals("Cancelled"))) {
                holder.mPhotoView.setVisibility(View.VISIBLE);
                File dir = new File(mContext.getFilesDir(), R.string.app_name + File.separator + R.string.images_dir);
                File path = new File(dir, current.getId() + R.string.image_file_extension);
                Picasso.get().load(path).rotate(current.getPhotoRotation()).into(holder.mPhotoView);
            } else {
                holder.mPhotoView.setVisibility(View.GONE);
            }
        } else {
            holder.mTitleView.setText("Empty Task");
        }
    }

    public void setTasks(List<Task> tasks) {
        mTasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mTasks != null)
            return mTasks.size();
        else return 0;
    }

    class CompletedTaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitleView;
        private final TextView mDescriptionView;
        private final TextView mResultView;
        private final TextView mDateRemovedView;
        private final TextView mDeadlineView;
        private final ImageView mPhotoView;
        private final TextView mExplanationView;

        private CompletedTaskViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.title);
            mDescriptionView = itemView.findViewById(R.id.description);
            mResultView = itemView.findViewById(R.id.result);
            mDateRemovedView = itemView.findViewById(R.id.date_removed);
            mDeadlineView = itemView.findViewById(R.id.deadline);
            mPhotoView = itemView.findViewById(R.id.photo);
            mExplanationView = itemView.findViewById(R.id.explanation);
        }
    }
}
