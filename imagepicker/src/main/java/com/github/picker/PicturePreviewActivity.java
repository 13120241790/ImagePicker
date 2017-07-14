package com.github.picker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.picker.photoview.PhotoView;
import com.github.picker.photoview.PhotoViewAttacher;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class PicturePreviewActivity extends Activity {

    static public final int RESULT_SEND = 1;

    private TextView mIndexTotal;
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageButton mBtnBack;
    private Button mBtnSend;
    private CheckButton mUseOrigin;
    private CheckButton mSelectBox;
    private HackyViewPager mViewPager;

    private ArrayList<PictureSelectorActivity.PicItem> mItemList;
    private int mCurrentIndex;
    private boolean mFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rc_picprev_activity);

        if (savedInstanceState != null) {
            mItemList = savedInstanceState.getParcelableArrayList("ItemList");
        }
        initView();

        mUseOrigin.setChecked(getIntent().getBooleanExtra("sendOrigin", false));
        mCurrentIndex = getIntent().getIntExtra("index", 0);
        mItemList = PictureSelectorActivity.PicItemHolder.itemList;
        mIndexTotal.setText(String.format("%d/%d", mCurrentIndex + 1, mItemList.size()));

        if (Build.VERSION.SDK_INT >= 11) {
            mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            int margin = getSmartBarHeight(this);
            if (margin > 0) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mToolbarBottom.getLayoutParams();
                lp.setMargins(0, 0, 0, margin);
                mToolbarBottom.setLayoutParams(lp);
            }
        }

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mToolbarTop.getLayoutParams());
        lp.setMargins(0, result, 0, 0);
        mToolbarTop.setLayoutParams(lp);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("sendOrigin", mUseOrigin.getChecked());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                ArrayList<Uri> list = new ArrayList<>();
                for (PictureSelectorActivity.PicItem item : mItemList) {
                    if (item.selected) {
                        list.add(Uri.parse("file://" + item.uri));
                    }
                }

                if (list.size() == 0) {
                    mSelectBox.setChecked(true);
                    list.add(Uri.parse("file://" + mItemList.get(mCurrentIndex).uri));
                }
                data.putExtra("sendOrigin", mUseOrigin.getChecked());
                data.putExtra(Intent.EXTRA_RETURN_RESULT, list);
                setResult(RESULT_SEND, data);
                finish();
            }
        });

        mUseOrigin.setText(R.string.rc_picprev_origin);
        mUseOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUseOrigin.setChecked(!mUseOrigin.getChecked());
                if (mUseOrigin.getChecked() && getTotalSelectedNum() == 0) {
                    mSelectBox.setChecked(!mSelectBox.getChecked());
                    mItemList.get(mCurrentIndex).selected = mSelectBox.getChecked();
                    updateToolbar();
                }
            }
        });
        mSelectBox.setText(R.string.rc_picprev_select);
        mSelectBox.setChecked(mItemList.get(mCurrentIndex).selected);
        mSelectBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSelectBox.getChecked() && getTotalSelectedNum() == 9) {
                    Toast.makeText(PicturePreviewActivity.this, R.string.rc_picsel_selected_max, Toast.LENGTH_SHORT).show();
                    return;
                }

                mSelectBox.setChecked(!mSelectBox.getChecked());
                mItemList.get(mCurrentIndex).selected = mSelectBox.getChecked();
                updateToolbar();
            }
        });

        mViewPager.setAdapter(new PreviewAdapter());
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mIndexTotal.setText(String.format("%d/%d", position + 1, mItemList.size()));
                mSelectBox.setChecked(mItemList.get(position).selected);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        updateToolbar();
    }

    private void initView() {
        mToolbarTop = findViewById(R.id.toolbar_top);
        mIndexTotal = (TextView) findViewById(R.id.index_total);
        mBtnBack = (ImageButton) findViewById(R.id.back);
        mBtnSend = (Button) findViewById(R.id.send);

        mWholeView = findViewById(R.id.whole_layout);
        mViewPager = (HackyViewPager) findViewById(R.id.viewpager);

        mToolbarBottom = findViewById(R.id.toolbar_bottom);
        mUseOrigin = new CheckButton(findViewById(R.id.origin_check), R.drawable.rc_origin_check_nor, R.drawable.rc_origin_check_sel);
        mSelectBox = new CheckButton(findViewById(R.id.select_check), R.drawable.rc_select_check_nor, R.drawable.rc_select_check_sel);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra("sendOrigin", mUseOrigin.getChecked());
            setResult(RESULT_OK, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getTotalSelectedNum() {
        int sum = 0;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).selected) {
                sum++;
            }
        }
        return sum;
    }

    private String getTotalSelectedSize() {
        float size = 0;
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).selected) {
                File file = new File(mItemList.get(i).uri);
                size = size + file.length() / 1024;
            }
        }

        String totalSize;
        if (size < 1024) {
            totalSize = String.format("%.0fK", size);
        } else {
            totalSize = String.format("%.1fM", size / 1024);
        }
        return totalSize;
    }

    private void updateToolbar() {
        int selNum = getTotalSelectedNum();
        if (mItemList.size() == 1 && selNum == 0) {
            mBtnSend.setText(R.string.rc_picsel_toolbar_send);
            mUseOrigin.setText(R.string.rc_picprev_origin);
            return;
        }

        if (selNum == 0) {
            mBtnSend.setText(R.string.rc_picsel_toolbar_send);
            mUseOrigin.setText(R.string.rc_picprev_origin);
        } else if (selNum <= 9) {
            mBtnSend.setText(String.format(getResources().getString(R.string.rc_picsel_toolbar_send_num), selNum));
            mUseOrigin.setText(String.format(getResources().getString(R.string.rc_picprev_origin_size), getTotalSelectedSize()));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int getSmartBarHeight(Context context) {
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("mz_action_button_min_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private class PreviewAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    mFullScreen = !mFullScreen;

                    if (mFullScreen) {
                        if (Build.VERSION.SDK_INT < 16) {
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        } else {
                            View decorView = getWindow().getDecorView();
                            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                        mToolbarTop.setVisibility(View.INVISIBLE);
                        mToolbarBottom.setVisibility(View.INVISIBLE);
                    } else {
                        if (Build.VERSION.SDK_INT < 16) {
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        } else {
                            View decorView = getWindow().getDecorView();
                            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                        mToolbarTop.setVisibility(View.VISIBLE);
                        mToolbarBottom.setVisibility(View.VISIBLE);
                    }
                }
            });

            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            String path = mItemList.get(position).uri;
            AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
            AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
            Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, 0, 0, new AlbumBitmapCacheHelper.ILoadImageCallback() {
                @Override
                public void onLoadImageCallBack(Bitmap bitmap, String p, Object... objects) {
                    if (bitmap == null) {
                        return;
                    }
                    photoView.setImageBitmap(bitmap);
                }
            }, position);
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
            } else {
                photoView.setImageResource(R.drawable.rc_grid_image_default);
            }
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class CheckButton {

        private View rootView;
        private ImageView image;
        private TextView text;

        private boolean checked = false;
        private int nor_resId;
        private int sel_resId;

        public CheckButton(View root, @DrawableRes int norId, @DrawableRes int selId) {
            rootView = root;
            image = (ImageView) root.findViewById(R.id.image);
            text = (TextView) root.findViewById(R.id.text);

            nor_resId = norId;
            sel_resId = selId;
            image.setImageResource(nor_resId);
        }

        public void setChecked(boolean check) {
            checked = check;
            image.setImageResource(checked ? sel_resId : nor_resId);
        }

        public boolean getChecked() {
            return checked;
        }

        public void setText(int resId) {
            text.setText(resId);
        }

        public void setText(CharSequence chars) {
            text.setText(chars);
        }

        public void setOnClickListener(@Nullable View.OnClickListener l) {
            rootView.setOnClickListener(l);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mItemList != null && mItemList.size() > 0) {
            outState.putParcelableArrayList("ItemList", mItemList);
        }
        super.onSaveInstanceState(outState);
    }
}
