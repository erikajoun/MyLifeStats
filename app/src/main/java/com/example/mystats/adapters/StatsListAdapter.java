package com.example.mystats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mystats.R;
import com.example.mystats.database.Stat;

import java.util.List;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.TaskViewHolder> {
    public interface ListItemClickListener {
        void onListItemClick(View view, int index);
    }

    private final LayoutInflater mInflater;
    private List<Stat> mStats;
    private int mMaxStatsValue;
    private final ListItemClickListener mOnClickListener;
    private String mTheme;

    public StatsListAdapter(Context context, ListItemClickListener mOnClickListener, String theme) {
        mInflater = LayoutInflater.from(context);
        this.mOnClickListener = mOnClickListener;
        this.mTheme = theme;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.stat_list_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        if (mTheme.contentEquals("night")) {
            holder.mEditButton.setBackgroundResource(R.drawable.positive_button_night);
            holder.mDeleteButton.setBackgroundResource(R.drawable.negative_button_night);
        } else if (mTheme.contentEquals("morning")) {
            holder.mEditButton.setBackgroundResource(R.drawable.positive_button_morning);
            holder.mDeleteButton.setBackgroundResource(R.drawable.negative_button_morning);
        } else if (mTheme.contentEquals("evening")) {
            holder.mEditButton.setBackgroundResource(R.drawable.positive_button_evening);
            holder.mDeleteButton.setBackgroundResource(R.drawable.negative_button_evening);
        }

        if (mStats != null) {
            Stat current = mStats.get(position);
            int scaled_value = (int) Math.round(mMaxStatsValue * Double.parseDouble(current.getScaling()));
            holder.mTitleView.setText(current.getStat() + ": " + current.getValueAsString() + "/" + scaled_value);
        } else {
            holder.mTitleView.setText("Empty Task");
        }
    }

    public void setStats(List<Stat> stats) {
        mStats = stats;
        notifyDataSetChanged();
    }

    public void setmMaxStatsValue(int mMaxStatsValue) {
        this.mMaxStatsValue = mMaxStatsValue;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mStats != null)
            return mStats.size();
        else return 0;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitleView;
        private final Button mEditButton;
        private final Button mDeleteButton;
        private final LinearLayout mButtonsViewGroup;

        private TaskViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.title);
            mEditButton = itemView.findViewById(R.id.edit_stat_button);
            mDeleteButton = itemView.findViewById(R.id.delete_stat_button);

            mEditButton.setOnClickListener(this);
            mDeleteButton.setOnClickListener(this);
            itemView.setOnClickListener(this);

            mButtonsViewGroup = itemView.findViewById(R.id.linearlayout_buttons);
            mButtonsViewGroup.setVisibility(View.GONE);

        }

        @Override
        public void onClick(View view) {
            if (view instanceof Button) {
                mOnClickListener.onListItemClick(view, this.getAdapterPosition());
            } else {
                if (mButtonsViewGroup.getVisibility() == View.GONE) {
                    mButtonsViewGroup.setVisibility(View.VISIBLE);
                } else {
                    mButtonsViewGroup.setVisibility(View.GONE);
                }
            }
        }
    }
}