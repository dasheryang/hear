package hear.app.media;

/**
 * Created by power on 14-8-23.
 */
public interface PlayListener {

    public void onOtherStart();

    public void onComplete();

    public void onLoadingEnd();
}
