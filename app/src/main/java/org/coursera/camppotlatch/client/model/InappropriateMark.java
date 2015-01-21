package org.coursera.camppotlatch.client.model;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by Fabio on 15/11/2014.
 */
public class InappropriateMark {

    @Expose
    private String giftId;
    @Expose
    private String userLogin;
    @Expose
    private Date createTime;

    public InappropriateMark() {
    }

    public InappropriateMark(String giftId, String userLogin, Date createTime) {
        this.giftId = giftId;
        this.userLogin = userLogin;
        this.createTime = createTime;
    }

    public String getGiftId() {
        return giftId;
    }
    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getUserLogin() {
        return userLogin;
    }
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Two inappropriate marks will generate the same hashcode if they have exactly the same
     * values for their giftId and userLogin.
     *
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(giftId, userLogin);
    }

    /**
     * Two inappropriate marks are considered equal if they have exactly the same values for
     * their giftId and userLogin.
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InappropriateMark) {
            InappropriateMark other = (InappropriateMark) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(giftId, other.giftId)
                    && Objects.equal(userLogin, other.userLogin);
        } else {
            return false;
        }
    }
}
