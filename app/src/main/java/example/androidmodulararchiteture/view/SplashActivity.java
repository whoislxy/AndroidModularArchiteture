package example.androidmodulararchiteture.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;


import com.alibaba.android.arouter.launcher.ARouter;

import example.androidmodulararchiteture.R;
import example.androidmodulararchiteture.databinding.AppActivitySplashLayoutBinding;
import example.androidmodulararchiteture.viewmodel.SplashViewModel;
import example.basiclib.activity.BaseActivity;
import example.basiclib.widget.CircleTextProgressbar;
import example.componentlib.component.ComponentManager;
import example.componentlib.component.interfaces.IDemoComponent;
import example.componentlib.service.ServiceManager;
import example.componentlib.service.account.Account;
import example.componentlib.service.account.IAccountService;
import example.demoaccountservice.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Splash
 * Email yummyl.lau@gmail.com
 * Created by yummylau on 2018/01/25.
 */

public class SplashActivity extends BaseActivity<SplashViewModel, AppActivitySplashLayoutBinding> {

    private Disposable mDisposable;
    private static final int PROGRESS_DEFAULT_WHAT = 0x01;

    @Override
    public Class<SplashViewModel> getViewModel() {
        return SplashViewModel.class;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.app_activity_splash_layout;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding.loadingProgress.setProgressType(CircleTextProgressbar.ProgressType.COUNT);
        dataBinding.loadingProgress.setProgressLineWidth(5);
        dataBinding.loadingProgress.setTimeMillis(3000);
        dataBinding.loadingProgress.setProgressColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
        dataBinding.loadingProgress.setOutLineColor(Color.WHITE);
        dataBinding.loadingProgress.setInCircleColor(Color.WHITE);
        dataBinding.loadingProgress.setCountdownProgressListener(PROGRESS_DEFAULT_WHAT, new CircleTextProgressbar.OnCountdownProgressListener() {
            @Override
            public void onProgress(int what, int progress) {
                if (what == PROGRESS_DEFAULT_WHAT && progress == 100) {
                    mDisposable = checkLoginStatus();
                }
            }
        });
        dataBinding.loadingProgress.start();
        dataBinding.loadingProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLoginStatus();
            }
        });
    }

    private Disposable checkLoginStatus() {
        return viewModel.checkLoginStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Account>() {
                    @Override
                    public void accept(Account account) throws Exception {
                        if (account == null) {
                            ServiceManager.getService(IAccountService.class).login(
                                    ComponentManager.getComponent(IDemoComponent.class).getMainPath());
                        } else {
                            //go to democomponent main activity
                            ComponentManager.getComponent(IDemoComponent.class).gotoMainActivity();
                        }
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(Constants.LOG_TAG, "check login status fail :" + throwable.getMessage());
                        ServiceManager.getService(IAccountService.class).login(
                                ComponentManager.getComponent(IDemoComponent.class).getMainPath());
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
