package hear.app;

/**
 * Created by power on 14-8-22.
 */
public class ArticleLike {

    public static final String LIKE_ARTICLE="like_article_";

    public static final String ARTICLE_LIKE_COUNT="article_like_count";


    public static int getLikeArticle(int pageno){
        return AppContext.getSharedPrefernce().get(LIKE_ARTICLE+pageno,-1);
    }


    public static void setLikeArticle(int pageno,int i){
        AppContext.getSharedPrefernce().put(LIKE_ARTICLE+pageno,i);
    }

    public static void setLikeCount(int pageno,int count){
        AppContext.getSharedPrefernce().put(ARTICLE_LIKE_COUNT+pageno,count);
    }

    public static int getLikeCount(int pageno){
        return AppContext.getSharedPrefernce().get(ARTICLE_LIKE_COUNT + pageno, 0);
    }

    public static void descLikeCount(int pageno) {
        int likeCount=getLikeCount(pageno);
        if(likeCount>0){
            setLikeCount(pageno,likeCount-1);
        }
    }

    public static void incLikeCount(int pageno) {
        int likeCount=getLikeCount(pageno);
        setLikeCount(pageno,likeCount+1);
    }
}
