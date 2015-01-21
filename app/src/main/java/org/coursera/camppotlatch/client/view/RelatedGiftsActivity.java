package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import org.coursera.camppotlatch.R;

public class RelatedGiftsActivity extends Activity {
    public static final String CAPTION_GIFT_ID_EXTRA = "captionGiftId";

    private FragmentManager mFragmentManager;

    private Fragment mRelatedGiftsFragment;

    protected String mCaptionGiftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_gifts);

        mCaptionGiftId = getIntent().getStringExtra(CAPTION_GIFT_ID_EXTRA);

        // Get a reference to the FragmentManager
        mFragmentManager = getFragmentManager();

        mRelatedGiftsFragment = new RelatedGiftsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RelatedGiftsFragment.CAPTION_GIFT_ID_EXTRA, mCaptionGiftId);
        mRelatedGiftsFragment.setArguments(bundle);

        // Start a new FragmentTransaction
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        // Add the new gifts fragment to the layout
        fragmentTransaction.add(R.id.fragment_container, mRelatedGiftsFragment);

        // Commit the FragmentTransaction
        fragmentTransaction.commit();
    }
}
