package org.coursera.camppotlatch.client.commons;

import android.graphics.Bitmap;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.coursera.camppotlatch.client.model.Gift;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Fabio on 10/11/2014.
 */
public class GiftUtils {
    public static Bitmap loadThumbnailImageBitmap(Gift gift) {
        WeakReference<Bitmap> thumbnailRef = gift.getThumbnailImage();
        Bitmap thumbnailBitmap = (thumbnailRef != null) ? thumbnailRef.get() : null;
        if (thumbnailBitmap != null)
            return thumbnailBitmap;

        String thumbnailImagePath = gift.getThumbnailImagePath();
        if (thumbnailImagePath == null)
            return null;

        thumbnailBitmap = ImageUtils.getBitmap(thumbnailImagePath);
        gift.setThumbnailImage(new WeakReference(thumbnailBitmap));

        return thumbnailBitmap;
    }

    public static Bitmap loadImageBitmap(Gift gift) {
        WeakReference<Bitmap> imageRef = gift.getImage();
        Bitmap imageBitmap = (imageRef != null) ? imageRef.get() : null;
        if (imageBitmap != null)
            return imageBitmap;

        String imagePath = gift.getImagePath();
        if (imagePath == null)
            return null;

        imageBitmap = ImageUtils.getBitmap(imagePath);
        gift.setImage(new WeakReference(imageBitmap));

        return imageBitmap;
    }

    public static Bitmap loadImageBitmap(Gift gift, int targetW, int targetH, boolean crop) {
        WeakReference<Bitmap> imageRef = gift.getImage();
        Bitmap imageBitmap = (imageRef != null) ? imageRef.get() : null;
        if (imageBitmap != null)
            return imageBitmap;

        String imagePath = gift.getImagePath();
        if (imagePath == null)
            return null;

        imageBitmap = ImageUtils.getBitmap(imagePath, targetW, targetH, crop);
        gift.setImage(new WeakReference(imageBitmap));

        return imageBitmap;
    }

    // Sorts by creation date in descending order
    final static Comparator<Date> dateDescComparator = new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return -o1.compareTo(o2);
        }
    };

    // Sorts by creation date in descending order
    public static List<Gift> sortByCreationTimeDesc(Collection<Gift> gifts) {
        SortedMap<Date, Gift> sortedGifts = new TreeMap<Date, Gift>(dateDescComparator);
        for (Gift gift : gifts) {
            sortedGifts.put(gift.getCreateTime(), gift);
        }

        return Lists.newArrayList(sortedGifts.values());
    }

    // Likes and creation date class
    private static class LikesCreationDate implements Comparable<LikesCreationDate> {
        public Integer likes;
        public Date creationDate;
        public LikesCreationDate(Integer likes, Date creationDate) {
            this.likes = likes;
            this.creationDate = creationDate;
        }

        @Override
        public int hashCode() {
            // Google Guava provides great utilities for hashing
            return Objects.hashCode(likes, creationDate);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LikesCreationDate) {
                LikesCreationDate other = (LikesCreationDate) obj;
                // Google Guava provides great utilities for equals too!
                return Objects.equal(likes, other.likes)
                        && Objects.equal(creationDate, other.creationDate);
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(LikesCreationDate o) {
            if (likes != o.likes)
                return (likes < o.likes) ? -1 : 1;
            else if (!creationDate.equals(o.creationDate))
                return creationDate.compareTo(o.creationDate);
            else
                return 0;
        }
    }

    // Sorts by likes and creation date in descending order
    final static Comparator<LikesCreationDate> topGiftsDescDateDescComparator =
            new Comparator<LikesCreationDate>() {
        @Override
        public int compare(LikesCreationDate o1, LikesCreationDate o2) {
            return -o1.compareTo(o2);
        }
    };

    // Sorts by likes and creation date in descending order
    public static List<Gift> sortByLikesAndCreationTimeDesc(Collection<Gift> gifts) {
        SortedMap<LikesCreationDate, Gift> sortedGifts =
                new TreeMap<LikesCreationDate, Gift>(topGiftsDescDateDescComparator);
        for (Gift gift : gifts) {
            sortedGifts.put(new LikesCreationDate(gift.getLikesCount(), gift.getCreateTime()), gift);
        }

        return Lists.newArrayList(sortedGifts.values());
    }

    public static void updateGiftData(Gift targetGift, Gift gift) {
        targetGift.setTitle(gift.getTitle());
        targetGift.setComments(gift.getComments());

        targetGift.setCreatorLogin(gift.getCreatorLogin());
        targetGift.setCreatorName(gift.getCreatorName());
        targetGift.setCreateTime(gift.getCreateTime());

        targetGift.setLikesCount(gift.getLikesCount());
        targetGift.setInappropriateCount(gift.getInappropriateCount());

        targetGift.setCaptionGiftId(gift.getCaptionGiftId());
        targetGift.setRelatedCount(gift.getRelatedCount());

        targetGift.setLikeFlag(gift.getLikeFlag());
        targetGift.setInappropriateFlag(gift.getInappropriateFlag());

        targetGift.setIndex(gift.getIndex());
        targetGift.setImagePath(gift.getImagePath());
        targetGift.setThumbnailImagePath(gift.getThumbnailImagePath());
    }

    public static boolean equalsGiftData(Gift targetGift, Gift gift) {
        boolean result = true;

        result &= targetGift.getTitle().equals(gift.getTitle());
        result &= targetGift.getComments().equals(gift.getComments());

        result &= targetGift.getCreatorLogin().equals(gift.getCreatorLogin());
        result &= targetGift.getCreatorName().equals(gift.getCreatorName());
        result &= targetGift.getCreateTime().equals(gift.getCreateTime());

        result &= targetGift.getLikesCount().equals(gift.getLikesCount());
        result &= targetGift.getInappropriateCount().equals(gift.getInappropriateCount());

        result &= targetGift.getCaptionGiftId().equals(gift.getCaptionGiftId());
        result &= targetGift.getRelatedCount().equals(gift.getRelatedCount());

        result &= targetGift.getLikeFlag().equals(gift.getLikeFlag());
        result &= targetGift.getInappropriateFlag().equals(gift.getInappropriateFlag());

        //targetGift.setIndex(gift.getIndex());
        //targetGift.setImagePath(gift.getImagePath());
        //targetGift.setThumbnailImagePath(gift.getThumbnailImagePath());

        return result;
    }
}
