package org.coursera.camppotlatch.client.model;

import android.graphics.Bitmap;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by Fabio on 15/11/2014.
 */
public class User {
    public static final int THUMBNAIL_WIDTH = 128;
    public static final int THUMBNAIL_HEIGHT = 128;

    public static final String DEFAULT_ROLES = "USER";

    @Expose
    private String login;
    @Expose
    private String password;
    @Expose
    private String roles;
    @Expose
    private String name;
    @Expose
    private String email;

    @Expose
    private String imageId;

    private String imageContentType;
    private String imagePath;
    private WeakReference<Bitmap> image;

    @Expose
    private String city;
    @Expose
    private String country;
    @Expose
    private Date createTime;
    @Expose
    private Boolean disableInappropriate;
    @Expose
    private Integer giftsReloadPeriod;

    @Expose
    private Integer likesCount;
    @Expose
    private Integer inappropriatesCount;
    @Expose
    private Integer postedGiftsCount;

    private int index;

    private Long version;

    public User() {
    }

    public User(String login, String password, String roles,
                String name, String email, String imageId, String city, String country,
                Date createTime, Boolean disableInappropriate, Integer giftsReloadPeriod) {
        this.login = login;
        this.password = password;
        this.roles = roles;

        this.name = name;
        this.email = email;

        this.imageId = imageId;

        this.city = city;
        this.country = country;

        this.createTime = createTime;

        this.disableInappropriate = disableInappropriate;
        this.giftsReloadPeriod = giftsReloadPeriod;

        this.likesCount = 0;
        this.inappropriatesCount = 0;
        this.postedGiftsCount = 0;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }
    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageContentType() {
        return imageContentType;
    }
    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public WeakReference<Bitmap> getImage() {
        return image;
    }
    public void setImage(WeakReference<Bitmap> image) {
        this.image = image;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getDisableInappropriate() {
        return disableInappropriate;
    }
    public void setDisableInappropriate(Boolean disableInappropriate) {
        this.disableInappropriate = disableInappropriate;
    }

    public Integer getGiftsReloadPeriod() {
        return giftsReloadPeriod;
    }
    public void setGiftsReloadPeriod(Integer giftsReloadPeriod) {
        this.giftsReloadPeriod = giftsReloadPeriod;
    }

    public Integer getLikesCount() {
        return likesCount;
    }
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getInappropriatesCount() {
        return inappropriatesCount;
    }
    public void setInappropriatesCount(Integer inappropriatesCount) {
        this.inappropriatesCount = inappropriatesCount;
    }

    public Integer getPostedGiftsCount() { return postedGiftsCount; }
    public void setPostedGiftsCount(Integer postedGiftsCount) {
        this.postedGiftsCount = postedGiftsCount;
    }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    /**
     * Two users will generate the same hashcode if they have exactly the same
     * values for their name.
     *
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(name);
    }

    /**
     * Two users are considered equal if they have exactly the same values for
     * their name.
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User other = (User) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(name, other.name);
        } else {
            return false;
        }
    }
}
