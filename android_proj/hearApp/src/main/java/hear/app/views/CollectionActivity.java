package hear.app.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import hear.app.R;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class CollectionActivity extends BaseFragmentActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, CollectionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_container);
        getSupportFragmentManager().beginTransaction().add(R.id.container_fragment, CollectionFragment.newInstance()).commit();
    }
}
