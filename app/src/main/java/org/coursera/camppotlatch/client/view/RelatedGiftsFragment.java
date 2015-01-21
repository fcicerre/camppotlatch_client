package org.coursera.camppotlatch.client.view;



import android.app.Activity;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class RelatedGiftsFragment extends AbstractGiftsFragment {
    public static final String CAPTION_GIFT_ID_EXTRA = "captionGiftId";

    private Activity mActivity;

    protected String mCaptionGiftId;

    @Override
    public void setArguments(Bundle bundle) {
        super.setArguments(bundle);

        mCaptionGiftId = bundle.getString(CAPTION_GIFT_ID_EXTRA);
        setCaptionGiftId(mCaptionGiftId);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    protected List<Gift> loadGifts(String mSearchText, int offset, int limit) {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        List<Gift> retrievedGiftsList = null;
        if (svc != null) {
            DateUtils dateUtils = new DateUtils();

            Calendar calendar = Calendar.getInstance();
            calendar.set(2014, Calendar.JANUARY, 1);
            Date minDate = calendar.getTime();

            String minDateStr = dateUtils.convertToISO8601DateFormat(minDate);
            String maxDateStr = dateUtils.convertToISO8601DateFormat(new Date());

            Collection<Gift> retrievedGifts = svc.findByCaptionGiftId(mCaptionGiftId,
                    minDateStr, maxDateStr, mSearchText,
                    AppContext.getUser().getDisableInappropriate(),
                    offset, limit);

            retrievedGiftsList = Lists.newArrayList(retrievedGifts);
        }

        return retrievedGiftsList;
    }
}
