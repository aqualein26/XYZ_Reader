package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final float PARALLAX_FACTOR = 1.25f;
    FloatingActionButton fab;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private android.support.v7.widget.Toolbar mToolbar;


    private TextView bylineView;
    private TextView titleView;
    private TextView bodyView;
    private LinearLayout metaBar;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }


        setHasOptionsMenu(true);


    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);


        getLoaderManager().initLoader(0, null, this);
        return mRootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mCoordinatorLayout = (CoordinatorLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        fab = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        metaBar = (LinearLayout) mRootView.findViewById(R.id.meta_bar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.photo_container);
        // set up the toolbar
        mToolbar = (android.support.v7.widget.Toolbar) mRootView.findViewById(R.id.toolbar);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setAlpha(0f);
            fab.setScaleX(0f);
            fab.setScaleY(0f);
            fab.setTranslationZ(1f);
            fab.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationZ(25f)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setStartDelay(300)
                    .start();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));

            }
        });


    }


    private void bindViews() {
        if (mRootView == null) {
            return;
        }


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));


        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);
            final String title = mCursor.getString(ArticleLoader.Query.TITLE);

            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).substring(0, 1000).replaceAll("(\r\n|\n)", "<br />")));
            if (mToolbar != null) {
                ((ArticleDetailActivity) getActivity()).setSupportActionBar(mToolbar);
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                ((ArticleDetailActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ArticleDetailActivity) getActivity()).supportFinishAfterTransition();

                    }
                });
            }
            Typeface mainTypeface = Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf");
            titleView.setText(title);
            bodyView.setTypeface(mainTypeface);
            bylineView.setTypeface(mainTypeface);
            titleView.setTypeface(mainTypeface);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPhotoView.setTransitionName(getResources().getString(R.string.transition));
            }

            Picasso.get().load((mCursor.getString(ArticleLoader.Query.PHOTO_URL))).into(mPhotoView, new Callback() {
                @Override
                public void onSuccess() {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().startPostponedEnterTransition();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().startPostponedEnterTransition();
                    }
                }
            });


        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {


        mCursor = cursor;

        if (mCursor != null && !mCursor.moveToFirst()) {

            mCursor.close();
            mCursor = null;
        }
        bindViews();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;

    }


}
