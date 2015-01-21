package org.coursera.camppotlatch.client.model;

import android.graphics.Bitmap;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Date;

public class Gift implements Serializable {
    @Expose
	private String id;
    @Expose
	private String title;

    @Expose
    private String imageId;

    private String imageContentType;
    private String imagePath;
    private WeakReference<Bitmap> image;
    private String thumbnailImagePath;
    private WeakReference<Bitmap> thumbnailImage;

    @Expose
    private String comments;
    @Expose
    private String creatorLogin;
    @Expose
    private Date createTime;
    @Expose
    private Integer likesCount;
    @Expose
    private Integer inappropriateCount;
    @Expose
    private String captionGiftId;

    @Expose
    private String creatorName;
    @Expose
    private Integer relatedCount;
    @Expose
    private Integer likeFlag;
    @Expose
    private Integer inappropriateFlag;

    private int index;
    private boolean selected;

	public Gift() {
	}

    // Caption gift constructor
    public Gift(String title, String imageId, String comments,
                String creatorLogin, String creatorName, Date createTime) {
        this(title, imageId, comments, creatorLogin, creatorName, createTime, null);
    }

    // Related gift constructor
    public Gift(String title, String imageId, String comments,
            String creatorLogin, String creatorName, Date createTime,
            String captionGiftId) {
		this.title = title;

        this.imageId = imageId;

        this.comments = comments;
        this.creatorLogin = creatorLogin;
        this.creatorName = creatorName;
        this.createTime = createTime;

        this.likesCount = 0;
        this.inappropriateCount = 0;

        this.captionGiftId = captionGiftId;

        this.relatedCount = 0;
        this.likeFlag = 0;
        this.inappropriateFlag = 0;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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

    public String getThumbnailImagePath() { return thumbnailImagePath; }
    public void setThumbnailImagePath (String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    public WeakReference<Bitmap> getThumbnailImage() {
        return thumbnailImage;
    }
    public void setThumbnailImage(WeakReference<Bitmap> thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCreatorLogin() {
        return creatorLogin;
    }
    public void setCreatorLogin(String creatorLogin) {
        this.creatorLogin = creatorLogin;
    }

    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getLikesCount() {
        return likesCount;
    }
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getInappropriateCount() {
        return inappropriateCount;
    }
    public void setInappropriateCount(Integer inappropriateCount) {
        this.inappropriateCount = inappropriateCount;
    }

    public String getCaptionGiftId() {
        return captionGiftId;
    }
    public void setCaptionGiftId(String captionGiftId) {
        this.captionGiftId = captionGiftId;
    }

    public String getCreatorName() {
        return creatorName;
    }
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Integer getRelatedCount() {
        return relatedCount;
    }
    public void setRelatedCount(Integer relatedCount) {
        this.relatedCount = relatedCount;
    }

    public Integer getLikeFlag() {
        return likeFlag;
    }
    public void setLikeFlag(Integer likeFlag) {
        this.likeFlag = likeFlag;
    }

    public Integer getInappropriateFlag() {
        return inappropriateFlag;
    }
    public void setInappropriateFlag(Integer inappropriateFlag) {
        this.inappropriateFlag = inappropriateFlag;
    }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public boolean getSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    /**
     * Two Gifts will generate the same hashcode if they have exactly the same
     * values for their title and url.
     *
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(title, creatorLogin, createTime);
    }

    /**
     * Two Gifts are considered equal if they have exactly the same values for
     * their title, url.
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gift) {
            Gift other = (Gift) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(title, other.title)
                    && Objects.equal(creatorLogin, other.creatorLogin)
                    && Objects.equal(createTime, other.createTime);
        } else {
            return false;
        }
    }
}
