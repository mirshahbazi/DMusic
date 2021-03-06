package com.d.music.online.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.lib.xrv.adapter.CommonHolder;
import com.d.lib.xrv.adapter.MultiItemTypeSupport;
import com.d.music.R;
import com.d.music.module.glide.GlideCircleTransform;
import com.d.music.online.model.MVCommentModel;
import com.d.music.online.model.MVDetailModel;
import com.d.music.online.model.MVInfoModel;
import com.d.music.online.model.MVSimilarModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MVDetailAdapter
 * Created by D on 2018/8/11.
 */
public class MVDetailAdapter extends CommonAdapter<MVDetailModel> {

    private GlideCircleTransform circleTransform;
    private SimpleDateFormat dateFormat;

    public MVDetailAdapter(Context context, List<MVDetailModel> datas, MultiItemTypeSupport<MVDetailModel> multiItemTypeSupport) {
        super(context, datas, multiItemTypeSupport);
        circleTransform = new GlideCircleTransform(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    }

    @Override
    public void convert(final int position, final CommonHolder holder, final MVDetailModel item) {
        switch (holder.mLayoutId) {
            case R.layout.module_online_adapter_mv_detail_info:
                if (item instanceof MVInfoModel) {
                    convertInfo(position, holder, (MVInfoModel) item);
                }
                break;
            case R.layout.module_online_adapter_mv_detail_similar:
                if (item instanceof MVSimilarModel) {
                    convertSimilar(position, holder, (MVSimilarModel) item);
                }
                break;
            case R.layout.module_online_adapter_mv_detail_comment:
                if (item instanceof MVCommentModel) {
                    convertComment(position, holder, (MVCommentModel) item);
                }
                break;
        }
    }

    private void convertInfo(final int position, final CommonHolder holder, final MVInfoModel item) {
        holder.setText(R.id.tv_mv_info_title, item.name);
        holder.setText(R.id.tv_mv_info_singer, item.artistName);
        holder.setText(R.id.tv_mv_info_detail, item.publishTime + "    " + item.playCount + "次播放");
        holder.setText(R.id.tv_mv_info_content, item.descX);

        holder.setText(R.id.tv_mv_info_like_count, "" + item.likeCount);
        holder.setText(R.id.tv_mv_info_sub_count, "" + item.subCount);
        holder.setText(R.id.tv_mv_info_comment_count, "" + item.commentCount);
        holder.setText(R.id.tv_mv_info_share_count, "" + item.shareCount);
    }

    private void convertSimilar(final int position, final CommonHolder holder, final MVSimilarModel item) {
        holder.setText(R.id.tv_mv_similar_play_count, ">: " + item.playCount);
        holder.setText(R.id.tv_mv_similar_title, item.name);
        holder.setText(R.id.tv_mv_similar_singer, item.artistName);
        Glide.with(mContext)
                .load(item.cover)
                .apply(new RequestOptions().dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_mv_similar_cover));
    }

    private void convertComment(final int position, final CommonHolder holder, final MVCommentModel item) {
        holder.setText(R.id.tv_mv_comment_user, item.user.nickname);
        holder.setText(R.id.tv_mv_comment_time, dateFormat.format(new Date(item.time)));
        holder.setText(R.id.tv_mv_comment_content, item.content);
        Glide.with(mContext)
                .load(item.user.avatarUrl)
                .apply(new RequestOptions().transform(circleTransform).dontAnimate())
                .into((ImageView) holder.getView(R.id.iv_mv_comment_cover));
    }
}
