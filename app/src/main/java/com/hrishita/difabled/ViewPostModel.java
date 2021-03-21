package com.hrishita.difabled;

import java.io.Serializable;

class ViewPostModel implements Serializable {
    String caption;
    String creatorId;
    String creatorName;
    String paymentLink;
    String creation_date;
    String creator_profile_link;
    String[] urls;
    Boolean liked = Boolean.FALSE;
    String postId;
}
