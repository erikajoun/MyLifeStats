package com.example.mystats.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.R;
import com.example.mystats.database.Task;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    public interface ListItemClickListener {
        void onListItemClick(View view, int index);
    }

    private final LayoutInflater mInflater;
    private List<Task> mTasks;
    private final ListItemClickListener mOnClickListener;
    private String mTheme;

    public TaskListAdapter(Context context, ListItemClickListener mOnClickListener, String theme) {
        mInflater = LayoutInflater.from(context);
        this.mOnClickListener = mOnClickListener;
        this.mTheme = theme;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.hideButtons();

        holder.mCompleteTaskButton.setEnabled(true);
        if (mTheme.contentEquals("night")) {
            holder.mCompleteTaskButton.setTextColor(Color.WHITE);
        }
        else {
            holder.mCompleteTaskButton.setTextColor(Color.BLACK);
        }

        if (mTheme.contentEquals("night")) {
            holder.mCompleteTaskButton.setBackgroundResource(R.drawable.positive_button_night);
            holder.mCancelTaskButton.setBackgroundResource(R.drawable.negative_button_night);
        }
        else if (mTheme.contentEquals("morning")) {
            holder.mCompleteTaskButton.setBackgroundResource(R.drawable.positive_button_morning);
            holder.mCancelTaskButton.setBackgroundResource(R.drawable.negative_button_morning);
        }
        else if (mTheme.contentEquals("evening")) {
            holder.mCompleteTaskButton.setBackgroundResource(R.drawable.positive_button_evening);
            holder.mCancelTaskButton.setBackgroundResource(R.drawable.negative_button_evening);
        }

        if (mTasks != null) {
            Task current = mTasks.get(position);
            String titleText = current.getTitle();
            if (current.getKeepPrivate()) {
                titleText += " (Private)";
            }
            holder.mTitleView.setText(titleText);

            if (current.getDescription().isEmpty()) {
                holder.mDescriptionView.setVisibility(View.GONE);
            }
            else {
                holder.mDescriptionView.setVisibility(View.VISIBLE);
                holder.mDescriptionView.setText(current.getDescription());
            }

            String resultText = "Reward: +" + current.getValue() + " " + current.getStat();
            if (current.getPenalty() > 0) {
                resultText += "\nPenalty: -" + current.getPenalty() + " " + current.getStat();
            }
            holder.mResultView.setText(resultText);

            if (current.getDeadline().isEmpty()) {
                holder.mDeadlineView.setVisibility(View.GONE);
                holder.mCompleteTaskButton.setEnabled(true);
            }
            else {
                holder.mDeadlineView.setVisibility(View.VISIBLE);
                String deadlineText = "Deadline: " + current.getDeadline();

                if (current.daysLeft() == 0) {
                    deadlineText += "\n\nDue Today";
                } else if (current.daysLeft() == 1) {
                    deadlineText += "\n\nDue Tomorrow";
                } else {
                    deadlineText += "\n\n" + (current.daysLeft() + 1) + " Days Left";
                }

                if (current.getRepeatInterval() >= 1) {
                    if (current.getRepeatInterval() == 1) {
                        deadlineText += "\n\nDaily";
                    } else if (current.getRepeatInterval() == 7) {
                        deadlineText += "\n\nWeekly";
                    } else {
                        deadlineText += "\n\nEvery " + current.getRepeatInterval() + " days";
                    }

                    if (current.getStatus().contentEquals("Waiting")) {
                        deadlineText += " (Not Available Yet)";
                        holder.mCompleteTaskButton.setEnabled(false);
                        holder.mCompleteTaskButton.setTextColor(Color.GRAY);
                    }
                }
                holder.mDeadlineView.setText(deadlineText);
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

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitleView;
        private final TextView mDescriptionView;
        private final TextView mResultView;
        private final TextView mDeadlineView;
        private final Button mCompleteTaskButton;
        private final Button mCancelTaskButton;
        private final LinearLayout mButtonsViewGroup;

        private TaskViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.title);
            mDescriptionView = itemView.findViewById(R.id.description);
            mResultView = itemView.findViewById(R.id.result);
            mDeadlineView = itemView.findViewById(R.id.deadline);
            mCompleteTaskButton = itemView.findViewById(R.id.complete_task_button);
            mCancelTaskButton = itemView.findViewById(R.id.cancel_task_button);

            mCompleteTaskButton.setOnClickListener(this);
            mCancelTaskButton.setOnClickListener(this);
            itemView.setOnClickListener(this);

            mButtonsViewGroup = itemView.findViewById(R.id.linearlayout_buttons);
            hideButtons();
        }

        public void hideButtons() {
            mButtonsViewGroup.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (view instanceof Button) {
                mOnClickListener.onListItemClick(view, this.getAdapterPosition());
            }
            else {
                if (mButtonsViewGroup.getVisibility() == View.GONE) {
                    mButtonsViewGroup.setVisibility(View.VISIBLE);
                }
                else {
                    hideButtons();
                }
            }
        }
    }
}