package org.coursera.camppotlatch.client.commons;

import android.graphics.Bitmap;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.coursera.camppotlatch.client.model.User;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Fabio on 22/11/2014.
 */
public class UserUtils {
    public static Bitmap loadImageBitmap(User user) {
        WeakReference<Bitmap> imageRef = user.getImage();
        Bitmap imageBitmap = (imageRef != null) ? imageRef.get() : null;
        if (imageBitmap != null)
            return imageBitmap;

        String imagePath = user.getImagePath();
        if (imagePath == null)
            return null;

        imageBitmap = ImageUtils.getBitmap(imagePath);
        user.setImage(new WeakReference(imageBitmap));

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
    public static List<User> sortByCreationTimeDesc(Collection<User> users) {
        SortedMap<Date, User> sortedUsers = new TreeMap<Date, User>(dateDescComparator);
        for (User user : users) {
            sortedUsers.put(user.getCreateTime(), user);
        }

        return Lists.newArrayList(sortedUsers.values());
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
    public static List<User> sortByLikesAndCreationTimeDesc(Collection<User> users) {
        SortedMap<LikesCreationDate, User> sortedUsers =
                new TreeMap<LikesCreationDate, User>(topGiftsDescDateDescComparator);
        for (User user : users) {
            sortedUsers.put(new LikesCreationDate(user.getLikesCount(), user.getCreateTime()), user);
        }

        return Lists.newArrayList(sortedUsers.values());
    }

    public static void updateUserData(User targetUser, User user) {
        targetUser.setName(user.getName());
        targetUser.setEmail(user.getEmail());

        targetUser.setCity(user.getCity());
        targetUser.setCountry(user.getCountry());

        targetUser.setCreateTime(user.getCreateTime());

        targetUser.setPassword(user.getPassword());

        targetUser.setDisableInappropriate(user.getDisableInappropriate());
        targetUser.setGiftsReloadPeriod(user.getGiftsReloadPeriod());

        targetUser.setLikesCount(user.getLikesCount());
        targetUser.setInappropriatesCount(user.getInappropriatesCount());
        targetUser.setPostedGiftsCount(user.getPostedGiftsCount());

        targetUser.setIndex(user.getIndex());
        targetUser.setImagePath(user.getImagePath());
    }

    public static boolean equalsUserData(User targetUser, User user) {
        boolean result = true;

        result &= targetUser.getName().equals(user.getName());
        result &= targetUser.getEmail().equals(user.getEmail());

        result &= targetUser.getCity().equals(user.getCity());
        result &= targetUser.getCountry().equals(user.getCountry());

        result &= targetUser.getCreateTime().equals(user.getCreateTime());

        //targetUser.setPassword(user.getPassword());

        result &= targetUser.getDisableInappropriate().equals(user.getDisableInappropriate());
        result &= targetUser.getGiftsReloadPeriod().equals(user.getGiftsReloadPeriod());

        result &= targetUser.getLikesCount().equals(user.getLikesCount());
        result &= targetUser.getInappropriatesCount().equals(user.getInappropriatesCount());
        result &= targetUser.getPostedGiftsCount().equals(user.getPostedGiftsCount());

        //targetUser.setIndex(user.getIndex());
        result &= targetUser.getImagePath().equals(user.getImagePath());

        return result;
    }
}
