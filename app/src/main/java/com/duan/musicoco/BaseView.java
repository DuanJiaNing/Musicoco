/*
 * Copyright 2017 lizhaotailang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duan.musicoco;

import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by DuanJiaNing on 2017/4/22.
 */

public interface BaseView<T> {

    /**
     * set the presenter of mvp
     * @param presenter
     */
    void setPresenter(T presenter);

    /**
     * init the views of fragment or activity
     * @param view
     * @param obj
     */
    void initViews(@Nullable View view,@Nullable Object obj);


}
