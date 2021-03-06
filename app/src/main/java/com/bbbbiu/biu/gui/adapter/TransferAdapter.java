package com.bbbbiu.biu.gui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bbbbiu.biu.R;
import com.bbbbiu.biu.gui.adapter.util.HeaderViewHolder;
import com.bbbbiu.biu.gui.adapter.util.OnViewTouchListener;
import com.bbbbiu.biu.gui.adapter.util.VideoIconRequestHandler;
import com.bbbbiu.biu.gui.transfer.FileItem;
import com.bbbbiu.biu.util.StorageUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 上传、下载、接收文件等界面({@link com.bbbbiu.biu.gui.transfer.TransferBaseActivity})的Adapter。
 * <p/>
 * Created by YieldNull at 3/23/16
 */
public class TransferAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TransferAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_WORKING = 1;
    private static final int VIEW_TYPE_WAITING = 2;
    private static final int VIEW_TYPE_FINISHED = 3;
    private static final int VIEW_TYPE_FAILED = 4;

    private final Context context;

    /**
     * Adapter 数据集
     */
    private ArrayList<FileItem> mDataSet = new ArrayList<>();

    /**
     * 等待队列
     */
    private Queue<FileItem> mWaitingQueue = new LinkedList<>();

    /**
     * 已完成
     */
    private List<FileItem> mFinishedList = new ArrayList<>();

    /**
     * 已失败
     */
    private List<FileItem> mFailedList = new ArrayList<>();


    /**
     * 当前任务对象
     */
    private FileItem mWorkingItem;


    /**
     * 下载文件存放处
     */
    private File mDownloadDir;


    private static final String PICASSO_TAG = "tag-img";
    private Picasso mVideoPicasso;
    private Picasso mImgPicasso;


    public TransferAdapter(Context context) {
        this.context = context;
        mDownloadDir = StorageUtil.getDownloadDir(context);

        Picasso.Builder builder = new Picasso.Builder(context);
        builder.addRequestHandler(new VideoIconRequestHandler());
        mVideoPicasso = builder.build();
        mImgPicasso = Picasso.with(context);

    }


    /**
     * 刷新数据集，将各文件分类。
     * <p/>
     * 显示时是按 正在下载，等待，已完成的次序显示的，因此加入数据集中的顺序不能乱
     */
    private void refreshDataSet() {
        if (mWorkingItem != null) {
            mDataSet.add(null);
            mDataSet.add(mWorkingItem);
        }

        if (mWaitingQueue.size() != 0) {
            mDataSet.add(null);
            mDataSet.addAll(mWaitingQueue);
        }

        if (mFinishedList.size() != 0) {
            mDataSet.add(null);
            mDataSet.addAll(mFinishedList);
        }

        if (mFailedList.size() != 0) {
            mDataSet.add(null);
            mDataSet.addAll(mFailedList);
        }

    }


    /**
     * 将任务加入等待队列
     *
     * @param fileItems 亟待完成的FileItem
     */
    public void addItem(ArrayList<FileItem> fileItems) {
        if (fileItems == null || fileItems.size() == 0) {
            return;
        }

        mDataSet.clear();

        mWaitingQueue.addAll(fileItems);

        if (mWorkingItem == null) { // 初始化
            mWorkingItem = mWaitingQueue.poll();
        }

        refreshDataSet();

        notifyDataSetChanged();
    }


    /**
     * 任务完成
     */
    public void setTaskFinished() {

        mDataSet.clear();

        mFinishedList.add(mWorkingItem);

        mWorkingItem = mWaitingQueue.poll();

        refreshDataSet();

        notifyDataSetChanged();
    }

    /**
     * 任务失败
     */
    public void setTaskFailed() {
        mDataSet.clear();

        mFailedList.add(mWorkingItem);

        mWorkingItem = mWaitingQueue.poll();

        refreshDataSet();

        notifyDataSetChanged();
    }


    /**
     * 剩下的所有任务全部失败
     */
    public void setAllTaskFailed() {
        mDataSet.clear();

        mFailedList.add(mWorkingItem);
        mFailedList.addAll(mWaitingQueue);

        mWaitingQueue.clear();
        mWorkingItem = null;

        refreshDataSet();
        notifyDataSetChanged();
    }


    /**
     * 根据文件Uri找到对应的文件，并更新进度
     *
     * @param recyclerView RecyclerView
     * @param fileUri      文件Uri
     * @param progress     当前进度 0-100
     */
    public void updateProgress(RecyclerView recyclerView, String fileUri, int progress) {
        int position;
        FileItem fileItem;

        if (fileUri != null) {
            position = getPosition(fileUri);
            fileItem = getItem(fileUri);
        } else {
            fileItem = mWorkingItem;
            position = getPosition(fileItem);
        }

        WorkingViewHolder holder;
        try {
            holder = (WorkingViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        } catch (ClassCastException e) {
            return; // 文件太小 已经完成了才收到
        }

        if (holder != null) { // 文件太小，导致list还没完成更新，此处NullPointer
            holder.progressBar.setProgress(progress);

            String read = StorageUtil.getReadableSize((long) (fileItem.size * progress * 0.01));
            String all = StorageUtil.getReadableSize(fileItem.size);

            holder.infoText.setText(String.format("%s/%s", read, all));
        }
    }


    /**
     * 所有任务是否完成或失败，即没有剩余的任务等待执行
     *
     * @return 是否完成
     */
    public boolean finished() {
        return mWaitingQueue.size() == 0 && mWorkingItem == null;
    }


    /**
     * 获取当前任务对象
     *
     * @return 当前任务
     */
    public FileItem getCurrentItem() {
        return mWorkingItem;
    }

    /**
     * 根据文件Uri找到其对应的任务在Adapter中的位置
     * <p/>
     * {@link FileItem} 重写equals方法，使Uri相同则两对象相同
     *
     * @param fileUri 文件Uri
     * @return 位置
     * @see FileItem
     */
    public int getPosition(String fileUri) {
        return mDataSet.indexOf(new FileItem(fileUri, "", 0));
    }


    /**
     * 找到FileItem在 Adapter中的位置
     *
     * @param fileItem FileItem
     * @return 位置
     */
    public int getPosition(FileItem fileItem) {
        return mDataSet.indexOf(fileItem);
    }


    /**
     * 根据文件Uri获取在数据集中对应的FileItem
     *
     * @param fileUri 文件Uri
     * @return 对应的FileItem
     */
    public FileItem getItem(String fileUri) {
        if (fileUri != null) {
            return mDataSet.get(getPosition(fileUri));
        } else {
            return mWorkingItem;
        }
    }

    /**
     * 获取指定位置的FileItem
     *
     * @param position 位置
     * @return Item
     */
    public FileItem getItem(int position) {
        return mDataSet.get(position);
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {

        FileItem item = getItem(position);

        if (item == null) {
            return VIEW_TYPE_HEADER;
        } else if (item == mWorkingItem) {
            return VIEW_TYPE_WORKING;
        } else if (mWaitingQueue.contains(item)) {
            return VIEW_TYPE_WAITING;
        } else if (mFinishedList.contains(item)) {
            return VIEW_TYPE_FINISHED;
        } else {
            return VIEW_TYPE_FAILED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_HEADER) {
            return HeaderViewHolder.build(inflater, parent);
        } else if (viewType == VIEW_TYPE_WORKING) {
            View itemView = inflater.inflate(R.layout.list_transfer_working, parent, false);
            return new WorkingViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_WAITING) {
            View itemView = inflater.inflate(R.layout.list_transfer_waiting, parent, false);
            return new WaitingViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_FINISHED) {
            View itemView = inflater.inflate(R.layout.list_transfer_finished, parent, false);
            return new FinishedViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.list_transfer_failed, parent, false);
            return new FailedViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder hd, int position) {
        int type = getItemViewType(position);
        FileItem fileItem = getItem(position);

        if (type == VIEW_TYPE_HEADER) {
            HeaderViewHolder holder = (HeaderViewHolder) hd;

            fileItem = getItem(position + 1);
            if (mWorkingItem == fileItem) {
                holder.headerText.setText(R.string.header_transfer_working);
            } else if (mWaitingQueue.contains(fileItem)) {
                holder.headerText.setText(R.string.header_transfer_waiting);
            } else if (mFinishedList.contains(fileItem)) {
                holder.headerText.setText(R.string.header_transfer_finished);
            } else {
                holder.headerText.setText(R.string.header_transfer_failed);
            }

        } else {
            Drawable iconDrawable = StorageUtil.getFileIcon(context, StorageUtil.getFileType(
                    StorageUtil.getFileExtension(fileItem.name)));

            String name = fileItem.name;
            String readableSize = StorageUtil.getReadableSize(fileItem.size);

            if (type == VIEW_TYPE_WORKING) {
                WorkingViewHolder holder = (WorkingViewHolder) hd;
                holder.iconImage.setImageDrawable(iconDrawable);
                holder.nameText.setText(name);
                holder.infoText.setText(readableSize);

                holder.progressBar.setMax(100);
                holder.progressBar.setProgress(0);

            } else if (type == VIEW_TYPE_WAITING) {
                WaitingViewHolder holder = (WaitingViewHolder) hd;
                holder.iconImage.setImageDrawable(iconDrawable);
                holder.nameText.setText(name);
                holder.sizeText.setText(readableSize);

            } else if (type == VIEW_TYPE_FAILED) {
                FailedViewHolder holder = (FailedViewHolder) hd;
                holder.iconImage.setImageDrawable(iconDrawable);
                holder.nameText.setText(name);
                holder.infoText.setText(readableSize);

            } else {
                FinishedViewHolder holder = (FinishedViewHolder) hd;

                final Uri uri;
                String filePath = null;

                File file = new File(mDownloadDir, fileItem.name);
                if (file.exists()) {
                    uri = Uri.fromFile(file); // 表示下载
                    filePath = file.getAbsolutePath();
                } else {
                    uri = Uri.parse(fileItem.uri); // 表示上传,需要注意 "content://"情况
                    if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                        filePath = uri.getPath();
                    }
                }

                holder.nameText.setText(name);
                holder.infoText.setText(readableSize);

                int fileType = StorageUtil.getFileType(StorageUtil.getFileExtension(fileItem.name));

                if (fileType == StorageUtil.TYPE_VIDEO) {

                    if (filePath == null) {
                        holder.iconImage.setImageDrawable(StorageUtil.getFileIcon(context, StorageUtil.TYPE_VIDEO));
                    } else {
                        mVideoPicasso.load(VideoIconRequestHandler.PICASSO_SCHEME_VIDEO + ":" + filePath)
                                .resize(VideoIconRequestHandler.THUMB_SIZE, VideoIconRequestHandler.THUMB_SIZE)
                                .placeholder(R.drawable.ic_type_video)
                                .tag(PICASSO_TAG)
                                .into(holder.iconImage);
                    }

                } else if (fileType == StorageUtil.TYPE_IMG) {
                    mImgPicasso.load(uri)
                            .resize(VideoIconRequestHandler.THUMB_SIZE, VideoIconRequestHandler.THUMB_SIZE)
                            .placeholder(R.drawable.ic_type_img)
                            .tag(PICASSO_TAG)
                            .into(holder.iconImage);

                } else if (fileType == StorageUtil.TYPE_APK) {
                    Drawable drawable = StorageUtil.getApkIcon(context, filePath);
                    if (drawable == null) {
                        drawable = StorageUtil.getFileIcon(context, StorageUtil.TYPE_APK);
                    }
                    holder.iconImage.setImageDrawable(drawable);

                } else {
                    holder.iconImage.setImageDrawable(StorageUtil.getFileIcon(context, fileType));
                }

                final FileItem finalFileItem = fileItem;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StorageUtil.openFile(context, uri, finalFileItem.name);
                    }
                });

                holder.itemView.setOnTouchListener(new OnViewTouchListener(context));
            }
        }
    }


    class WorkingViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageView_icon)
        ImageView iconImage;

        @Bind(R.id.textView_name)
        TextView nameText;

        @Bind(R.id.textView_info)
        TextView infoText;

        @Bind(R.id.progressBar)
        ProgressBar progressBar;

        public WorkingViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    class WaitingViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageView_icon)
        ImageView iconImage;

        @Bind(R.id.textView_name)
        TextView nameText;

        @Bind(R.id.textView_size)
        TextView sizeText;

        public WaitingViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }
    }

    class FinishedViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.imageView_icon)
        ImageView iconImage;

        @Bind(R.id.textView_name)
        TextView nameText;

        @Bind(R.id.textView_info)
        TextView infoText;

        @Bind(R.id.imageView_option)
        ImageView optionImage;

        public FinishedViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }


    class FailedViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageView_icon)
        ImageView iconImage;

        @Bind(R.id.textView_name)
        TextView nameText;

        @Bind(R.id.textView_info)
        TextView infoText;

        public FailedViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
