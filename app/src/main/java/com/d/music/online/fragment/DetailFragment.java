package com.d.music.online.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.common.module.loader.AbsFragment;
import com.d.lib.common.module.mvp.MvpView;
import com.d.lib.common.module.repeatclick.ClickUtil;
import com.d.lib.common.view.TitleLayout;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.music.R;
import com.d.music.module.greendao.bean.MusicModel;
import com.d.music.online.activity.DetailActivity;
import com.d.music.online.adapter.DetailAdapter;
import com.d.music.online.model.BillSongsRespModel;
import com.d.music.online.model.RadioSongsRespModel;
import com.d.music.online.presenter.MusicPresenter;
import com.d.music.online.view.IMusicView;
import com.d.music.view.SongHeaderView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * DetailFragment
 * Created by D on 2018/8/12.
 */
public class DetailFragment extends AbsFragment<MusicModel, MusicPresenter> implements IMusicView {
    @BindView(R.id.tl_title)
    TitleLayout tlTitle;
    @BindView(R.id.iv_cover)
    ImageView ivCover;

    private int type;
    private String channel, title, cover;
    private SongHeaderView header;

    @OnClick({R.id.iv_title_left})
    public void onClickListener(View v) {
        if (ClickUtil.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_title_left:
                getActivity().finish();
                break;
        }
    }

    @Override
    public MusicPresenter getPresenter() {
        return new MusicPresenter(getActivity().getApplicationContext());
    }

    @Override
    protected MvpView getMvpView() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.module_online_fragment_detail;
    }

    @Override
    protected CommonAdapter<MusicModel> getAdapter() {
        return new DetailAdapter(mContext, new ArrayList<MusicModel>(), R.layout.module_online_adapter_music);
    }

    @Override
    protected void init() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt(DetailActivity.ARG_TYPE, DetailActivity.TYPE_BILL);
            channel = bundle.getString(DetailActivity.ARG_CHANNEL);
            title = bundle.getString(DetailActivity.ARG_TITLE);
            cover = bundle.getString(DetailActivity.ARG_COVER);
        }
        tlTitle.setText(R.id.tv_title_title, !TextUtils.isEmpty(title) ? title : "音乐");
        super.init();
    }

    @Override
    protected void initList() {
        header = new SongHeaderView(mContext);
        header.setVisibility(View.GONE);
        xrvList.setCanRefresh(false);
        if (type == DetailActivity.TYPE_RADIO) {
            xrvList.setCanLoadMore(false);
        }
        xrvList.addHeaderView(header);
        super.initList();
    }

    @Override
    protected void onLoad(int page) {
        if (type == DetailActivity.TYPE_BILL) {
            mPresenter.getBillSongs(channel, page);
        } else if (type == DetailActivity.TYPE_RADIO) {
            mPresenter.getRadioSongs(channel, page);
        }
    }

    @Override
    public void setInfo(BillSongsRespModel info) {
        if (!TextUtils.isEmpty(cover)) {
            setCover(cover);
            return;
        }
        if (info == null || info.billboard == null || info.billboard.pic_s260 == null) {
            return;
        }
        setCover(info.billboard.pic_s260);
    }

    @Override
    public void setInfo(RadioSongsRespModel info) {
        if (!TextUtils.isEmpty(cover)) {
            setCover(cover);
            return;
        }
        if (info == null || info.result == null || info.result.songlist == null
                || info.result.songlist.size() <= 0
                || info.result.songlist.get(0) == null) {
            return;
        }
        setCover(info.result.songlist.get(0).thumb);
    }

    @Override
    public void setData(List<MusicModel> datas) {
        super.setData(datas);
        notifyDataCountChanged(commonLoader.getDatas().size());
    }

    private void setCover(String url) {
        Glide.with(mContext)
                .load(url)
                .apply(new RequestOptions().dontAnimate())
                .into(ivCover);
    }

    private void notifyDataCountChanged(int count) {
        header.setSongCount(count);
        header.setVisibility(count <= 0 ? View.GONE : View.VISIBLE);
    }
}
