package com.d.music.online.view;

import com.d.lib.common.module.loader.IAbsView;
import com.d.music.module.greendao.music.base.MusicModel;
import com.d.music.online.model.BillSongsRespModel;
import com.d.music.online.model.RadioSongsRespModel;

/**
 * IMusicView
 * Created by D on 2018/8/13.
 */
public interface IMusicView extends IAbsView<MusicModel> {
    void setInfo(BillSongsRespModel info);

    void setInfo(RadioSongsRespModel info);
}
