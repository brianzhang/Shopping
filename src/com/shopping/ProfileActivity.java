package com.shopping;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Layout;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: ahkj
 * Date: 27/11/11
 * Time: 14.42
 * Similar to GroupProfileActivity, just view pushing stuff.
 */
public class ProfileActivity extends android.app.Activity {
    private User user;
    private View previousView;
    public static String SELECTED_USER = "selected_user";
    private Timer timer;
    public static final String SHOPPING_FRIENDS = "user_shopping_friends";
    private ArrayList<User> shoppingFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        Bundle bundle = getIntent().getExtras();
        user = (User)bundle.getParcelable(ProfileActivity.SELECTED_USER);
        shoppingFriends = bundle.getParcelableArrayList(ProfileActivity.SHOPPING_FRIENDS);
        setUserImage(user.getImageUrl());
        TextView tv = (TextView)findViewById(R.id.userTextView);
        String text = "";
        if(user.getFullName()==null || user.getFullName().isEmpty())
            text = "" + user.getUserId();
        else text = user.getFirstName();
        tv.setText(text);

                        //listener for left home button, shopping cart
        Button lhome = (Button)findViewById(R.id.profilelhomebtn);
        lhome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                timer.cancel();
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.putParcelableArrayListExtra(GalleryActivity.ACTIVE_USERS, shoppingFriends);
                startActivity(intent);
            }
        });

        //listener for left home button, shopping cart
        Button rhome = (Button)findViewById(R.id.profilerhomebtn);
        rhome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                timer.cancel();
                finish();
            }
        });

        setupButtonListeners();
        restartTimer();
    }

    private void setProfileStatus(View view) {
        RelativeLayout statusLayout = (RelativeLayout)findViewById(R.id.statusLayout);
        statusLayout.removeAllViews();
        statusLayout.invalidate();
        updateButtonColor(view);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        TextView statusText = new TextView(this);
        statusText.setLayoutParams(params);
        statusText.setTextSize(20.0f);
        String text;
        if(user.getUserActivity()==UserActivity.Shopping)
            text = user.getFirstName() + " er på indkøb " + (user.getLocation().isEmpty() ? "" : "i " + user.getLocation());
        else
        text = "Ingen status på " + user.getFirstName();
        statusText.setText(text);
        statusText.setTextSize(40);
        statusLayout.addView(statusText);
    }

    private void setWeekOverview(View view) {
        RelativeLayout statusLayout = (RelativeLayout)findViewById(R.id.statusLayout);
//        ArrayList<ArrayList<Movable>> weekActivity = FetchActivityTask.getWeekActivity(HomeActivity.USER_ID);
//        populateWeekView(weekActivity);
        statusLayout.removeAllViews();
        statusLayout.invalidate();
        updateButtonColor(view);
    }


    private void populateWeekView(ArrayList<ArrayList<Movable>> weekActivity) {

    }

    private void setSparks(View view) {
        RelativeLayout statusLayout = (RelativeLayout)findViewById(R.id.statusLayout);
        statusLayout.removeAllViews();
        statusLayout.invalidate();
        updateButtonColor(view);

        ArrayList<ShoppingOffer> offers =  FetchActivityTask.getAllOffersForUser("user01");

    }

    private void setShoppingStats(View view) {
        RelativeLayout statusLayout = (RelativeLayout)findViewById(R.id.statusLayout);
        statusLayout.removeAllViews();
        statusLayout.invalidate();
        updateButtonColor(view);
    }


    private void setupButtonListeners() {
        Button b = (Button)findViewById(R.id.profileBtn1);
        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                restartTimer();
                setProfileStatus( view);
            }
        });
        //Initialize with the profile view set
        setProfileStatus(b);

        b = (Button)findViewById(R.id.profileBtn2);
        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                restartTimer();
                setWeekOverview(view);
            }
        });

        b = (Button)findViewById(R.id.profileBtn3);
        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                restartTimer();
                setSparks( view);
            }
        });

        //Flest indkøb
//        b = (Button)findViewById(R.id.profileBtn4);
//        b.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View view) {
//                restartTimer();
//                setShoppingStats( view);
//            }
//        });
    }

    private void updateButtonColor(View newButton){
        //Update button row
        if(previousView != null)
            previousView.setBackgroundColor(getResources().getColor(R.color.white));
        newButton.setBackgroundColor(getResources().getColor(R.color.myblue));
        previousView = newButton;
    }

        private void restartTimer(){
        if(timer!=null)timer.cancel();
        timer = new Timer("sleeptime");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                ArrayList<User> objs = new ArrayList<User>();
                objs.addAll(shoppingFriends);
                objs.addAll(FetchActivityTask.getObjectsActivity(HomeActivity.USER_ID));
                intent.putParcelableArrayListExtra(GalleryActivity.ACTIVE_USERS, objs);
                startActivity(intent);
            }
        },GalleryActivity.SLEEP_DELAY);
    }


    //Get image from server
    private void setUserImage(String url){
        Drawable image = ImageOperations(this, url, "image.jpg");
        ImageView iv = (ImageView)findViewById(R.id.userView);
        iv.setImageDrawable(image);
    }

    private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }
}
